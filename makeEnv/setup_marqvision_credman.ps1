#requires -Version 5.1
Write-Host "== MarqVision credentials → Windows Credential Manager =="

# 모듈 준비
if (-not (Get-Module -ListAvailable -Name CredentialManager)) {
  try {
    Install-Module -Name CredentialManager -Scope CurrentUser -Force -ErrorAction Stop
  } catch {
    Write-Error "Install-Module CredentialManager failed. Run PowerShell as Admin or set PSGallery trust."
    exit 1
  }
}

$service = 'marqvision.login'
$account = 'marqvision'

# 입력
$email = Read-Host 'MARQVISION_EMAIL'
$secPass = Read-Host 'MARQVISION_PASSWORD' -AsSecureString
$plainPass = [Runtime.InteropServices.Marshal]::PtrToStringBSTR(
  [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secPass)
)

# JSON으로 묶어서 저장 (Password 필드에 JSON 저장)
$json = @{ email = $email; password = $plainPass } | ConvertTo-Json -Compress
$secJson = ConvertTo-SecureString $json -AsPlainText -Force

# 있으면 덮어쓰기
$existing = Get-StoredCredential -Target $service -ErrorAction SilentlyContinue
if ($existing) { Remove-StoredCredential -Target $service | Out-Null }

New-StoredCredential -Target $service -UserName $account -Password $secJson -Persist LocalMachine | Out-Null
Write-Host "Saved to Credential Manager: Target=$service, UserName=$account"

# 간단 검증(이메일만 출력, 비밀번호는 마스킹)
$readJson = (Get-StoredCredential -Target $service).GetNetworkCredential().Password
try {
  $obj = $readJson | ConvertFrom-Json
  Write-Host ("Verified email: " + $obj.email)
} catch {
  Write-Warning "Verification failed (JSON parse)."
}
