param(
    [Parameter(Mandatory = $true)]
    [string]$KeystorePath
)

if (-not (Test-Path $KeystorePath)) {
    Write-Error "Datei nicht gefunden: $KeystorePath"
    exit 1
}

[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path $KeystorePath)))
