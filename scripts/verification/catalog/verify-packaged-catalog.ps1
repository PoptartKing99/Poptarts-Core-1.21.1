#requires -Version 7.0
param([string] $JarPath, [string] $CatalogManifest, [string] $ReferenceRingTextures, [string] $ReferenceWayfarerSources, [string] $ReferenceWorkbenchSources)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/../../lib/resources.ps1"
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../../..')).Path
$JarPath = Get-ModJarPath $repo $JarPath
$jar = Get-Item -LiteralPath $JarPath
if (-not $CatalogManifest) { $CatalogManifest = Join-Path $repo 'build/game-test-run/catalog-manifest.json' }
if (-not (Test-Path -LiteralPath $CatalogManifest)) { throw 'Run scripts/verify-mod.ps1 to generate the live catalog manifest.' }
if ((Get-Item -LiteralPath $CatalogManifest).LastWriteTimeUtc -lt $jar.LastWriteTimeUtc) {
    throw 'The catalog manifest predates this JAR. Run the full server verification again.'
}
$catalog = Get-Content -LiteralPath $CatalogManifest -Raw | ConvertFrom-Json -AsHashtable
if ($catalog.schemaVersion -ne 8) { throw 'Unsupported catalog manifest format. Run scripts/verify-mod.ps1 to refresh it.' }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead($jar.FullName)
try {
    $entries = @{}
    foreach ($entry in $archive.Entries) { $entries[$entry.FullName] = $entry }
    $errors = [Collections.Generic.List[string]]::new()
    $visited = [Collections.Generic.HashSet[string]]::new()
    $documents = @{}

    function Read-TextEntry([string] $path) {
        if (-not $entries.ContainsKey($path)) { $errors.Add("Missing packaged resource: $path"); return $null }
        $reader = [IO.StreamReader]::new($entries[$path].Open())
        try { return $reader.ReadToEnd() }
        finally { $reader.Dispose() }
    }

    function Read-Entry([string] $path) {
        if ($documents.ContainsKey($path)) { return $documents[$path] }
        $text = Read-TextEntry $path
        if ($null -eq $text) { return $null }
        if ($path -match '^(assets/poptartcore/(blockstates|models)/|data/poptartcore/loot_table/)') {
            foreach ($namespace in $catalog.forbiddenResourceNamespaces) {
                if ($text.IndexOf($namespace, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
                    $errors.Add("Obsolete resource namespace ${namespace}: $path")
                }
            }
        }
        $document = ConvertFrom-Json -AsHashtable -InputObject $text
        $documents[$path] = $document
        return $document
    }

    function Get-EntryHash([string] $path) {
        if (-not $entries.ContainsKey($path)) { $errors.Add("Missing packaged resource: $path"); return $null }
        $stream = $entries[$path].Open()
        $sha = [Security.Cryptography.SHA256]::Create()
        try { return [Convert]::ToHexString($sha.ComputeHash($stream)) }
        finally { $stream.Dispose(); $sha.Dispose() }
    }

    function Compare-ReferenceTexture([string] $path, [string] $reference) {
        if (-not (Test-Path -LiteralPath $reference -PathType Leaf)) { $errors.Add("Missing reference texture: $reference") }
        elseif ((Get-EntryHash $path) -ne (Get-FileHash -LiteralPath $reference -Algorithm SHA256).Hash) {
            $errors.Add("Reference texture differs: $path")
        }
    }

    function Get-JsonStrings($node) {
        if ($node -is [string]) { $node }
        elseif ($node -is [Collections.IDictionary]) {
            foreach ($child in $node.Values) { Get-JsonStrings $child }
        } elseif ($node -is [array]) {
            foreach ($child in $node) { Get-JsonStrings $child }
        }
    }

    function Get-JsonObjects($node) {
        if ($node -is [Collections.IDictionary]) {
            $node
            foreach ($child in $node.Values) { Get-JsonObjects $child }
        } elseif ($node -is [array]) {
            foreach ($child in $node) { Get-JsonObjects $child }
        }
    }

    function Test-JsonEqual($actual, $expected) {
        if ($expected -is [Collections.IDictionary]) {
            if ($actual -isnot [Collections.IDictionary] -or $actual.Count -ne $expected.Count) { return $false }
            foreach ($key in $expected.Keys) {
                if (-not $actual.Contains($key) -or -not (Test-JsonEqual $actual[$key] $expected[$key])) { return $false }
            }
            return $true
        }
        if ($expected -is [array]) {
            if ($actual -isnot [array] -or $actual.Count -ne $expected.Count) { return $false }
            for ($i = 0; $i -lt $expected.Count; $i++) {
                if (-not (Test-JsonEqual $actual[$i] $expected[$i])) { return $false }
            }
            return $true
        }
        return $actual -ceq $expected
    }

    function Check-Mixins($config, [string] $section, $required) {
        if ($null -eq $config) { return }
        foreach ($mixin in $required) {
            if ($config[$section] -cnotcontains $mixin) { $errors.Add("Missing required mixin registration: $mixin") }
            $package = [string]$config.package
            $class = "$($package.Replace('.', '/'))/$($mixin.Replace('.', '/')).class"
            if (-not $entries.ContainsKey($class)) { $errors.Add("Missing required mixin class: $class") }
        }
    }

    function Get-RecipeShape($recipe, [bool] $mirror = $false) {
        $rows = [Collections.Generic.List[string]]::new()
        foreach ($row in $recipe.pattern) { $rows.Add([string]$row) }
        while ($rows.Count -gt 0 -and $rows[0].Trim().Length -eq 0) { $rows.RemoveAt(0) }
        while ($rows.Count -gt 0 -and $rows[$rows.Count - 1].Trim().Length -eq 0) { $rows.RemoveAt($rows.Count - 1) }
        if ($rows.Count -eq 0 -or $rows.Count -gt 3) { return '<invalid pattern>' }
        $left = 3
        $right = -1
        foreach ($row in $rows) {
            foreach ($match in [regex]::Matches($row, '[^ ]')) {
                $left = [Math]::Min($left, $match.Index)
                $right = [Math]::Max($right, $match.Index)
            }
        }
        if ($left -gt $right -or $right -gt 2) { return '<invalid pattern>' }
        $shape = foreach ($row in $rows) {
            $cells = @()
            for ($column = $left; $column -le $right; $column++) {
                $symbol = if ($column -lt $row.Length) { [string]$row[$column] } else { ' ' }
                if ($symbol -eq ' ') { $cells += '' }
                elseif ($recipe.key[$symbol].item) { $cells += $recipe.key[$symbol].item }
                else { return '<invalid ingredient>' }
            }
            if ($mirror) { [Array]::Reverse($cells) }
            $cells -join ','
        }
        return $shape -join '/'
    }

    function Get-ShapelessIngredients($recipe) {
        $ingredients = foreach ($ingredient in $recipe.ingredients) {
            if (-not $ingredient.item) { return '<invalid ingredient>' }
            $ingredient.item
        }
        return ($ingredients | Sort-Object) -join ','
    }

    function Check-Tag([string] $tag, [string] $kind, [string] $member) {
        $location = $tag -split ':', 2
        $path = "data/$($location[0])/tags/$kind/$($location[1]).json"
        $document = Read-Entry $path
        if ($null -eq $document) { return }
        if ($document.replace -eq $true) { $errors.Add("$path replaces other mods' tag entries") }
        $values = @($document.values | ForEach-Object { if ($_ -is [Collections.IDictionary]) { $_.id } else { $_ } })
        if ($values -notcontains $member) { $errors.Add("$member is absent from its declared $kind tag $tag") }
    }

    foreach ($path in $entries.Keys | Where-Object {
        $_ -match '^(assets/(poptartcore|poptart_catalog)/|data/).+\.json$'
    }) { $null = Read-Entry $path }

    $handwrittenNames = Read-Entry 'assets/poptartcore/lang/en_us.json'
    $generatedNames = Read-Entry 'assets/poptart_catalog/lang/en_us.json'
    if ($null -eq $handwrittenNames) { $handwrittenNames = @{} }
    if ($null -eq $generatedNames) { $generatedNames = @{} }
    foreach ($key in $generatedNames.Keys) {
        if ($handwrittenNames.ContainsKey($key)) { $errors.Add("English translation has two owners: $key") }
    }
    $names = @{}
    foreach ($key in $generatedNames.Keys) { $names[$key] = $generatedNames[$key] }
    foreach ($key in $handwrittenNames.Keys) { $names[$key] = $handwrittenNames[$key] }
    foreach ($key in $catalog.requiredTranslations.Keys) {
        if ($names[$key] -cne $catalog.requiredTranslations[$key]) { $errors.Add("Wrong or missing original translation: $key") }
    }

    $mixins = Read-Entry 'poptartcore.mixins.json'
    Check-Mixins $mixins 'mixins' $catalog.requiredMixins
    $features = $catalog.featureAssets
    Check-Mixins $mixins 'mixins' $features.commonMixins
    Check-Mixins $mixins 'client' $features.clientMixins
    foreach ($path in $features.requiredResources) {
        if (-not $entries.ContainsKey($path)) { $errors.Add("Missing required resource: $path") }
    }
    foreach ($rule in $features.requiredText) {
        $content = Read-TextEntry $rule.path
        if ($null -ne $content -and -not $content.Contains($rule.text)) { $errors.Add("Missing required metadata: $($rule.path) $($rule.text)") }
    }
    foreach ($path in $features.referenceTextures.Keys) {
        if ($ReferenceWorkbenchSources) {
            Compare-ReferenceTexture $path (Join-Path $ReferenceWorkbenchSources $features.referenceTextures[$path])
        }
    }
    foreach ($path in $entries.Keys | Where-Object { $_ -match '^data/[^/]+/recipes?/' }) {
        if ([IO.Path]::GetFileNameWithoutExtension($path) -cin $features.forbiddenRecipeFilenames) {
            $errors.Add("Unexpected feature recipe: $path")
        }
    }
    foreach ($rule in $features.jsonRules) {
        $actual = Read-Entry $rule.path
        $found = $null -ne $actual
        if ($rule.pointer) {
            foreach ($part in $rule.pointer.Substring(1).Split('/')) {
                $key = $part.Replace('~1', '/').Replace('~0', '~')
                if ($actual -is [Collections.IDictionary] -and $actual.Contains($key)) { $actual = $actual[$key] }
                elseif ($actual -is [array] -and $key -match '^\d+$' -and [int]$key -lt $actual.Count) { $actual = $actual[[int]$key] }
                else { $found = $false; break }
            }
        }
        $valid = $found -and $(switch ($rule.mode) {
            'exists' { $true }
            'not-empty' { $actual.Count -gt 0 }
            'count' { $actual.Count -eq $rule.expected }
            'exact' { Test-JsonEqual $actual $rule.expected }
            'contains' {
                $absent = @($rule.expected | Where-Object { $wanted = $_; -not @($actual | Where-Object { Test-JsonEqual $_ $wanted }).Count })
                $absent.Count -eq 0
            }
            'values-prefix' {
                $invalid = @($actual.Values | Where-Object { $_ -notlike '#*' -and -not $_.StartsWith($rule.expected, [StringComparison]::Ordinal) })
                $invalid.Count -eq 0
            }
            default { throw "Unknown resource rule: $($rule.mode)" }
        })
        if (-not $valid) { $errors.Add("Wrong feature resource: $($rule.path)$($rule.pointer) [$($rule.mode)]") }
    }

    $equipment = $catalog.equipmentAssets
    $ringTag = Read-Entry 'data/poptartcore/tags/item/equipment/rings.json'
    foreach ($member in $ringTag.values) {
        # The test overlay must not hide a production ring from the loaded tag.
        $id = if ($member -is [Collections.IDictionary]) { $member.id } else { $member }
        if ($id -notlike '#*' -and $catalog.loadedRingTagMembers -notcontains $id) {
            $errors.Add("Test tag shadows a real ring: $id")
        }
    }
    foreach ($id in $equipment.items.Keys) {
        $contract = $equipment.items[$id]
        if ($catalog.items.id -notcontains $id) { $errors.Add("Equipment asset contract has no registered item: $id") }
        $path = "assets/poptartcore/models/item/$id.json"
        $model = Read-Entry $path
        if ($null -ne $model -and $model.textures.layer0 -cne "poptartcore:item/$id") {
            $errors.Add("Wrong equipment model texture: $id")
        }
        $texture = "assets/poptartcore/textures/item/$id.png"
        if (-not $entries.ContainsKey($texture)) { $errors.Add("Missing packaged resource: $texture") }
        if ($contract.ring) {
            if ([string]::IsNullOrWhiteSpace($names["item.poptartcore.$id.desc"])) { $errors.Add("Missing ring tooltip: $id") }
            Check-Tag 'poptartcore:equipment/rings' 'item' "poptartcore:$id"
            if (@($ringTag.values | Where-Object { $_ -eq "poptartcore:$id" }).Count -ne 1) {
                $errors.Add("Missing or duplicate ring-slot tag: $id")
            }
        }
        if ($entries.ContainsKey("data/poptartcore/recipe/$id.json")) { $errors.Add("Unexpected equipment recipe: $id") }
        if ($ReferenceRingTextures) {
            Compare-ReferenceTexture $texture (Join-Path $ReferenceRingTextures $contract.referenceTexture)
        }
    }
    foreach ($path in $catalog.requiredResources) {
        if (-not $entries.ContainsKey($path)) { $errors.Add("Missing required resource: $path") }
    }
    foreach ($path in $catalog.wayfarerReferenceTextures.Keys) {
        if (-not $entries.ContainsKey($path)) { $errors.Add("Missing packaged resource: $path") }
        if ($ReferenceWayfarerSources) {
            Compare-ReferenceTexture $path (Join-Path $ReferenceWayfarerSources $catalog.wayfarerReferenceTextures[$path])
        }
    }
    foreach ($path in $catalog.requiredAssetHashes.Keys) {
        if ((Get-EntryHash $path) -ne $catalog.requiredAssetHashes[$path]) { $errors.Add("Stale packaged equipment asset: $path") }
    }
    foreach ($path in $entries.Keys | Where-Object { $_ -match '^data/[^/]+/recipe/.+\.json$' }) {
        $namespace = ($path -split '/')[1]
        $excluded = @($equipment.items.Keys | Where-Object { $equipment.items[$_].noRecipeReferences -in @('*', $namespace) })
        if (-not $excluded.Count) { continue }
        foreach ($value in Get-JsonStrings (Read-Entry $path)) {
            if ($value -clike 'poptartcore:*' -and $excluded -ccontains $value.Substring('poptartcore:'.Length)) {
                $errors.Add("Unexpected equipment recipe reference: $value in $path")
            }
        }
    }
    $extensionPath = 'META-INF/enumextensions.json'
    $extensions = Read-Entry $extensionPath
    $wanted = $equipment.rarityExtension
    $matching = @($extensions.entries | Where-Object {
        $_.enum -ceq $wanted.enum -and $_.name -ceq $wanted.name -and $_.constructor -ceq $wanted.constructor -and
        $_.parameters.class -ceq $wanted.parameters.class -and $_.parameters.field -ceq $wanted.parameters.field
    })
    if ($matching.Count -ne 1) { $errors.Add('Missing or incorrect malevolent rarity extension') }
    $metadata = Read-TextEntry 'META-INF/neoforge.mods.toml'
    if ($metadata -cnotmatch '(?m)^\s*enumExtensions\s*=\s*"META-INF/enumextensions\.json"\s*(?:#.*)?$') {
        $errors.Add('Packaged rarity extension is not enabled')
    }

    function Check-Model([string] $reference, [string] $owner) {
        if ($reference -notmatch '^(poptartcore|poptart_catalog):(.+)$') { return }
        $path = "assets/$($Matches[1])/models/$($Matches[2]).json"
        if (-not $entries.ContainsKey($path)) { $errors.Add("$owner refers to missing model $path"); return }
        if ($visited.Add($path)) { Check-References (Read-Entry $path) $path }
    }

    function Check-References($node, [string] $owner) {
        if ($node -is [Collections.IDictionary]) {
            foreach ($key in $node.Keys) {
                if ($key -in @('parent', 'model') -and $node[$key] -is [string]) {
                    Check-Model $node[$key] $owner
                } elseif ($key -eq 'textures' -and $node[$key] -is [Collections.IDictionary]) {
                    foreach ($texture in $node[$key].Values) {
                        if ($texture -is [string] -and $texture -match '^(poptartcore|poptart_catalog):(.+)$') {
                            $path = "assets/$($Matches[1])/textures/$($Matches[2]).png"
                            if (-not $entries.ContainsKey($path)) { $errors.Add("$owner refers to missing texture $path") }
                        }
                    }
                } else { Check-References $node[$key] $owner }
            }
        } elseif ($node -is [array]) {
            foreach ($child in $node) { Check-References $child $owner }
        }
    }

    # Inspect every packaged model and blockstate, including helper variants outside the catalog.
    foreach ($path in $entries.Keys | Where-Object { $_ -match '^assets/(poptartcore|poptart_catalog)/models/.+\.json$' }) {
        if ($visited.Add($path)) { Check-References (Read-Entry $path) $path }
    }
    foreach ($path in $entries.Keys | Where-Object { $_ -match '^assets/(poptartcore|poptart_catalog)/blockstates/.+\.json$' }) {
        Check-References (Read-Entry $path) $path
    }

    foreach ($block in $catalog.blocks) {
        $id = $block.id
        $state = "assets/poptartcore/blockstates/$id.json"
        if (-not $entries.ContainsKey($state)) { $errors.Add("Missing blockstate: $state") }
        else { Check-References (Read-Entry $state) $state }
        $lootLocation = $block.lootTable -split ':', 2
        $lootPath = "data/$($lootLocation[0])/loot_table/$($lootLocation[1]).json"
        if ($block.lootTable -eq 'minecraft:empty') {
            if ($entries.ContainsKey("data/poptartcore/loot_table/blocks/$id.json")) { $errors.Add("No-loot block has a loot table: $id") }
        } else {
            $loot = Read-Entry $lootPath
            if ($block.loot -eq 'GENERATED' -and $null -ne $loot) {
                $drop = if ($block.oreDrop) { $block.oreDrop } else { "poptartcore:$id" }
                $dropEntries = @(Get-JsonObjects $loot | Where-Object { $_.type -eq 'minecraft:item' -and $_.name -eq $drop })
                if ($dropEntries.Count -eq 0) { $errors.Add("Wrong generated loot drop: $id must drop $drop") }
                if ($block.oreDrop) {
                    $bonus = @($dropEntries | ForEach-Object { $_.functions } | Where-Object {
                        $_.function -eq 'minecraft:apply_bonus' -and $_.enchantment -eq 'minecraft:fortune' -and $_.formula -eq 'minecraft:ore_drops'
                    })
                    $silk = @(Get-JsonObjects $loot | Where-Object { $_.type -eq 'minecraft:item' -and $_.name -eq "poptartcore:$id" })
                    $silkConditions = @(Get-JsonObjects @($silk | ForEach-Object { $_.conditions }) | Where-Object {
                        $_.enchantments -eq 'minecraft:silk_touch' -and $_.levels.min -eq 1
                    })
                    if ($bonus.Count -eq 0 -or $silkConditions.Count -eq 0) { $errors.Add("Missing Fortune or Silk Touch behavior: $id") }
                }
                if ($block.model -eq 'SLAB') {
                    $doubles = @(Get-JsonObjects $loot | Where-Object {
                        $_.function -eq 'minecraft:set_count' -and $_.count -eq 2 -and
                        @($_.conditions | Where-Object { $_.properties.type -eq 'double' }).Count -gt 0
                    })
                    if ($doubles.Count -eq 0) { $errors.Add("Double slab loot must return two slabs: $id") }
                }
            }
        }
        foreach ($tag in $block.tags) { Check-Tag $tag 'block' "poptartcore:$id" }

        if ($block.model -ne 'EXTERNAL') {
            $model = "poptartcore:block/$($block.modelPath)"
            $models = switch ($block.model) {
                'SIMPLE' { @($model) }
                'SLAB' { @($model, "${model}_top", "${model}_double") }
                'STAIRS' { @($model, "${model}_inner", "${model}_outer") }
                'WALL' { @("${model}_post", "${model}_side", "${model}_side_tall", "${model}_inventory") }
                { $_ -in @('RANDOM_CUBE', 'RANDOM_BOTTOM_TOP') } { @($model); for ($i = 1; $i -lt $block.variants; $i++) { "${model}_$i" } }
                default { $errors.Add("Unknown block model type for ${id}: $($block.model)"); @() }
            }
            foreach ($reference in $models) { Check-Model $reference $id }
            if ($block.model -eq 'SIMPLE') {
                $cube = Read-Entry "assets/poptartcore/models/block/$($block.modelPath).json"
                if ($null -ne $cube -and ($cube.parent -ne 'minecraft:block/cube_all' -or $cube.textures.all -ne $block.texture)) {
                    $errors.Add("Wrong cube model or texture: $id")
                }
            }
            $itemModel = Read-Entry "assets/poptartcore/models/item/$id.json"
            $parent = if ($block.model -eq 'WALL') { "${model}_inventory" } else { $model }
            if ($null -ne $itemModel -and $itemModel.parent -ne $parent) { $errors.Add("Wrong block item model parent: $id") }
        }
        if ($block.storage) {
            $storage = $block.storage
            $packing = Read-Entry "data/poptartcore/recipe/$($storage.packing).json"
            if ($null -ne $packing -and ($packing.type -ne 'minecraft:crafting_shaped' -or
                    ($packing.pattern -join '/') -ne '###/###/###' -or $packing.key.'#'.item -ne $storage.ingredient -or
                    $packing.result.id -ne "poptartcore:$id" -or ($packing.result.ContainsKey('count') -and $packing.result.count -ne 1))) {
                $errors.Add("Wrong packing recipe: $id")
            }
            $unpacking = Read-Entry "data/poptartcore/recipe/$($storage.unpacking).json"
            if ($null -ne $unpacking -and ($unpacking.type -ne 'minecraft:crafting_shapeless' -or
                    @($unpacking.ingredients).Count -ne 1 -or $unpacking.ingredients[0].item -ne "poptartcore:$id" -or
                    $unpacking.result.id -ne $storage.ingredient -or $unpacking.result.count -ne 9)) {
                $errors.Add("Wrong unpacking recipe: $id")
            }
        }
    }
    foreach ($expected in $catalog.craftingRecipes) {
        $recipe = Read-Entry "data/poptartcore/recipe/$($expected.id).json"
        if ($null -eq $recipe) { continue }
        $ingredientsMatch = switch ($expected.type) {
            'minecraft:crafting_shaped' {
                $shape = Get-RecipeShape $recipe
                $shape -ceq (Get-RecipeShape $expected) -or $shape -ceq (Get-RecipeShape $expected $true)
            }
            'minecraft:crafting_shapeless' { (Get-ShapelessIngredients $recipe) -ceq (Get-ShapelessIngredients $expected) }
            default { $false }
        }
        $count = if ($recipe.result.ContainsKey('count')) { $recipe.result.count } else { 1 }
        if ($recipe.type -ne $expected.type -or $recipe.result.id -ne $expected.result.id -or $count -ne $expected.result.count -or
                -not $ingredientsMatch) {
            $errors.Add("Wrong crafting recipe: $($expected.id)")
        }
    }
    foreach ($tag in $catalog.requiredBlockTags.Keys) {
        foreach ($member in $catalog.requiredBlockTags[$tag]) { Check-Tag $tag 'block' $member }
    }
    foreach ($tag in $catalog.requiredItemTags.Keys) {
        foreach ($member in $catalog.requiredItemTags[$tag]) { Check-Tag $tag 'item' $member }
    }
    foreach ($expected in $catalog.itemModelOverrides) {
        $model = Read-Entry "assets/poptartcore/models/item/$($expected.id).json"
        if ($null -eq $model) { continue }
        if ($model.parent -cne $expected.parent -or @($model.overrides).Count -ne @($expected.overrides).Count) {
            $errors.Add("Wrong item model overrides: $($expected.id)")
            continue
        }
        foreach ($texture in $expected.textures.Keys) {
            if ($model.textures[$texture] -cne $expected.textures[$texture]) { $errors.Add("Wrong item model override texture: $($expected.id)") }
        }
        for ($i = 0; $i -lt $expected.overrides.Count; $i++) {
            $actual = $model.overrides[$i]
            $wanted = $expected.overrides[$i]
            if ($actual.model -cne $wanted.model -or
                    (($actual.predicate.Keys | Sort-Object) -join ',') -cne (($wanted.predicate.Keys | Sort-Object) -join ',')) {
                $errors.Add("Wrong item model override at index ${i}: $($expected.id)")
                continue
            }
            foreach ($key in $wanted.predicate.Keys) {
                if ($actual.predicate[$key] -ne $wanted.predicate[$key]) { $errors.Add("Wrong item model override predicate ${key}: $($expected.id)") }
            }
        }
    }
    foreach ($id in @('millstone_structural', 'millstone_rotor')) {
        if ($entries.ContainsKey("data/poptartcore/loot_table/blocks/$id.json")) { $errors.Add("Internal helper block has a loot table: $id") }
    }
    foreach ($item in @($catalog.items) + @($catalog.blocks)) {
        foreach ($tag in $item.itemTags) { Check-Tag $tag 'item' "poptartcore:$($item.id)" }
        $key = $item.descriptionId
        if (-not $names.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($names[$key])) { $errors.Add("Missing English translation: $key") }
        elseif ($generatedNames.ContainsKey($key) -and $generatedNames[$key] -cne $item.name) { $errors.Add("Generated name disagrees with catalog: $key") }
        if ($item.fuel -gt 0) {
            $fuels = Read-Entry 'data/neoforge/data_maps/item/furnace_fuels.json'
            if ($null -ne $fuels -and $fuels.values["poptartcore:$($item.id)"].burn_time -ne $item.fuel) {
                $errors.Add("Wrong furnace fuel value: $($item.id)")
            }
        }
        $model = "assets/poptartcore/models/item/$($item.id).json"
        if (-not $entries.ContainsKey($model)) {
            if ($item.visible) { $errors.Add("Visible catalog entry lacks item model: $model") }
        } else { Check-References (Read-Entry $model) $model }
    }
    foreach ($item in $catalog.items | Where-Object model -In @('GENERATED', 'HANDHELD')) {
        $path = "assets/poptartcore/models/item/$($item.id).json"
        $model = Read-Entry $path
        $parent = "minecraft:item/$($item.model.ToLowerInvariant())"
        if ($null -ne $model -and ($model.parent -ne $parent -or $model.textures.layer0 -ne "poptartcore:item/$($item.id)")) {
            $errors.Add("Wrong item model parent or texture: $($item.id)")
        }
    }
    if ($errors.Count) { throw ($errors -join "`n") }
    Write-Host "Verified $($catalog.blocks.Count) catalog blocks, $($catalog.items.Count) items and $($visited.Count) packaged models in $($jar.Name)."
} finally { $archive.Dispose() }
