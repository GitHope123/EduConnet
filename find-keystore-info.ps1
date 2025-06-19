Write-Host "=== Buscando información del keystore ===" -ForegroundColor Green
Write-Host ""

# Lista de contraseñas comunes para probar
$passwords = @(
    "android",
    "123456",
    "password",
    "keystore",
    "release",
    "key0",
    "educonnet",
    "cerroalegre",
    "a24322843",
    "admin",
    "12345678",
    "qwerty"
)

$keystorePath = "my-release-key.jks"
$found = $false

foreach ($password in $passwords) {
    Write-Host "Probando contraseña: '$password'" -ForegroundColor Yellow
    
    # Crear un archivo temporal con la contraseña
    $tempFile = "temp_password.txt"
    $password | Out-File -FilePath $tempFile -Encoding ASCII
    
    try {
        # Intentar listar el keystore con esta contraseña
        $result = keytool -list -v -keystore $keystorePath -storepass $password 2>$null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "¡ÉXITO! Contraseña encontrada: '$password'" -ForegroundColor Green
            Write-Host ""
            Write-Host "Información del keystore:" -ForegroundColor Cyan
            Write-Host $result
            $found = $true
            break
        }
    }
    catch {
        # Continuar con la siguiente contraseña
    }
    finally {
        # Limpiar archivo temporal
        if (Test-Path $tempFile) {
            Remove-Item $tempFile
        }
    }
}

if (-not $found) {
    Write-Host ""
    Write-Host "No se pudo encontrar la contraseña automáticamente." -ForegroundColor Red
    Write-Host "Necesitas recordar o encontrar la contraseña del keystore." -ForegroundColor Red
    Write-Host ""
    Write-Host "Opciones:" -ForegroundColor Yellow
    Write-Host "1. Revisar documentación del proyecto" -ForegroundColor White
    Write-Host "2. Buscar en archivos de configuración" -ForegroundColor White
    Write-Host "3. Crear un nuevo keystore" -ForegroundColor White
} 