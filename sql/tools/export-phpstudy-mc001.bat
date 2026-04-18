@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM 在 Windows + phpStudy 环境下将 mc001 导出到 sql\dumps（与 db.sh import 配合）。
REM 本 bat 位于 \sql\tools；phpStudy 默认路径为仓库 old\phpStudy。
REM 用法: cmd 中执行  \sql\tools\export-phpstudy-mc001.bat

set "PHPMYSQL_BIN=%~dp0..\..\..\old\phpStudy\MySQL\bin"
set "MYSQL_HOST=127.0.0.1"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
REM 见 old\phpStudy\SQL-Front\Accounts\Accounts.xml；若改过 root 密码请修改下一行。
set "MYSQL_PASSWORD=root"

set "OUT_DIR=%~dp0..\dumps"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set dt=%%I
set "STAMP=%dt:~0,8%_%dt:~8,6%"
set "OUT_FILE=%OUT_DIR%\mc001_phpstudy_%STAMP%.sql"

set "MYSQLDUMP=%PHPMYSQL_BIN%\mysqldump.exe"
if not exist "%MYSQLDUMP%" (
  echo [错误] 未找到 mysqldump: %MYSQLDUMP%
  echo 请编辑 PHPMYSQL_BIN 为你的 phpStudy\MySQL\bin 路径。
  exit /b 1
)

echo 导出库 mc001 到:
echo %OUT_FILE%
echo.

"%MYSQLDUMP%" -h"%MYSQL_HOST%" -P"%MYSQL_PORT%" -u"%MYSQL_USER%" -p"%MYSQL_PASSWORD%" ^
  --single-transaction ^
  --routines ^
  --triggers ^
  --databases mc001 ^
  --result-file="%OUT_FILE%"

if errorlevel 1 (
  echo.
  echo [失败] 请确认 phpStudy MySQL 已启动且库名为 mc001。
  exit /b 1
)

echo.
echo [完成] 将上述文件拷到服务器后，在服务器目录执行:
echo   ./db.sh import sql/dumps\（文件名）
echo %OUT_FILE%
endlocal
exit /b 0
