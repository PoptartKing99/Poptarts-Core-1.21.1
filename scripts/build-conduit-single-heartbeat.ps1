param(
    [Parameter(Mandatory = $true)][string]$Ffmpeg,
    [Parameter(Mandatory = $true)][string]$SourceAudio
)
$soundDirectory = Join-Path $PSScriptRoot '../src/main/resources/assets/poptartcore/sounds/conduit'
$clipDurationSeconds = 0.8
$fadeInSeconds = 0.025
$fadeOutStartSeconds = 0.55
$fadeOutSeconds = 0.25
$audioFilter = 'highpass=f=35,lowpass=f=180:p=2,lowpass=f=180:p=2,' +
    "afade=t=in:st=0:d=$fadeInSeconds,afade=t=out:st=${fadeOutStartSeconds}:d=$fadeOutSeconds"
[System.IO.Directory]::CreateDirectory($soundDirectory) | Out-Null
& $Ffmpeg -hide_banner -loglevel error -i $SourceAudio -t $clipDurationSeconds `
    -af $audioFilter -ac 1 -c:a libvorbis -q:a 6 `
    -y (Join-Path $soundDirectory 'heartbeat_single.ogg')
if ($LASTEXITCODE -ne 0) { throw 'Heartbeat audio generation failed.' }
