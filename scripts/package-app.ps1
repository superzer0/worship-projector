param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9]+\.[0-9]+\.[0-9]+$')]
    [string]$Version,

    [ValidateSet('x64')]
    [string]$Architecture = 'x64'
)

$ErrorActionPreference = 'Stop'

if ([int64]($Version.Split('.')[0]) -lt 1) {
    throw "Version major component must be positive for cross-platform packaging: $Version"
}

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw 'jpackage from JDK 21 is required'
}

$jarFile = 'target/worship-1.0-SNAPSHOT.jar'
$dependencyDir = 'target/dependency'
if (-not (Test-Path $jarFile -PathType Leaf) -or -not (Test-Path $dependencyDir -PathType Container)) {
    throw 'Run Maven package and dependency:copy-dependencies before packaging'
}

$releaseRoot = 'target/release'
$inputDir = Join-Path $releaseRoot 'input'
$appDir = Join-Path $releaseRoot 'app'
$distDir = 'dist'
$artifactName = "jWorship-$Version-windows-$Architecture"

Remove-Item $releaseRoot -Recurse -Force -ErrorAction SilentlyContinue
New-Item (Join-Path $inputDir 'lib') -ItemType Directory -Force | Out-Null
New-Item $appDir -ItemType Directory -Force | Out-Null
New-Item $distDir -ItemType Directory -Force | Out-Null
Copy-Item $jarFile (Join-Path $inputDir 'jWorship.jar')
Copy-Item (Join-Path $dependencyDir '*.jar') (Join-Path $inputDir 'lib')

& jpackage `
    --type app-image `
    --name jWorship `
    --app-version $Version `
    --vendor 'jWorship contributors' `
    --description 'Worship lyrics projection application' `
    --input $inputDir `
    --dest $appDir `
    --main-jar jWorship.jar `
    --main-class sk.calvary.worship.App `
    --add-modules ALL-MODULE-PATH `
    --java-options '-Dfile.encoding=UTF-8'

if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

$archive = Join-Path $distDir "$artifactName.zip"
Remove-Item $archive -Force -ErrorAction SilentlyContinue
Compress-Archive -Path (Join-Path $appDir 'jWorship') -DestinationPath $archive -CompressionLevel Optimal
$hash = (Get-FileHash $archive -Algorithm SHA256).Hash.ToLowerInvariant()
$checksumPath = "$archive.sha256"
Set-Content -Path $checksumPath -Value "$hash  $(Split-Path $archive -Leaf)" -Encoding ascii
Write-Output "Created $archive"
