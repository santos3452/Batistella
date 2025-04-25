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
        this.productForm.patchValue({
          marca: product.marca,
          tipoAlimento: product.tipoAlimento,
          tipoRaza: product.tipoRaza || '',
          description: product.description || '',
          kg: product.kg,
          priceMinorista: product.priceMinorista,
          priceMayorista: product.priceMayorista,
          stock: product.stock,
          animalType: product.animalType,
          activo: product.activo
        });
        
        // Mostrar la imagen actual
        if (product.imageUrl) {
          this.imagePreview = product.imageUrl;
        }
      },
      error: (error) => {
        this.errorMessage = 'Error al cargar el producto: ' + error.message;
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      
      // Mostrar vista previa de la imagen
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.markFormGroupTouched(this.productForm);
      return;
    }

    if (!this.selectedFile && !this.isEditing) {
      this.errorMessage = 'Debe seleccionar una imagen para el producto';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const productData = this.productForm.value;
    
    // Preparar la data del producto como un Blob (JSON)
    const productBlob = new Blob([JSON.stringify(productData)], { type: 'application/json' });
    
    // Crear FormData
    const formData = new FormData();
    formData.append('product', productBlob);
    
    if (this.selectedFile) {
      formData.append('image', this.selectedFile);
    }

    if (this.isEditing && this.productId) {
      // Si estamos editando, pasamos el ID
      this.productService.updateProductWithImage(this.productId, formData).subscribe({
        next: (response) => {
          this.isSubmitting = false;
          this.successMessage = 'Producto actualizado con éxito';
          setTimeout(() => {
            this.router.navigate(['/admin/products']);
          }, 1500);
        },
        error: (error) => {
          this.isSubmitting = false;
          this.errorMessage = 'Error al actualizar el producto: ' + (error.error?.message || error.message);
        }
      });
    } else {
      // Si estamos creando un nuevo producto
      this.productService.saveProductWithImage(formData).subscribe({
        next: (response) => {
          this.isSubmitting = false;
          this.successMessage = 'Producto creado con éxito';
          setTimeout(() => {
            this.router.navigate(['/admin/products']);
          }, 1500);
        },
        error: (error) => {
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