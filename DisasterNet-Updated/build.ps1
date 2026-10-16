$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

$sources = Get-ChildItem -LiteralPath 'src\main\java' -Filter '*.java' -Recurse
if ($sources.Count -eq 0) {
    throw 'No Java source files were found under src\main\java.'
}

New-Item -ItemType Directory -Path 'target\classes' -Force | Out-Null
& javac --release 17 -encoding UTF-8 -d 'target\classes' $sources.FullName
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Build succeeded. Classes are in $PSScriptRoot\target\classes"
