# Legt auf dem Desktop die Verknuepfung "Caracalas Therme" mit dem Programmsymbol an.
# Aufruf ueber Verknuepfung.bat (Doppelklick).
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

# JAR + ICO für dein Projekt
$jar  = Join-Path $root 'dist\CaracalasTherme.jar'
$ico  = Join-Path $root 'caracalla.ico'

if (-not (Test-Path $jar)) { throw "dist\CaracalasTherme.jar fehlt - zuerst in NetBeans bauen (Clean and Build)." }
if (-not (Test-Path $ico)) { throw "caracalla.ico fehlt." }

# javaw.exe suchen
$javaw = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javaw.exe'))) {
    $javaw = Join-Path $env:JAVA_HOME 'bin\javaw.exe'
} else {
    $cmd = Get-Command javaw.exe -ErrorAction SilentlyContinue
    if ($cmd) { $javaw = $cmd.Source }
}
if (-not $javaw) { throw "javaw.exe nicht gefunden - JAVA_HOME setzen oder Java in den PATH aufnehmen." }

# Desktop-Verknüpfung
$desktop = [Environment]::GetFolderPath('Desktop')
$lnk = Join-Path $desktop 'Caracalas Therme.lnk'

$sh = New-Object -ComObject WScript.Shell
$s = $sh.CreateShortcut($lnk)
$s.TargetPath       = $javaw
$s.Arguments        = '-Xmx4g -jar "' + $jar + '"'
$s.WorkingDirectory = $root
$s.IconLocation     = $ico
$s.Description      = 'Caracalas Therme – Java Anwendung'
$s.Save()

Write-Host "Verknuepfung angelegt: $lnk"
