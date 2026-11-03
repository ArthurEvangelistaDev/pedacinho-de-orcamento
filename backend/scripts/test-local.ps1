param([string] $ToolchainRoot)
$ErrorActionPreference = 'Stop'
$testProjectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if (-not $ToolchainRoot) { $ToolchainRoot = Join-Path $testProjectRoot 'work/toolchain' }
$testJavaRoot = (Get-ChildItem (Join-Path $ToolchainRoot 'jdk25') -Directory | Select-Object -First 1).FullName
$testMysqlRoot = (Get-ChildItem (Join-Path $ToolchainRoot 'mysql') -Directory | Select-Object -First 1).FullName
if (-not $testJavaRoot -or -not $testMysqlRoot) { throw 'Portable JDK/MySQL missing; see backend/README.md.' }
$testStateRoot = Join-Path $testProjectRoot 'work/mysql-insumos-test'
New-Item -ItemType Directory -Force -Path $testStateRoot | Out-Null
$testDataRoot = Join-Path $testStateRoot 'data'
$testSecretPath = Join-Path $testStateRoot 'password.txt'
if (-not (Test-Path -LiteralPath $testSecretPath)) { [IO.File]::WriteAllText($testSecretPath, [guid]::NewGuid().ToString('N')) }
$testPassword = [IO.File]::ReadAllText($testSecretPath).Trim()
$testServerConfig = Join-Path $testStateRoot 'server.cnf'
$testClientConfig = Join-Path $testStateRoot 'client.cnf'
$testMysqlPath = $testMysqlRoot.Replace('\','/')
$testDataPath = $testDataRoot.Replace('\','/')
[IO.File]::WriteAllText($testServerConfig, "[mysqld]`nbasedir=$testMysqlPath`ndatadir=$testDataPath`nbind-address=127.0.0.1`nport=33306`nmysqlx=OFF`ncharacter-set-server=utf8mb4`ncollation-server=utf8mb4_0900_ai_ci`n")
[IO.File]::WriteAllText($testClientConfig, "[client]`nhost=127.0.0.1`nport=33306`nuser=root`npassword=$testPassword`nprotocol=tcp`n")
$testPortProbe = [Net.Sockets.TcpClient]::new()
try { $testPortProbe.Connect('127.0.0.1',33306); throw 'Port 33306 already in use; refusing to touch another server.' }
catch [Net.Sockets.SocketException] { }
finally { $testPortProbe.Dispose() }
if (-not (Test-Path -LiteralPath $testDataRoot)) {
    $testInit = Start-Process -FilePath (Join-Path $testMysqlRoot 'bin/mysqld.exe') -ArgumentList @('--no-defaults','--initialize-insecure',"--basedir=`"$testMysqlRoot`"","--datadir=`"$testDataRoot`"") -WindowStyle Hidden -Wait -PassThru -RedirectStandardOutput (Join-Path $testStateRoot 'init.out.log') -RedirectStandardError (Join-Path $testStateRoot 'init.err.log')
    if ($testInit.ExitCode -ne 0) { throw 'Test MySQL initialization failed; inspect work/mysql-insumos-test/init.err.log.' }
}
$testPreviousJava = $env:JAVA_HOME
$testPreviousMavenHome = $env:MAVEN_USER_HOME
$testPreviousUrl = $env:APP_DB_URL
$testPreviousUser = $env:APP_DB_USER
$testPreviousPassword = $env:APP_DB_PASSWORD
$testProcess = $null
$testExit = 1
try {
    $testProcess = Start-Process -FilePath (Join-Path $testMysqlRoot 'bin/mysqld.exe') -ArgumentList "--defaults-file=`"$testServerConfig`"" -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $testStateRoot 'server.out.log') -RedirectStandardError (Join-Path $testStateRoot 'server.err.log')
    $testReady = $false
    foreach ($testAttempt in 1..120) {
        if ($testProcess.HasExited) { throw 'Test MySQL stopped; inspect work/mysql-insumos-test/server.err.log.' }
        $testProbe = [Net.Sockets.TcpClient]::new()
        try { $testProbe.Connect('127.0.0.1',33306); $testReady=$true; break }
        catch [Net.Sockets.SocketException] { Start-Sleep -Milliseconds 250 }
        finally { $testProbe.Dispose() }
    }
    if (-not $testReady) { throw 'Test MySQL did not become ready.' }
    $testMarker = Join-Path $testStateRoot 'configured'
    if (-not (Test-Path -LiteralPath $testMarker)) {
        $testSetupSql = "ALTER USER 'root'@'localhost' IDENTIFIED BY '$testPassword'; CREATE DATABASE IF NOT EXISTS pedacinho_insumos_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
        $testNativePreference = $ErrorActionPreference
        try {
            $ErrorActionPreference = 'Continue'
            $testSetupSql | & (Join-Path $testMysqlRoot 'bin/mysql.exe') --no-defaults --protocol=tcp --host=127.0.0.1 --port=33306 --user=root --skip-password
            $testSetupExit = $LASTEXITCODE
        } finally { $ErrorActionPreference = $testNativePreference }
        if ($testSetupExit -ne 0) { throw 'Isolated test database setup failed.' }
        [IO.File]::WriteAllText($testMarker,'ready')
    }
    $env:JAVA_HOME = $testJavaRoot
    $env:MAVEN_USER_HOME = Join-Path $ToolchainRoot 'maven-user'
    $env:APP_DB_URL = 'jdbc:mysql://127.0.0.1:33306/pedacinho_insumos_test?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true'
    $env:APP_DB_USER = 'root'
    $env:APP_DB_PASSWORD = $testPassword
    Push-Location (Join-Path $testProjectRoot 'backend')
    try {
        # Windows PowerShell turns redirected native stderr warnings into ErrorRecords.
        # Preserve the output and use Maven's exit code as the test/build gate.
        $ErrorActionPreference = 'Continue'
        & (Join-Path $testProjectRoot 'backend/mvnw.cmd') --batch-mode --no-transfer-progress "-Dmaven.repo.local=$(Join-Path $ToolchainRoot 'm2')" -Pmysql-integration verify
        $testExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = 'Stop'; Pop-Location }
} finally {
    if ($testProcess -and -not $testProcess.HasExited) {
        $ErrorActionPreference = 'Continue'
        & (Join-Path $testMysqlRoot 'bin/mysqladmin.exe') "--defaults-extra-file=$testClientConfig" shutdown 2>$null | Out-Null
        if (-not $testProcess.WaitForExit(10000)) { Stop-Process -Id $testProcess.Id -Force }
        $ErrorActionPreference = 'Stop'
    }
    $env:JAVA_HOME = $testPreviousJava
    $env:MAVEN_USER_HOME = $testPreviousMavenHome
    $env:APP_DB_URL = $testPreviousUrl
    $env:APP_DB_USER = $testPreviousUser
    $env:APP_DB_PASSWORD = $testPreviousPassword
}
exit $testExit
