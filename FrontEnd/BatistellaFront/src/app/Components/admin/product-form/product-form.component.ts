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
  dragOver = false; // Para indicar cuando se está arrastrando un archivo
  // Descuento para calcular precio mayorista (28%)
  readonly DESCUENTO_MAYORISTA = 0.28;
  
  // Selección de tipo de producto inicial
  seleccionInicial = true; // Muestra la pantalla de selección inicial
  tipoProductoSeleccionado: 'MASCOTA' | 'GRANJA' | null = null;

  // Opciones para los campos del formulario
  marcaOptions = [
    { value: 'TOPNUTRITION', label: 'TopNutrition' },
    { value: 'KENL', label: 'Ken-L' },
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
    { value: 'CACHORRO', label: 'Cachorro' },
    { value: 'ADULTO_SENIOR', label: 'Adulto Senior' },
    { value: 'LIGHT', label: 'Light' },
    { value: 'PIEL_SENSIBLE', label: 'Piel Sensible' },
    { value: 'PREMIUM', label: 'Premium' },
    { value: 'CARNE', label: 'Carne' },
    { value: 'MIX_CARNE_HIGADO_POLLO', label: 'Mix Carne, Hígado y Pollo' },
    { value: 'PESCADO', label: 'Pescado' },
    { value: 'CARNE_POLLO_ATUN', label: 'Carne, Pollo y Atún' },
    { value: 'CARNE_POLLO_VERDURAS', label: 'Carne, Pollo y Verduras' },
    { value: 'PEQUEÑAS', label: 'Pequeñas' },
    { value: 'CARNE_Y_LECHE', label: 'Carne y Leche' },
    { value: 'SALMON_Y_ATUN', label: 'Salmón y Atún' },
    { value: 'CARNES_SELECCIONADAS', label: 'Carnes Seleccionadas' },
    { value: 'HOGAREÑOS_ESTERILIZADOS', label: 'Hogareños/Esterilizados' },
    { value: 'GATITO_KITTEN', label: 'Gatito Kitten' },
    { value: 'PERRO_CACHORRO', label: 'Perro Cachorro' },
    { value: 'PERRO_ADULTO', label: 'Perro Adulto' },
    { value: 'PREMIUM_PERRO_CACHORRO', label: 'Premium Perro Cachorro' },
    { value: 'PREMIUM_PERRO_ADULTO', label: 'Premium Perro Adulto' },
    { value: 'PREMIUM_GATO_ADULTO_URINARIO', label: 'Premium Gato Adulto Urinario' }
  ];

  tipoRazaOptions = [
    { value: 'RAZA_GRANDE', label: 'Raza Grande' },
    { value: 'RAZA_MEDIANA', label: 'Raza Mediana' },
    { value: 'RAZA_PEQUENA', label: 'Raza Pequeña' },
    { value: 'RAZA_MEDIANA_GRANDE', label: 'Raza Mediana y Grande' }
  ];

  kgOptions = [
    { value: 'ONE_KG', label: '1kg' },
    { value: 'ONE_POINT_FIVE', label: '1.5kg' },
    { value: 'TWO_KG', label: '2kg' },
    { value: 'THREE_KG', label: '3kg' },
    { value: 'FIVE_KG', label: '5kg' },
    { value: 'SEVEN_POINT_FIVE_KG', label: '7.5kg' },
    { value: 'EIGHT_KG', label: '8kg' },
    { value: 'TEN_KG', label: '10kg' },
    { value: 'FIFTEEN_KG', label: '15kg' },
    { value: 'FIFTEEN_PLUS_THREE_KG', label: '15+3kg' },
    { value: 'EIGHTEEN_KG', label: '18kg' },
    { value: 'EIGHTEEN_PLUS_THREE_KG', label: '18+3kg' },
    { value: 'TWENTY_KG', label: '20kg' },
    { value: 'TWENTY_TWO_KG', label: '22kg' },
    { value: 'TWENTY_TWO_PLUS_THREE_KG', label: '22+3kg' },
    { value: 'TWENTY_FIVE_KG', label: '25kg' },
    { value: 'THIRTY_KG', label: '30kg' }
  ];

  animalTypeOptions = [
    { value: 'PERROS', label: 'Perros' },
    { value: 'GATOS', label: 'Gatos' },
    { value: 'GRANJA', label: 'Granja' }
  ];

  // Opciones para las categorías de granja
  categoriaGranjaOptions = [
    { value: 'AVES', label: 'Aves' },
    { value: 'PONEDORAS', label: 'Ponedoras' },
    { value: 'CONEJOS', label: 'Conejos' },
    { value: 'PORCINOS', label: 'Porcinos' },
    { value: 'EQUINOS', label: 'Equinos' },
    { value: 'VACUNOS', label: 'Vacunos' },
    { value: 'VARIOS', label: 'Varios' },
    { value: 'CEREAL', label: 'Cereal' }
  ];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Verificar si estamos editando un producto existente
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id && id !== 'new') {
        this.isEditing = true;
        this.productId = id;
        this.loadProduct(id);
        // Al editar, saltamos la pantalla de selección inicial
        this.seleccionInicial = false;
      }
    });
  }

  /**
   * Selecciona el tipo de producto a crear y muestra el formulario correspondiente
   */
  seleccionarTipoProducto(tipo: 'MASCOTA' | 'GRANJA'): void {
    this.tipoProductoSeleccionado = tipo;
    this.seleccionInicial = false;
    
    // Inicializar formulario según el tipo seleccionado
    if (tipo === 'MASCOTA') {
      this.initFormMascotas();
    } else {
      this.initFormGranja();
    }
    
    // Escuchar cambios en los campos de precio para calcular automáticamente
    this.listenPriceChanges();
  }

  /**
   * Inicializa el formulario para productos de mascotas
   */
  initFormMascotas(): void {
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

  /**
   * Inicializa el formulario para productos de granja/cereales
   */
  initFormGranja(): void {
    this.productForm = this.fb.group({
      nombre: ['', Validators.required],
      categoriaGranja: ['', Validators.required],
      description: [''],
      kg: ['', Validators.required],
      priceMinorista: ['', [Validators.required, Validators.min(0)]],
      priceMayorista: ['', [Validators.required, Validators.min(0)]],
      stock: ['', [Validators.required, Validators.min(0)]],
      animalType: ['GRANJA'], // Siempre es GRANJA para este tipo de producto
      activo: [true]
    });
  }

  /**
   * Escucha cambios en los campos de precio para calcular automáticamente el otro precio con un 28% de descuento
   */
  listenPriceChanges(): void {
    // Escuchar cambios en el precio minorista
    this.productForm.get('priceMinorista')?.valueChanges.subscribe(value => {
      if (value && !isNaN(parseFloat(value))) {
        // Solo actualizar el precio mayorista si el campo no está enfocado
        const mayoristaPrecio = this.calcularPrecioMayorista(parseFloat(value));
        const mayoristaCampo = this.productForm.get('priceMayorista');
        
        // Verificar si el campo de precio mayorista está enfocado
        const mayoristaCampoElement = document.getElementById('priceMayorista');
        if (mayoristaCampo && !document.activeElement?.isSameNode(mayoristaCampoElement)) {
          mayoristaCampo.setValue(mayoristaPrecio.toFixed(2), { emitEvent: false });
        }
      }
    });

    // Escuchar cambios en el precio mayorista
    this.productForm.get('priceMayorista')?.valueChanges.subscribe(value => {
      if (value && !isNaN(parseFloat(value))) {
        // Solo actualizar el precio minorista si el campo no está enfocado
        const minoristaPrecio = this.calcularPrecioMinorista(parseFloat(value));
        const minoristaCampo = this.productForm.get('priceMinorista');
        
        // Verificar si el campo de precio minorista está enfocado
        const minoristaCampoElement = document.getElementById('priceMinorista');
        if (minoristaCampo && !document.activeElement?.isSameNode(minoristaCampoElement)) {
          minoristaCampo.setValue(minoristaPrecio.toFixed(2), { emitEvent: false });
        }
      }
    });
  }

  /**
   * Calcula el precio mayorista a partir del precio minorista
   * aplicando un 28% de descuento
   */
  calcularPrecioMayorista(precioMinorista: number): number {
    return precioMinorista * (1 - this.DESCUENTO_MAYORISTA);
  }

  /**
   * Calcula el precio minorista a partir del precio mayorista
   * considerando el 28% de descuento
   */
  calcularPrecioMinorista(precioMayorista: number): number {
    return precioMayorista / (1 - this.DESCUENTO_MAYORISTA);
  }

  loadProduct(id: string): void {
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        console.log('Producto cargado para editar:', product);
        
        // Determinar el tipo de producto para saber qué formulario mostrar
        if (product.animalType === 'GRANJA') {
          this.tipoProductoSeleccionado = 'GRANJA';
          this.initFormGranja();
          
          // Asignar valores al formulario de granja
          this.productForm.patchValue({
            nombre: product.nombre || '',
            categoriaGranja: product.categoriaGranja || '',
            description: product.description || '',
            kg: this.mapKgValue(product.kg),
            priceMinorista: product.priceMinorista,
            priceMayorista: product.priceMayorista,
            stock: product.stock,
            activo: product.activo
          });
        } else {
          this.tipoProductoSeleccionado = 'MASCOTA';
          this.initFormMascotas();
          
          // Mapear los valores recibidos a los valores aceptados por los selectores para productos de mascotas
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
          
          // Asignar valores al formulario de mascotas
          this.productForm.patchValue({
            marca: marcaValue,
            tipoAlimento: tipoAlimentoValue,
            tipoRaza: product.tipoRaza || '',
            animalType: product.animalType,
            description: product.description || '',
            kg: this.mapKgValue(product.kg),
            priceMinorista: product.priceMinorista,
            priceMayorista: product.priceMayorista,
            stock: product.stock,
            activo: product.activo
          });
        }
        
        // Configurar listeners para cambios en precios
        this.listenPriceChanges();
        
        // Si hay imagen, mostrarla en la vista previa
        if (product.imageUrl) {
          this.imagePreview = product.imageUrl;
        }
      },
      error: (error) => {
        console.error('Error al cargar el producto', error);
        this.errorMessage = 'Error al cargar el producto. Por favor, intenta de nuevo más tarde.';
      }
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.markFormGroupTouched(this.productForm);
      return;
    }

    // Validación de imagen:
    if (!this.selectedFile) {
      if (!this.isEditing) {
        this.errorMessage = 'Debe seleccionar una imagen para el producto';
        return;
      } else if (!this.imagePreview) {
        this.errorMessage = 'El producto debe tener una imagen. Por favor, seleccione una.';
        return;
      }
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Crear una copia del formulario para no modificar el original
    const formData = { ...this.productForm.value };
    
    // Manejar campos según el tipo de producto
    if (this.tipoProductoSeleccionado === 'GRANJA') {
      // Para productos de granja, establecemos marca como null y eliminamos campos específicos de mascotas
      formData.marca = null;
      formData.tipoAlimento = null;
      formData.tipoRaza = null;
      formData.animalType = 'GRANJA';
    } else {
      // Para productos de mascotas, solo manejamos el tipo de raza
      if (formData.tipoRaza === '') {
        formData.tipoRaza = null;
      }
    }

    // Generar el nombre completo del producto usando el servicio (solo para uso local)
    const fullNameForDisplay = this.productService.generateProductFullName(formData);
    console.log('Nombre completo generado (solo para mostrar):', fullNameForDisplay);
    
    // Obtener el ID para usarlo tanto en la URL como en el cuerpo de la petición
    const productId = this.isEditing ? this.productId : null;
    
    // Si estamos editando, añadir el ID al objeto del producto
    if (this.isEditing && productId) {
      formData.id = productId;
    }
    
    // Si estamos editando y hay una imagen previa pero no se seleccionó una nueva,
    // incluimos la URL de la imagen actual para mantenerla
    if (this.isEditing && !this.selectedFile && this.imagePreview) {
      formData.imageUrl = this.imagePreview;
    }
    
    // Eliminar el campo fullName ya que no existe en el backend
    delete formData.fullName;
    
    console.log('Datos del producto a enviar:', formData);

    // LÓGICA DUAL DE ACTUALIZACIÓN:
    if (this.isEditing && !this.selectedFile) {
      // CASO 1: Actualización sin cambio de imagen - Usar updateProduct con JSON
      console.log('Actualizando producto sin cambiar imagen, usando endpoint JSON');
      if (productId) {
        this.productService.updateProduct(productId, formData).subscribe({
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
      const formDataToSend = new FormData();
      
      // Añadir el producto como una cadena JSON
      formDataToSend.append('product', JSON.stringify(formData));
      
      // Añadir la imagen si hay una seleccionada
      if (this.selectedFile) {
        formDataToSend.append('image', this.selectedFile);
      }
      
      this.sendRequestWithImage(formDataToSend, productId);
    }
  }

  /**
   * Método para volver a la pantalla de selección de tipo de producto
   */
  volverASeleccion(): void {
    this.seleccionInicial = true;
    this.tipoProductoSeleccionado = null;
    this.productForm = null!;
    this.imagePreview = null;
    this.selectedFile = null;
    this.errorMessage = '';
    this.successMessage = '';
  }
  
  private mapKgValue(kg: string | undefined): string {
    if (!kg) return '';
    
    // Primero buscar por label exacto
    const kgOption = this.kgOptions.find(opt => opt.label === kg);
    if (kgOption) {
      return kgOption.value;
    }
    
    // Si no hay coincidencia exacta, intentar normalizar el formato
    const kgNormalized = kg.replace(' ', '').replace(',', '.').toLowerCase();
    // Buscar una opción cuya etiqueta normalizada coincida
    const kgOptionNormalized = this.kgOptions.find(opt => 
      opt.label.replace(' ', '').replace(',', '.').toLowerCase() === kgNormalized
    );
    
    if (kgOptionNormalized) {
      return kgOptionNormalized.value;
    }
    
    // Si todavía no hay coincidencia, añadir una nueva opción
    const newValue = `KG_${kgNormalized.replace('.', '_')}`;
    this.kgOptions.push({ value: newValue, label: kg });
    return newValue;
  }

  // Método para activar el input de selección de archivo
  triggerFileInput(): void {
    document.getElementById('product-image')?.click();
  }

  // Método para eliminar la imagen seleccionada
  removeImage(): void {
    this.imagePreview = null;
    this.selectedFile = null;
    // Limpiar también el input de archivo
    const fileInput = document.getElementById('product-image') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  // Método para manejar cuando se arrastra un archivo sobre el dropzone
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = true;
  }

  // Método para manejar cuando se deja de arrastrar sobre el dropzone
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = false;
  }

  // Método para manejar cuando se suelta un archivo en el dropzone
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = false;
    
    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      const file = event.dataTransfer.files[0];
      if (file.type.startsWith('image/')) {
        // Simular un evento de cambio de input
        const changeEvent = { target: { files: [file] } } as unknown as Event;
        this.onFileSelected(changeEvent);
      }
    }
  }

  // Método para regresar a la página anterior
  goBack(): void {
    this.router.navigate(['/admin/products']);
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
          
          // Crear archivo a partir del blob
          const convertedFile = new File([blob], file.name.replace(/\.[^.]+$/, '.jpg'), {
            type: 'image/jpeg',
            lastModified: new Date().getTime()
          });
          
          resolve(convertedFile);
        }, 'image/jpeg', quality);
      };
      
      img.onerror = () => {
        reject(new Error('Error al cargar la imagen'));
      };
      
      // Cargar imagen desde archivo
      const reader = new FileReader();
      reader.onload = (e) => {
        img.src = e.target?.result as string;
      };
      reader.onerror = () => {
        reject(new Error('Error al leer el archivo'));
      };
      reader.readAsDataURL(file);
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

  // Método auxiliar para marcar todos los campos como touched
  markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  /**
   * Enviar petición con imagen usando FormData
   */
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
} 