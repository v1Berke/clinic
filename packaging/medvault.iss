; MedVault kurulum betigi (Inno Setup 6)
#define MyAppName "MedVault"
#define MyAppVersion "1.4"
#define MyAppPublisher "MedVault"
#define MyAppExeName "MedVault.exe"

[Setup]
AppId={{425D1EDC-3438-46AE-841E-C04710DD9B2B}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
PrivilegesRequired=lowest
OutputDir=.
OutputBaseFilename=MedVault-Setup-{#MyAppVersion}
SetupIconFile=..\src\main\resources\ui\app.ico
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
DisableProgramGroupPage=yes
UninstallDisplayIcon={app}\{#MyAppExeName}

[Languages]
Name: "turkish"; MessagesFile: "compiler:Languages\Turkish.isl"

[Tasks]
Name: "desktopicon"; Description: "Masaustu kisayolu olustur"; GroupDescription: "Ek kisayollar:"

[Files]
Source: "MedVault\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "MedVault'u baslat"; Flags: nowait postinstall skipifsilent
