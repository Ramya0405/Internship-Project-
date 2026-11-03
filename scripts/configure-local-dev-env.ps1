$ErrorActionPreference = 'Stop'

function Read-RequiredValue([string]$Prompt) {
    do {
        $value = Read-Host $Prompt
    } while ([string]::IsNullOrWhiteSpace($value))
    return $value
}

function Read-RequiredSecret([string]$Prompt) {
    do {
        $secret = Read-Host $Prompt -AsSecureString
    } while ($secret.Length -eq 0)
    return $secret
}

function Set-UserEnvironmentSecret([string]$Name, [Security.SecureString]$Secret) {
    $pointer = [IntPtr]::Zero
    try {
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Secret)
        $value = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
        [Environment]::SetEnvironmentVariable($Name, $value, 'User')
    }
    finally {
        if ($pointer -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        }
        $value = $null
    }
}

$dbUsername = Read-RequiredValue 'MySQL username'
$dbPassword = Read-RequiredSecret 'MySQL password'
$adminEmail = Read-RequiredValue 'Initial administrator email'
try {
    $null = [System.Net.Mail.MailAddress]::new($adminEmail)
}
catch {
    $dbPassword.Dispose()
    throw 'The administrator email address is not valid.'
}
$adminPassword = Read-RequiredSecret 'Initial administrator password'

try {
    [Environment]::SetEnvironmentVariable('DB_USERNAME', $dbUsername, 'User')
    Set-UserEnvironmentSecret 'DB_PASSWORD' $dbPassword
    [Environment]::SetEnvironmentVariable('ADMIN_EMAIL', $adminEmail, 'User')
    Set-UserEnvironmentSecret 'ADMIN_PASSWORD' $adminPassword
}
finally {
    $dbUsername = $null
    $adminEmail = $null
    $dbPassword.Dispose()
    $adminPassword.Dispose()
}

Write-Output 'Configured DB_USERNAME, DB_PASSWORD, ADMIN_EMAIL, and ADMIN_PASSWORD for this Windows user.'
Write-Output 'Restart VS Code so the Java launcher inherits the variables. Values were not displayed.'
