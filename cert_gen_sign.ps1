<#
.DESCRIPTION
Script designed for generating csr and certificate signing.
In the process config file will be generated if it doesn't exist

.PARAMETER cert_name
Specify the certificate name - files generated in the process will be named as cert_name.

.PARAMETER cert_file
Specifies certificates other than issuing certificate to be included in the chain of output pfx file

.PARAMETER keyPassout
Specify the password for private key encryption. If not provided, prompts for input.

.PARAMETER CAcert
Specify the path to the issuing certificate. If not provided, prompts input.

.PARAMETER CAkey
Specify the path to the issuing certificate private key. If not provided, prompts for input.

.PARAMETER keyPassin
Specify the password used for CAKey encryption.

.PARAMETER pfxPassout
Password for output pfx, if not specified pass parameter will be used instead

.PARAMETER days
Validity period for issued certificate

.PARAMETER CA
Switch for whether config will be set to expect values for intermediate certificate or end entity

.PARAMETER clean
Switch for whether other temporary files with the exception of packaged pfx will be deleted after process completes

#>

param (
  [string]$cert_name = "default_cert_name",
  [string]$keyPassout = $( Read-Host "Password for private key encryption" ),
  [string]$CAcert = $( Read-Host "Path to issuing certifikate" ),
  [string]$CAkey = $( Read-Host "Path to issuing certificate private key "),
  [string]$keyPassin = $( Read-Host "$CAKey encryption password" ),
  [string[]]$cert_file,
  [string]$pfxPassout = $keyPassout,
  [int]$days = 365,
  [switch]$CA,
  [switch]$clean
)

$config_path="$cert_name.conf"
$config_path_csr="$cert_name.csr.conf"
$config_path_crt="$cert_name.crt.conf"
$key_path="$cert_name.key"
$crt_path="$cert_name.crt"
$csr_path="$cert_name.csr"
$pfx_path="$cert_name.pfx"
$temp_chain_file="$cert_name.tmp.chain.crt"

if (-not (Test-Path -Path $CAcert)) {
  Write-Host "Issuing certificate at $CAcert not found"
  exit -1
} 
if (-not (Test-Path -Path $CAkey)) {
  Write-Host "Issuing certificate key at $CAkey not found"
  exit -2
}

$was_config_generated = $false
if (-not (Test-Path -Path $config_path)) {
  $was_config_generated = $true
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
organizationName    = CA S.p.A
commonName          = Ca Intermediate V1

[v3_req]
$(If ($CA -eq $true) 
{ 
"basicConstraints       = critical,CA:TRUE
extendedKeyUsage       = emailProtection,clientAuth
keyUsage               = critical,cRLSign,keyCertSign
subjectKeyIdentifier   = hash
authorityKeyIdentifier = keyid, issuer
"
} Else 
{
"basicConstraints       = critical,CA:FALSE
extendedKeyUsage       = emailProtection,clientAuth
keyUsage               = critical,digitalSignature,keyEncipherment
subjectAltName         = email:@gmail.com
authorityInfoAccess    = caIssuers;URI:http://localhost:8080/certs/ca_intermediate,OCSP;URI:http://localhost:8090/ocsp
subjectKeyIdentifier   = hash
authorityKeyIdentifier = keyid, issuer
"
})
"@
  Set-Content -Path $config_path -Value $template
  $confirmation = Read-Host "Continue? ('yes' to continue)"
  if (-not ($confirmation -ieq "y" -or $confirmation -ieq "ye" -or $confirmation -ieq "yes")) {
    exit 0
  }
}

Get-Content $CAcert | Add-Content -path $temp_chain_file
foreach ($cert in $cert_file) {
  Get-Content $cert | Add-Content -Path $temp_chain_file
}

$conf_content = Get-Content -Path $config_path 
$v3_req = $conf_content | Select-String -Pattern "\[[ ]?v3_req[ ]?\]"
Set-Content -Path $config_path_csr -Value $conf_content[0..($v3_req.LineNumber - 1)]
Set-Content -Path $config_path_crt -Value $conf_content[($v3_req.LineNumber - 1)..($conf_content.Length)]

Write-Host "Generating $key_path..."
openssl genrsa -aes256 -out $key_path -passout pass:$keyPassout 2048
Write-Host "Generating $csr_path..."
openssl req -new -passin pass:$keyPassout -key $key_path -out $csr_path -config $config_path_csr
$serial = -join ((48..57) * 20 | Get-Random -Count 30 | ForEach-Object {[char]$_})
Write-Host "Signing $csr_path, generating $crt_path"
openssl x509 -req -days $days -in $csr_path -CA $CAcert -CAkey $CAKey -passin pass:$keyPassin -out $crt_path -set_serial $serial -sha256 -trustout -extensions v3_req -extfile $config_path_crt
Write-Host "Exporting $pfx_path"
openssl pkcs12 -export -in $crt_path -inkey $key_path -passin pass:$keyPassout -out $pfx_path -passout pass:$pfxPassout -certfile $temp_chain_file
Write-Host "Certificate $crt_path generated"
openssl x509 -in $crt_path -noout -text

Remove-Item $config_path_crt
Remove-Item $config_path_csr
Remove-Item $temp_chain_file
If ($clean) {
  Remove-Item $csr_path
  if ($was_config_generated -eq $true) {
    Remove-Item $config_path
  }
}
