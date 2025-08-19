@echo off
setlocal EnableExtensions

echo === MarqVision env setup (Windows) ===

rem -- 이메일 입력 (평문)
for /f "usebackq delims=" %%E in (`powershell -NoProfile -Command "Read-Host 'MARQVISION_EMAIL'"`) do set "MARQ_EMAIL_TMP=%%E"

rem -- 비밀번호 입력 (마스킹)
for /f "usebackq delims=" %%P in (`powershell -NoProfile -Command "$p=Read-Host -AsSecureString 'MARQVISION_PASSWORD'; $b=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($p); [Runtime.RuntimeInformation]::IsOSPlatform([System.Runtime.InteropServices.OSPlatform]::Windows) > $null; [Runtime.InteropServices.Marshal]::PtrToStringBSTR($b)"`) do set "MARQ_PASS_TMP=%%P"

if not defined MARQ_EMAIL_TMP (
  echo [ERR] Empty MARQVISION_EMAIL. Aborting.
  exit /b 1
)
if not defined MARQ_PASS_TMP (
  echo [ERR] Empty MARQVISION_PASSWORD. Aborting.
  exit /b 1
)

rem -- 현재 세션에 즉시 적용
set "MARQVISION_EMAIL=%MARQ_EMAIL_TMP%"
set "MARQVISION_PASSWORD=%MARQ_PASS_TMP%"

rem -- 사용자(User) 환경 변수로 영구 저장 (PowerShell API 사용: 특수문자/%% 안전)
powershell -NoProfile -Command ^
  "[Environment]::SetEnvironmentVariable('MARQVISION_EMAIL', $env:MARQVISION_EMAIL, 'User');" ^
  " [Environment]::SetEnvironmentVariable('MARQVISION_PASSWORD', $env:MARQVISION_PASSWORD, 'User')"

echo.
echo [OK] Set for current session and persisted to User environment.
echo     Open a NEW Command Prompt to see persisted variables.
echo.
echo   MARQVISION_EMAIL=%MARQVISION_EMAIL%
echo   MARQVISION_PASSWORD=********
echo.
pause
