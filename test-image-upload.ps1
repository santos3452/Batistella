# Test Image Upload and URL Generation
Write-Host "Testing Image Upload and URL Generation..." -ForegroundColor Cyan

# Test 1: Create a simple test image
Write-Host "`n1. Creating test image..." -ForegroundColor Yellow

$testImagePath = "test-image.jpg"
# Create a simple 1x1 pixel JPEG for testing
$jpegBytes = [System.Convert]::FromBase64String("/9j/4AAQSkZJRgABAQEAYABgAAD//gA7Q1JFQVRPUjogZ2QtanBlZyB2MS4wICh1c2luZyBJSkcgSlBFRyB2ODApLCBxdWFsaXR5ID0gOTAK/9sAQwADAgIDAgIDAwMDBAMDBAUIBQUEBAUKBwcGCAwKDAwLCgsLDQ4SEA0OEQ4LCxAWEBETFBUVFQwPFxgWFBgSFBUU/9sAQwEDBAQFBAUJBQUJFA0LDRQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQU/8AAEQgAAQABAwEiAAIRAQMRAf/EAB8AAAEFAQEBAQEBAAAAAAAAAAABAgMEBQYHCAkKC//EALUQAAIBAwMEAQMDAgIDAwMEAQIDBBEFEiExQQYHE1FhInEUMoGRoQgjQrHB8DPlgtJD4uPi/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBQQGAQMGAgMHAAERAhIDBAEhBQRAEiIxQUFhByIycRNUgZGhccHRo/BywuEjKjKC8FPS4rTw/9oADAMBAAIRAxEAPwD9/KKKKAP/2Q==")
[System.IO.File]::WriteAllBytes($testImagePath, $jpegBytes)
Write-Host "Test image created: $testImagePath" -ForegroundColor Green

# Test 2: Prepare product data
Write-Host "`n2. Preparing product data..." -ForegroundColor Yellow

$productData = @{
    marca = "TESTMARCA"
    tipoAlimento = "TESTALIMENTO"
    tipoRaza = "TESTRAZA"
    description = "Test product for image upload"
    kg = 1.0
    priceMinorista = 100.0
    priceMayorista = 80.0
    stock = 10
    animalType = "PERROS"
    activo = $true
} | ConvertTo-Json

Write-Host "Product data prepared" -ForegroundColor Green

# Test 3: Upload product with image
Write-Host "`n3. Uploading product with image..." -ForegroundColor Yellow

try {
    # Create form data
    $boundary = [System.Guid]::NewGuid().ToString()
    $LF = "`r`n"
    
    $bodyLines = (
        "--$boundary",
        "Content-Disposition: form-data; name=`"product`"",
        "Content-Type: application/json$LF",
        $productData,
        "--$boundary",
        "Content-Disposition: form-data; name=`"image`"; filename=`"test-image.jpg`"",
        "Content-Type: image/jpeg$LF",
        [System.Text.Encoding]::UTF8.GetString([System.IO.File]::ReadAllBytes($testImagePath)),
        "--$boundary--$LF"
    ) -join $LF
    
    $headers = @{
        "Content-Type" = "multipart/form-data; boundary=$boundary"
    }
    
    $response = Invoke-RestMethod -Uri "http://localhost/api/products/saveProductWithImage" -Method Post -Body $bodyLines -Headers $headers -TimeoutSec 30
    
    Write-Host "Product created successfully!" -ForegroundColor Green
    Write-Host "Product ID: $($response.id)" -ForegroundColor Cyan
    Write-Host "Image URL: $($response.imageUrl)" -ForegroundColor Cyan
    
    # Test 4: Verify image URL
    Write-Host "`n4. Testing generated image URL..." -ForegroundColor Yellow
    
    if ($response.imageUrl) {
        $imageUrl = $response.imageUrl -replace "https://www.batistellaycia.shop", "http://localhost"
        Write-Host "Testing: $imageUrl" -ForegroundColor Gray
        
        try {
            $imageResponse = Invoke-WebRequest -Uri $imageUrl -Method Get -TimeoutSec 15
            Write-Host "✅ Image accessible! Status: $($imageResponse.StatusCode), Size: $($imageResponse.Content.Length) bytes" -ForegroundColor Green
        } catch {
            Write-Host "❌ Image not accessible: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ No image URL returned from backend" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Upload failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Cleanup
Write-Host "`n5. Cleanup..." -ForegroundColor Yellow
if (Test-Path $testImagePath) {
    Remove-Item $testImagePath -Force
    Write-Host "Test image deleted" -ForegroundColor Gray
}

Write-Host "`nTest completed!" -ForegroundColor Cyan 