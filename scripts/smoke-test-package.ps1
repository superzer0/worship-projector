param(
    [Parameter(Mandatory = $true)]
    [string]$Archive
)

$ErrorActionPreference = 'Stop'

$archivePath = (Resolve-Path -LiteralPath $Archive).Path
$smokeRoot = Join-Path $PWD 'target/release-smoke-windows'
$extractRoot = Join-Path $smokeRoot 'extract'
$userHome = Join-Path $smokeRoot 'home'
$appData = Join-Path $userHome 'jWorship'

Remove-Item $smokeRoot -Recurse -Force -ErrorAction SilentlyContinue
New-Item $extractRoot -ItemType Directory -Force | Out-Null
New-Item (Join-Path $appData 'pictures') -ItemType Directory -Force | Out-Null
New-Item (Join-Path $appData 'songs') -ItemType Directory -Force | Out-Null
New-Item (Join-Path $appData 'videos') -ItemType Directory -Force | Out-Null
New-Item (Join-Path $appData 'settings') -ItemType Directory -Force | Out-Null
New-Item (Join-Path $appData 'thumbnailCache') -ItemType Directory -Force | Out-Null

Expand-Archive -LiteralPath $archivePath -DestinationPath $extractRoot
$launcher = Join-Path $extractRoot 'jWorship/jWorship.exe'
$packagedJar = Join-Path $extractRoot 'jWorship/app/jWorship.jar'
if (-not (Test-Path -LiteralPath $launcher -PathType Leaf)) {
    throw "Packaged launcher was not found: $launcher"
}
if (-not (Test-Path -LiteralPath $packagedJar -PathType Leaf)) {
    throw "Packaged application JAR was not found: $packagedJar"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = [System.IO.Compression.ZipFile]::OpenRead($packagedJar)
try {
    $entry = $jar.GetEntry('sk/calvary/misc/lang.lng')
    if ($null -eq $entry) {
        throw 'Language resource is missing from the packaged application JAR'
    }
    $languageFile = Join-Path $appData 'settings/lang.lng'
    [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $languageFile, $true)
}
finally {
    $jar.Dispose()
}

$previousJavaToolOptions = $env:JAVA_TOOL_OPTIONS
$env:JAVA_TOOL_OPTIONS = "-Duser.home=$userHome"
$process = $null
try {
    $process = Start-Process -FilePath $launcher -ArgumentList '-testmode' -PassThru
    $ready = $false
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        Start-Sleep -Seconds 1
        $process.Refresh()
        if ($process.HasExited) {
            throw "Packaged application exited before its window appeared (exit $($process.ExitCode))"
        }
        if ($process.MainWindowHandle -ne 0 -and $process.MainWindowTitle -like 'jWorship *') {
            $ready = $true
            break
        }
    }
    if (-not $ready) {
        throw 'Packaged application did not expose a visible jWorship window within 30 seconds'
    }

    for ($attempt = 0; $attempt -lt 15; $attempt++) {
        Start-Sleep -Seconds 1
        $process.Refresh()
        if ($process.HasExited) {
            throw "Packaged application exited during the observation window (exit $($process.ExitCode))"
        }
    }

    Write-Host 'Windows packaged application smoke test passed'
}
finally {
    if ($null -ne $process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
        $process.WaitForExit()
    }
    $env:JAVA_TOOL_OPTIONS = $previousJavaToolOptions
}
