<#

.DESCRIPTION
    This script provides functionality related to generating self signed certificates.

.PARAMETER cert_name
    Specifies the name of the certificate. If not provided, a default name "default_cert_name" is used. 
    Temporary and resulting files will be named based on cert_name

.PARAMETER passout
    Specifies the password for private key encryption. If not provided, the script will prompt for it.

.PARAMETER days
    Specifies the number of days for which the certificate is valid. The default is 3650 days.

.PARAMETER clean
    If specified, this switch will trigger the cleanup of temporary files created during the certificate generation process.

#>

param (
  [string]$cert_name = "default_cert_name",
  [string]$passout = $( Read-Host "Password for private key encryption" ),
  [int]$days = 3650,
  [switch]$clean
)

$config_path="$cert_name.conf"
$key_path="$cert_name.key"
$crt_path="$cert_name.crt"

if (-not (Test-Path -Path $config_path)) {
  Write-Host "Expected configuration file $config_path, template will be created, adjust values and continue"
  $template=@"
[ req ]
prompt                 = no
distinguished_name     = req_distinguished_name
req_extensions         = v3_req
  
[ req_distinguished_name ]
countryName         = RS
stateOrProvinceName = Nis
localityName        = Nis
organizationName    = CA S.p.A./17975
commonName          = Root CA

[ v3_req ]
basicConstraints       = critical,CA:TRUE
extendedKeyUsage       = emailProtection,clientAuth,codeSigning,serverAuth,timeStamping
keyUsage               = critical,cRLSign,keyCertSign
subjectKeyIdentifier   = hash
authorityKeyIdentifier = keyid:always,issuer
"@
  Set-Content -Path $config_path -Value $template
  $confirmation = Read-Host "Continue? ('yes' to continue)"
  if (-not ($confirmation -ieq "y" -or $confirmation -ieq "ye" -or $confirmation -ieq "yes")) {
    exit 0
  }
}

Write-Host "Generating $key_path..."
openssl genrsa -aes256 -out $key_path -passout pass:$passout 
Write-Host "Generating $crt_path..."
openssl req -new -x509 -days $days -key $key_path -out $crt_path -passin pass:$passout -config $config_path -extensions v3_req
Write-Host "Certificate $crt_path generated"
openssl x509 -in $crt_path -text -noout

if ($clean) {
  Remove-Item $config_path
}