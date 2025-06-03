# Test Images Gateway Configuration
Write-Host "Testing Images Gateway Configuration..." -ForegroundColor Cyan

# Test 1: Gateway health
Write-Host "`n1. Testing Gateway Health..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost/health" -Method Get -TimeoutSec 10
    Write-Host "Gateway Health: $healthResponse" -ForegroundColor Green
} catch {
    Write-Host "Gateway Health Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Products API
Write-Host "`n2. Testing Products API..." -ForegroundColor Yellow
try {
    $productsResponse = Invoke-RestMethod -Uri "http://localhost/api/products/getAllProducts" -Method Get -TimeoutSec 10
    $productCount = $productsResponse.Count
    Write-Host "Products API: Found $productCount products" -ForegroundColor Green
    
    # Check if any product has an image URL
    $productsWithImages = $productsResponse | Where-Object { $_.imageUrl }
    Write-Host "Products with images: $($productsWithImages.Count)" -ForegroundColor Cyan
    
    # Show first product with image URL
    if ($productsWithImages.Count -gt 0) {
        $firstProduct = $productsWithImages[0]
        Write-Host "First product image URL: $($firstProduct.imageUrl)" -ForegroundColor Cyan
        
        # Test image access
        Write-Host "`n3. Testing Image Access..." -ForegroundColor Yellow
        try {
            $imageUrl = $firstProduct.imageUrl -replace "https://www.batistellaycia.shop", "http://localhost"
            Write-Host "Testing image URL: $imageUrl" -ForegroundColor Gray
            
            $imageResponse = Invoke-WebRequest -Uri $imageUrl -Method Get -TimeoutSec 15
            Write-Host "Image Access: Status $($imageResponse.StatusCode), Size: $($imageResponse.Content.Length) bytes" -ForegroundColor Green
        } catch {
            Write-Host "Image Access Failed: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "No products with images found in database" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "Products API Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nSummary:" -ForegroundColor Cyan
Write-Host "- If tests pass, try creating a new product with an image" -ForegroundColor White
Write-Host "- Images should now work via https://www.batistellaycia.shop/images/..." -ForegroundColor White 