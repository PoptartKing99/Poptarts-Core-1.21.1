param(
    [string]$FireSource = 'C:\Users\badas\Downloads\New folder'
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$modelDir = Join-Path $repo 'src/main/resources/assets/poptartcore/models/block/crucible'
$textureDir = Join-Path $repo 'src/main/resources/assets/poptartcore/textures/block/crucible'
$blockstatePath = Join-Path $repo 'src/main/resources/assets/poptartcore/blockstates/crucible.json'

$litLogs = @{
    soul = 'minecraft:block/soul_campfire_log_lit'
    rift = 'poptartcore:block/rift_campfire_top_lit'
    cupric = 'caverns_and_chasms:block/cupric_campfire_log_lit'
}

foreach ($type in @('soul', 'rift', 'cupric')) {
    foreach ($kind in @('base', 'hot', 'fire')) {
        $parent = switch ($kind) {
            base { 'crucible' }
            hot { 'crucible_hot' }
            fire { 'crucible_fire' }
        }
        $textures = switch ($kind) {
            base { @{ particle = 'minecraft:block/campfire_log'; lit_log = $litLogs[$type] } }
            hot { @{ particle = 'minecraft:block/campfire_log'; lit_log = $litLogs[$type] } }
            fire { @{ fire = "poptartcore:block/crucible/crucible_${type}_fire" } }
        }
        $model = @{ parent = "poptartcore:block/crucible/$parent"; textures = $textures }
        $modelPath = Join-Path $modelDir "crucible_${type}_$kind.json"
        $model | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $modelPath -Encoding utf8
    }

    foreach ($extension in @('.png', '.png.mcmeta')) {
        $name = "crucible_${type}_fire$extension"
        Copy-Item -LiteralPath (Join-Path $FireSource $name) -Destination (Join-Path $textureDir $name) -Force
    }
}

$blockstate = Get-Content -LiteralPath $blockstatePath -Raw | ConvertFrom-Json
$normalParts = @($blockstate.multipart | Where-Object { -not $_.when.campfire_type -or $_.when.campfire_type -eq 'normal' })
$parts = [System.Collections.Generic.List[object]]::new()
foreach ($type in @('normal', 'soul', 'rift', 'cupric')) {
    foreach ($normalPart in $normalParts) {
        $part = $normalPart | ConvertTo-Json -Depth 20 | ConvertFrom-Json
        $part.when | Add-Member -NotePropertyName campfire_type -NotePropertyValue $type -Force
        if ($type -ne 'normal') {
            $model = $part.apply.model
            if ($model -eq 'poptartcore:block/crucible/crucible') {
                $part.apply.model = "poptartcore:block/crucible/crucible_${type}_base"
            } elseif ($model -eq 'poptartcore:block/crucible/crucible_hot') {
                $part.apply.model = "poptartcore:block/crucible/crucible_${type}_hot"
            } elseif ($model -eq 'poptartcore:block/crucible/crucible_fire') {
                $part.apply.model = "poptartcore:block/crucible/crucible_${type}_fire"
            }
        }
        $parts.Add($part)
    }
}
@{ multipart = $parts } | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $blockstatePath -Encoding utf8
