; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
AppName=Variman RETS Server
AppVerName=Variman RETS Server @VERSION@
AppPublisher=Center for REALTOR Technology
AppPublisherURL=http://www.crt.realors.org/
AppSupportURL=http://www.crt.realors.org/
AppUpdatesURL=http://www.crt.realors.org/
SourceDir=@BASEDIR@\build\rets-server
OutputDir=@BASEDIR@\dist
OutputBaseFilename=variman-@VERSION@-setup
DefaultDirName={pf}\Variman RETS Server
DefaultGroupName=Variman RETS Server
InfoBeforeFile=@BASEDIR@\project\build\iss_java.txt
LicenseFile=@BASEDIR@\LICENSE.TXT
UninstallFilesDir={app}\uninstall
MinVersion=0,4
Compression=@COMPRESSION@

[Files]
Source: "*"; Excludes: "\webapp\WEB-INF\rets\rets-config.xml,\webapp\WEB-INF\rets\rets-logging.properties,\logs\%"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs; Check: StopVarimanService
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Dirs]
Name: "{app}\logs"

[Icons]
;Name: "{group}\CRT Variman RETS Server"; Filename: "{app}\MyProg.exe"
; NOTE: The following entry contains an English phrase ("Uninstall"). You are free to translate it into another language if required.
Name: "{group}\Uninstall CRT Variman RETS Server"; Filename: "{uninstallexe}"
Name: "{group}\View README"; Filename: "{app}\doc\readme.txt"
Name: "{group}\View Manual"; Filename: "{app}\doc\manual\index.html"
Name: "{group}\Variman Administration"; Filename: "javaw.exe"; Parameters: "-jar variman-admin.jar"; WorkingDir: "{app}";

[UninstallDelete]
Type: filesandordirs; Name: "{app}\server\work"
Type: filesandordirs; Name: "{app}\server\logs"
Type: filesandordirs; Name: "{app}\logs"

[Run]
; NOTE: The following entry contains an English phrase ("Launch"). You are free to translate it into another language if required.
Filename: "{app}\variman.exe"; Parameters: "{code:InstallServiceParams}"; Description: "Install service"; Flags: runhidden; StatusMsg: "Installing Variman Service..."
Filename: "{app}\doc\readme.txt"; Description: "View the README file"; Flags: shellexec postinstall skipifsilent

[UninstallRun]
Filename: "net"; Parameters: "stop Variman"; Flags: runhidden
Filename: "{app}\variman.exe"; Parameters: "{code:UninstallServiceParams}"; Flags: runhidden

[Code]
var
  JavaFound: Boolean;
  JavaVersion: String;
  JavaMajorVersion: Integer;
  JavaMinorVersion: Integer;
  JavaHome: String;
  JavaJvmDll: String;
  ServiceName: String;
  ServiceStopped: Boolean;
  
function GetJavaHome(Default: String): String;
begin
  Result:= JavaHome;
end;

procedure InitializeJava();
var
  JreKey: String;
  JreVersionKey: String;
  MajorVersion: String;
  MinorVersion: String;
begin
  JavaFound := False;
  JreKey := 'SOFTWARE\JavaSoft\Java Runtime Environment';
  if not RegQueryStringValue(HKLM, JreKey, 'CurrentVersion', JavaVersion) then
    Exit;
  //JavaVersion := '1.3';
  
  JreVersionKey := JreKey + '\' + JavaVersion;
  if not RegQueryStringValue(HKLM, JreVersionKey, 'JavaHome', JavaHome) then
    Exit;
  if not RegQueryStringValue(HKLM, JreVersionKey, 'RuntimeLib', JavaJvmDll) then
    Exit;

  JavaFound := True;
  // JavaVersion is of the format "major.minor"
  MajorVersion := Copy(JavaVersion, 1, 1);
  JavaMajorVersion := StrToInt(MajorVersion);
  MinorVersion := Copy(JavaVersion, 3, 1);
  JavaMinorVersion := StrToInt(MinorVersion);

//  MsgBox('JavaVersion: ' + JavaVersion + #13
//          'JavaHome: ' + JavaHome + #13
//          'JavaJvmDll: ' + JavaJvmDll, mbInformation, MB_OK);
end;

function InitializeSetup(): Boolean;
begin
  InitializeJava();
  ServiceName := 'Variman';
  ServiceStopped := False;
  Result := True;
end;

function NextButtonClick(CurPage: Integer): Boolean;
begin
  Result := True;
  if CurPage = wpInfoBefore then begin
    if not JavaFound then begin
      MsgBox('Java was not found.' #13#13 'Cancel the installation and install Java.',
        mbCriticalError, MB_OK);
      Result := False;
      Exit;
    end
    if JavaMinorVersion < 4 then begin
      MsgBox('Java version 1.4 is required, detected: ' + JavaVersion + #13#13
        'Cancel the installation and upgrade Java.', mbCriticalError, MB_OK);
      Result := False;
      Exit;
    end;
  end
end;

function InstallServiceParams(Default: String): String;
begin
  Result := '-install "' + ServiceName + '" "' + JavaJvmDll +
    '" "-Djava.class.path={app}\variman.jar" ' +
    '-Xmx512m ' +
    '-Dcom.sun.management.jmxremote ' +
    '-start org.realtors.rets.server.tomcat.EmbeddedTomcat ' +
    '-params start ' +
    '-out "{app}\logs\stdout.log" -err "{app}\logs\stderr.log" ' +
    '-current "{app}"';
  Result := ExpandConstant(Result);
//  MsgBox('Install service: ' + Result, mbInformation, MB_OK);
end;

function UninstallServiceParams(Default: String): String;
begin
  Result := '-uninstall "' + ServiceName + '"';
//  MsgBox('Uninstall service: ' + Result, mbInformation, MB_OK);
end;

function StopVarimanService() : Boolean;
var rslt : integer;
begin
  if not ServiceStopped then begin
//    MsgBox('Removing Variman Service', mbInformation, MB_OK);
    Exec(GetSystemDir() + '\net.exe', 'stop Variman', GetSystemDir(), SW_HIDE, ewWaitUntilTerminated, rslt);
    ServiceStopped := True;
  end
  Result := True;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var lib: String;
var classes: String;
begin
  if CurStep = ssInstall then begin
    lib := ExpandConstant('{app}') + '\webapp\WEB-INF\lib'
    classes := ExpandConstant('{app}') + '\webapp\WEB-INF\classes'
    DelTree(lib, True, True, True);
    DelTree(classes, True, True, True);
  end;
end;