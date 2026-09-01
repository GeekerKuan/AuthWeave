param(
    [string[]]$Version = @(),
    [ValidateSet('all', 'fabric', 'neoforge')]
    [string]$Platform = 'all',
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$matrixPath = Join-Path $projectRoot 'versions\matrix.json'
$matrix = @(Get-Content -LiteralPath $matrixPath -Raw | ConvertFrom-Json)
$selected = if ($Version.Count -eq 0) {
    $matrix
} else {
    @($matrix | Where-Object { $_.minecraft -in $Version })
}

if ($selected.Count -ne ($Version | Select-Object -Unique).Count -and $Version.Count -gt 0) {
    $known = $matrix.minecraft -join ', '
    throw "At least one requested version is unknown. Supported versions: $known"
}

$distRoot = Join-Path $projectRoot 'dist'
New-Item -ItemType Directory -Path $distRoot -Force | Out-Null

foreach ($target in $selected) {
    $platforms = if ($Platform -eq 'all') { 'fabric,neoforge' } else { $Platform }
    $tasks = @('clean')
    if (-not $SkipTests) { $tasks += ':common:test' }
    if ($Platform -in @('all', 'fabric')) { $tasks += ':fabric:build' }
    if ($Platform -in @('all', 'neoforge')) { $tasks += ':neoforge:build' }

    Write-Host "Building Minecraft $($target.minecraft) ($Platform)"
    & (Join-Path $projectRoot 'gradlew.bat') @tasks `
        "-Ptarget_version=$($target.minecraft)" `
        "-Penabled_platforms=$platforms" `
        '--console=plain' '--stacktrace'
    if ($LASTEXITCODE -ne 0) { throw "Build failed for Minecraft $($target.minecraft)" }

    $targetDir = Join-Path $distRoot $target.minecraft
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    foreach ($loader in @('fabric', 'neoforge')) {
        if ($Platform -ne 'all' -and $Platform -ne $loader) { continue }
        $libDir = Join-Path $projectRoot "$loader\build\libs"
        Get-ChildItem -LiteralPath $libDir -Filter "server-login-profiles-$loader-*.jar" |
            Where-Object { $_.Name -notmatch '-dev(?:-shadow)?\.jar$' } |
            Copy-Item -Destination $targetDir -Force
    }
}

Write-Host "Artifacts are available in $distRoot"
