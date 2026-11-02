# Publishes a new fokus.xyz version. Every installed copy finds it on its next start, downloads it
# in the background and switches to it when Minecraft closes.
#
#   1. Raise mod_version in gradle.properties (e.g. 1.0.0 -> 1.0.1).
#   2. Run:  powershell -ExecutionPolicy Bypass -File release.ps1 -Notes "What changed"
#
# It builds the mod, writes dist\<version>\ (the jar, update.json and the zip to send around),
# and creates the GitHub release on irisclint/fokus.xyz (the repository is created the first time).
param([string]$Notes = "", [switch]$NoPublish)
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$repo = 'irisclint/fokus.xyz'

if (-not $env:JAVA_HOME) {
    # The Java 25 that ships with the Minecraft launcher is enough to build.
    $java = Get-ChildItem "$env:LOCALAPPDATA\Packages\Microsoft.4297127D64EC6_*\LocalCache\Local\runtime\java-runtime-epsilon\windows-x64\java-runtime-epsilon" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($java) { $env:JAVA_HOME = $java.FullName }
}

$version = (Select-String -Path "$root\gradle.properties" -Pattern '^mod_version=(.+)$').Matches[0].Groups[1].Value.Trim()
Write-Host "fokus.xyz $version" -ForegroundColor White

& "$root\gradlew.bat" build --console=plain
if ($LASTEXITCODE -ne 0) { throw "Build failed" }

$out = Join-Path $root "dist\$version"
New-Item -ItemType Directory -Force $out | Out-Null
$jar = Join-Path $out 'fokus.xyz.jar'
Copy-Item "$root\build\libs\fokus.xyz-$version.jar" $jar -Force
$sha = (Get-FileHash $jar -Algorithm SHA256).Hash.ToLower()

$manifest = [ordered]@{
    version = $version
    jar     = "https://github.com/$repo/releases/download/v$version/fokus.xyz.jar"
    sha256  = $sha
    notes   = $Notes
} | ConvertTo-Json
[IO.File]::WriteAllText((Join-Path $out 'update.json'), $manifest, (New-Object Text.UTF8Encoding($false)))

$zip = Join-Path $out "fokus.xyz-$version.zip"
$stage = Join-Path $out 'zip'
Remove-Item $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory $stage | Out-Null
Copy-Item $jar $stage
Copy-Item "$root\dist-files\*" $stage
Compress-Archive -Path "$stage\*" -DestinationPath $zip -Force
Remove-Item $stage -Recurse -Force
Write-Host "Built: $zip" -ForegroundColor Green

if ($NoPublish) { return }

# gh writes "not found" to stderr, which 'Stop' would turn into an error; only the exit code matters.
$ErrorActionPreference = 'Continue'
gh repo view $repo 2>&1 | Out-Null
$exists = $LASTEXITCODE -eq 0
$ErrorActionPreference = 'Stop'
if (-not $exists) {
    gh repo create $repo --public --add-readme --description 'fokus.xyz - PvP mod for Minecraft 26.1-26.3 (releases and updates)'
}
gh release create "v$version" $jar (Join-Path $out 'update.json') $zip --repo $repo --title "fokus.xyz $version" --notes $Notes
Write-Host "Published v$version - every installed copy updates itself on its next start." -ForegroundColor Green
