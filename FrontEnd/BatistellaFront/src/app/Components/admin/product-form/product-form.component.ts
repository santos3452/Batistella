import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService, Product } from '../../../Services/Product/product.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class ProductFormComponent implements OnInit {
  productForm!: FormGroup;
  isEditing = false;
  productId: string | null = null;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  selectedFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;

  // Opciones para los campos del formulario
  marcaOptions = [
    { value: 'TOPNUTRITION', label: 'TopNutrition' },
    { value: 'KENL', label: 'Kenl' },
    { value: 'ODWALLA', label: 'Odwalla' },
    { value: 'NINELIVES', label: '9Lives' },
    { value: 'AMICI', label: 'Amici' },
    { value: 'ZIMPI', label: 'Zimpi' },
    { value: 'FISHY', label: 'ProPlan' },
    { value: 'GANACAT', label: 'Ganacat' },
    { value: 'GANACAN', label: 'Ganacan' },
    { value: 'COMPINCHES', label: 'Compinches' },
    { value: 'EXACT', label: 'Exact' }
  ];

  tipoAlimentoOptions = [
    { value: 'ADULTO', label: 'Adulto' },
    { value: 'SENIOR', label: 'Senior' },
    { value: 'CACHORRO', label: 'Cachorro' }
  ];

  tipoRazaOptions = [
    { value: 'RAZA_GRANDE', label: 'Raza Grande' },
    { value: 'RAZA_MEDIANA', label: 'Raza Mediana' },
    { value: 'RAZA_PEQUENA', label: 'Raza Pequeña' }
  ];

  kgOptions = [
    { value: 'EIGHTEEN_KG', label: '18kg' },
    { value: 'THREE_KG', label: '3kg' },
    { value: 'SEVEN_POINT_FIVE_KG', label: '7.5kg' },
    { value: 'TWENTY_KG', label: '20kg' },
    { value: 'FIFTEEN_KG', label: '15kg' },
    { value: 'ONE_POINT_FIVE', label: '1.5kg' },
    { value: 'FIFTEEN_PLUS_THREE_KG', label: '15+3kg' },
    { value: 'TWENTY_TWO_KG', label: '22kg' },
    { value: 'TWENTY_TWO_PLUS_THREE_KG', label: '22+3kg' }
  ];

  animalTypeOptions = [
    { value: 'PERROS', label: 'Perros' },
    { value: 'GATOS', label: 'Gatos' },
    { value: 'GRANJA', label: 'Granja' }
  ];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.initForm();
    
    // Verificar si estamos editando un producto existente
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id && id !== 'new') {
        this.isEditing = true;
        this.productId = id;
        this.loadProduct(id);
      }
    });
  }

  initForm(): void {
    this.productForm = this.fb.group({
      marca: ['', Validators.required],
      tipoAlimento: ['', Validators.required],
      tipoRaza: [''],
      description: [''],
      kg: ['', Validators.required],
      priceMinorista: ['', [Validators.required, Validators.min(0)]],
      priceMayorista: ['', [Validators.required, Validators.min(0)]],
      stock: ['', [Validators.required, Validators.min(0)]],
      animalType: ['', Validators.required],
      activo: [true]
    });
  }

  loadProduct(id: string): void {
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        console.log('Producto cargado para editar:', product);
        
        // Mapear los valores recibidos a los valores aceptados por los selectores
        
        // Para marca - Convertir el valor de la API a las opciones del selector
        let marcaValue = '';
        if (product.marca) {
          // Buscar la opción que tenga un label que coincida con la marca (sin importar mayúsculas/minúsculas)
          const marcaOption = this.marcaOptions.find(opt => 
            opt.label.toLowerCase() === product.marca.toLowerCase()
          );
          marcaValue = marcaOption ? marcaOption.value : product.marca.toUpperCase();
        }
        
        // Para tipoAlimento - Añadir el valor si no existe en las opciones
        let tipoAlimentoValue = product.tipoAlimento || '';
        if (tipoAlimentoValue && !this.tipoAlimentoOptions.some(opt => opt.value === tipoAlimentoValue)) {
          this.tipoAlimentoOptions.push({ 
            value: tipoAlimentoValue, 
            label: tipoAlimentoValue.charAt(0) + tipoAlimentoValue.slice(1).toLowerCase()
          });
        }
        
        // Para kg - Mapear el valor de la API a las opciones del selector
        let kgValue = '';
        if (product.kg) {
          // Primero buscar por label exacto
          const kgOption = this.kgOptions.find(opt => opt.label === product.kg);
          if (kgOption) {
            kgValue = kgOption.value;
          } else {
            // Si no hay coincidencia exacta, intentar normalizar el formato
            const kgNormalized = product.kg.replace(' ', '').replace(',', '.').toLowerCase();
            // Buscar una opción cuya etiqueta normalizada coincida
            const kgOptionNormalized = this.kgOptions.find(opt => 
              opt.label.replace(' ', '').replace(',', '.').toLowerCase() === kgNormalized
            );
            
            if (kgOptionNormalized) {
              kgValue = kgOptionNormalized.value;
            } else {
              // Si todavía no hay coincidencia, añadir una nueva opción
              const newValue = `KG_${kgNormalized.replace('.', '_')}`;
              this.kgOptions.push({ value: newValue, label: product.kg });
              kgValue = newValue;
            }
          }
        }
        
        // Asegurarnos de que todos los campos estén correctamente asignados
        this.productForm.patchValue({
          marca: marcaValue,
          tipoAlimento: tipoAlimentoValue,
          tipoRaza: product.tipoRaza || '',
          description: product.description || '',
          kg: kgValue,
          priceMinorista: product.priceMinorista || 0,
          priceMayorista: product.priceMayorista || 0,
          stock: product.stock || 0,
          animalType: product.animalType || '',
          activo: product.activo !== undefined ? product.activo : true
        });
        
        // Mostrar la imagen actual
        if (product.imageUrl) {
          this.imagePreview = product.imageUrl;
        }
        
        // Verificar que los campos se han cargado correctamente
        console.log('Formulario después de cargar datos:', this.productForm.value);
        console.log('Opciones actualizadas:', {
          marca: this.marcaOptions,
          tipoAlimento: this.tipoAlimentoOptions,
          kg: this.kgOptions
        });
      },
      error: (error) => {
        console.error('Error al cargar el producto:', error);
        this.errorMessage = 'Error al cargar el producto: ' + error.message;
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const originalFile = input.files[0];
      
      // Mostrar vista previa de la imagen
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview = reader.result;
        
        // Convertir la imagen a formato WebP
        this.convertImageToWebP(originalFile, 0.9).then(convertedFile => {
          this.selectedFile = convertedFile;
          console.log('Imagen convertida a WebP:', convertedFile.name, convertedFile.type, convertedFile.size);
        }).catch(error => {
          console.error('Error al convertir la imagen:', error);
          // En caso de error, usar el archivo original
          this.selectedFile = originalFile;
        });
      };
      reader.readAsDataURL(originalFile);
    }
  }
  
  /**
   * Convierte cualquier imagen a formato JPEG
   * @param file Archivo de imagen original
   * @param quality Calidad de compresión (0 a 1)
   * @returns Promise con el archivo convertido a JPEG
   */
  private convertImageToJpeg(file: File, quality: number = 0.9): Promise<File> {
    return new Promise((resolve, reject) => {
      // Crear elemento de imagen
      const img = new Image();
      img.onload = () => {
        // Crear canvas
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        
        // Dibujar imagen en el canvas
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          reject(new Error('No se pudo obtener el contexto del canvas'));
          return;
        }
        
        // Fondo blanco para imágenes transparentes
        ctx.fillStyle = 'white';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        
        // Dibujar la imagen
        ctx.drawImage(img, 0, 0);
        
        // Convertir a JPEG
        canvas.toBlob((blob) => {
          if (!blob) {
            reject(new Error('No se pudo generar el blob'));
            return;
          }
          
          // Crear nombre de archivo con extensión .jpg
          const originalName = file.name.replace(/\.[^/.]+$/, ""); // Eliminar extensión existente
          const newFile = new File([blob], `${originalName}.jpg`, { 
            type: 'image/jpeg', 
            lastModified: file.lastModified 
          });
          
          resolve(newFile);
        }, 'image/jpeg', quality);
      };
      
      img.onerror = () => {
        reject(new Error('Error al cargar la imagen'));
      };
      
      // Cargar imagen desde el archivo
      img.src = URL.createObjectURL(file);
    });
  }
  
  /**
   * Alternativa para convertir a WebP si se prefiere ese formato
   * @param file Archivo de imagen original
   * @param quality Calidad de compresión (0 a 1)
   * @returns Promise con el archivo convertido a WebP
   */
  private convertImageToWebP(file: File, quality: number = 0.9): Promise<File> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          reject(new Error('No se pudo obtener el contexto del canvas'));
          return;
        }
        
        // Fondo blanco para imágenes transparentes
        ctx.fillStyle = 'white';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        
        // Dibujar la imagen
        ctx.drawImage(img, 0, 0);
        
        // Convertir a WebP
        canvas.toBlob((blob) => {
          if (!blob) {
            reject(new Error('No se pudo generar el blob'));
            return;
          }
          
          // Crear nombre de archivo con extensión .webp
          const originalName = file.name.replace(/\.[^/.]+$/, ""); // Eliminar extensión existente
          const newFile = new File([blob], `${originalName}.webp`, { 
            type: 'image/webp', 
            lastModified: file.lastModified 
          });
          
          resolve(newFile);
        }, 'image/webp', quality);
      };
      
      img.onerror = () => {
        reject(new Error('Error al cargar la imagen'));
      };
      
      // Cargar imagen desde el archivo
      img.src = URL.createObjectURL(file);
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.markFormGroupTouched(this.productForm);
      return;
    }

    // Validación de imagen:
    // - Para productos nuevos: siempre necesitamos una imagen
    // - Para ediciones: si no hay una nueva imagen seleccionada, debe existir una previa
    if (!this.selectedFile) {
      if (!this.isEditing) {
        // Caso 1: Nuevo producto sin imagen
        this.errorMessage = 'Debe seleccionar una imagen para el producto';
        return;
      } else if (!this.imagePreview) {
        // Caso 2: Editando un producto, sin imagen nueva ni existente
        this.errorMessage = 'El producto debe tener una imagen. Por favor, seleccione una.';
        return;
      }
      // Caso 3: Editando un producto, sin imagen nueva pero con una existente
      // En este caso, continuamos con la actualización usando la imagen existente
    }
    // Caso 4: Hay una nueva imagen seleccionada (para nuevo producto o edición)
    // Procedemos normalmente con la nueva imagen

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const productData = this.productForm.value;
    
    // Manejar campos especiales
    
    // Tipo de raza: convertir cadena vacía a null para el enum del backend
    if (productData.tipoRaza === '') {
      productData.tipoRaza = null;
    }

    // Generar el nombre completo del producto usando el servicio (solo para uso local)
    // No lo incluimos en los datos enviados al backend
    const fullNameForDisplay = this.productService.generateProductFullName(productData);
    console.log('Nombre completo generado (solo para mostrar):', fullNameForDisplay);
    
    // Obtener el ID para usarlo tanto en la URL como en el cuerpo de la petición
    const productId = this.isEditing ? this.productId : null;
    
    // Si estamos editando, añadir el ID al objeto del producto
    if (this.isEditing && productId) {
      productData.id = productId;
    }
    
    // Si estamos editando y hay una imagen previa pero no se seleccionó una nueva,
    // incluimos la URL de la imagen actual para mantenerla
    if (this.isEditing && !this.selectedFile && this.imagePreview) {
      productData.imageUrl = this.imagePreview;
    }
    
    // Crear una copia de los datos del producto para enviar al backend, sin el campo fullName
    const productDataForBackend = { ...productData };
    // Eliminar el campo fullName ya que no existe en el backend
    delete productDataForBackend.fullName;
    
    console.log('Datos del producto a enviar:', productDataForBackend);
    
    // LÓGICA DUAL DE ACTUALIZACIÓN:
    // 1. Si estamos editando y NO hay una nueva imagen, usamos el endpoint JSON simple
    // 2. Para nuevos productos o actualizaciones con nueva imagen, usamos FormData
    
    if (this.isEditing && !this.selectedFile) {
      // CASO 1: Actualización sin cambio de imagen - Usar updateProduct con JSON
      console.log('Actualizando producto sin cambiar imagen, usando endpoint JSON');
      if (productId) {
        // Agregar logs detallados para depuración
        console.log('ID del producto:', productId);
        console.log('Tipo de ID:', typeof productId);
        console.log('Datos completos enviados al endpoint updateProduct:', JSON.stringify(productDataForBackend, null, 2));
        console.log('URL del endpoint:', `${this.productService['apiUrl']}/updateProduct`);
        
        this.productService.updateProduct(productId, productDataForBackend).subscribe({
          next: (response: any) => {
            console.log('Producto actualizado:', response);
            this.isSubmitting = false;
            this.successMessage = 'Producto actualizado con éxito';
            
            // Limpiar caché de productos para forzar recarga de datos
            this.productService.clearProductsCache();
            
            setTimeout(() => {
              this.router.navigate(['/admin/products'], { queryParams: { refresh: new Date().getTime() } });
            }, 1500);
          },
          error: (error: any) => {
            console.error('Error al actualizar el producto:', error);
            this.isSubmitting = false;
            this.errorMessage = 'Error al actualizar el producto: ' + (error.error?.message || error.message);
          }
        });
      } else {
        this.isSubmitting = false;
        this.errorMessage = 'Error: ID de producto no disponible para la actualización';
      }
    } else {
      // CASO 2: Nuevo producto o actualización con nueva imagen - Usar FormData
      // Crear FormData
      const formData = new FormData();
      
      // Añadir el producto como una cadena JSON
      formData.append('product', JSON.stringify(productDataForBackend));
      
      // Añadir la imagen si hay una seleccionada (debe haberla para nuevos productos)
      if (this.selectedFile) {
        formData.append('image', this.selectedFile);
      }
      
      this.sendRequestWithImage(formData, productId);
    }
  }

  // Método para enviar la petición HTTP con imagen (FormData)
  private sendRequestWithImage(formData: FormData, productId: string | number | null): void {
    if (this.isEditing && productId) {
      // Si estamos editando, usamos el método de actualización con imagen
      this.productService.updateProductWithImage(productId, formData).subscribe({
        next: (response) => {
          console.log('Producto actualizado con imagen:', response);
          this.isSubmitting = false;
          this.successMessage = 'Producto actualizado con éxito';
          
          // Limpiar caché de productos para forzar recarga de datos
          this.productService.clearProductsCache();
          
          setTimeout(() => {
            this.router.navigate(['/admin/products'], { queryParams: { refresh: new Date().getTime() } });
          }, 1500);
        },
        error: (error) => {
          console.error('Error al actualizar el producto con imagen:', error);
          this.isSubmitting = false;
          this.errorMessage = 'Error al actualizar el producto: ' + (error.error?.message || error.message);
        }
      });
    } else {
      // Si estamos creando un nuevo producto
      this.productService.saveProductWithImage(formData).subscribe({
        next: (response) => {
          console.log('Producto creado:', response);
          this.isSubmitting = false;
          this.successMessage = 'Producto creado con éxito';
          
          // Limpiar caché de productos para forzar recarga de datos
          this.productService.clearProductsCache();
          
          setTimeout(() => {
            this.router.navigate(['/admin/products'], { queryParams: { refresh: new Date().getTime() } });
          }, 1500);
        },
        error: (error) => {
          console.error('Error al crear el producto:', error);
          this.isSubmitting = false;
          this.errorMessage = 'Error al crear el producto: ' + (error.error?.message || error.message);
        }
      });
    }
  }

  // Método auxiliar para marcar todos los campos como touched
  markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
} 