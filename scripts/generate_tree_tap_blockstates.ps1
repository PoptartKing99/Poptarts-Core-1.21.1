$ErrorActionPreference = 'Stop'
$target = Join-Path $PSScriptRoot '../src/main/resources/assets/poptartcore/blockstates/tree_tap.json'
$woods = @('maple', 'spruce', 'pine', 'birch', 'acacia', 'jungle')
$rotation = [ordered]@{ north = 0; east = 90; south = 180; west = 270 }
$variants = [ordered]@{}
foreach ($wood in $woods) {
    foreach ($facing in $rotation.Keys) {
        foreach ($bucket in @('false', 'true')) {
            foreach ($full in @('false', 'true')) {
                foreach ($level in 0..5) {
                    foreach ($dripping in @('false', 'true')) {
                        $model = if ($bucket -eq 'false') {
                            'tree_tap'
                        } elseif ($full -eq 'true' -or $level -eq 5) {
                            "tree_tap_${wood}_full"
                        } else {
                            "tree_tap_${wood}_level_${level}_dripping"
                        }
                        $variants["dripping=$dripping,facing=$facing,fill_level=$level,full=$full,has_bucket=$bucket,wood=$wood"] = [ordered]@{
                            model = "poptartcore:block/tree_tap/$model"
                            y = $rotation[$facing]
                        }
                    }
                }
            }
        }
    }
}
$blockstate = [ordered]@{ variants = $variants } | ConvertTo-Json -Depth 8
[System.IO.File]::WriteAllText($target, $blockstate + "`n")
