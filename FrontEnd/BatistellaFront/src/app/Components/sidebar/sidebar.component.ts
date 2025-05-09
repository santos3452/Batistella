import { Component, EventEmitter, Input, OnInit, Output, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ProductService } from '../../Services/Product/product.service';
import { AuthService } from '../../Services/Auth/auth.service';
import { Subscription } from 'rxjs';

interface Category {
  id: string;
  name: string;
  icon: string;
  color: string;
}

interface MarcaInfo {
  nombre: string;
  tieneIcono: boolean;
  necesitaBorde?: boolean;
}

// Información sobre los archivos de imagen/icono para cada marca
interface IconoMarca {
  nombre: string;
  archivo: string;
  necesitaBorde?: boolean;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive]
})
export class SidebarComponent implements OnInit, OnDestroy {
  @Input() isOpen: boolean = false;
  @Output() onClose = new EventEmitter<void>();
  
  isLoggedIn: boolean = false;
  isAdmin: boolean = false;
  private authSubscription: Subscription = new Subscription();
  
  animalCategories: Category[] = [
    { 
      id: 'PERROS', 
      name: 'Perros', 
      icon: '🐕', 
      color: 'text-blue-500'
    },
    { 
      id: 'GATOS', 
      name: 'Gatos', 
      icon: '🐈', 
      color: 'text-purple-500'
    },
    { 
      id: 'GRANJA', 
      name: 'Animales de Granja', 
      icon: '🐄', 
      color: 'text-green-500'
    }
  ];

  // Array para almacenar la información de las marcas
  marcasInfo: MarcaInfo[] = [];
  
  // Configuración de iconos/imágenes para cada marca
  private iconosMarcas: IconoMarca[] = [
    { nombre: 'TopNutrition', archivo: 'TopNutrition_Brand_white_vFF.ico' },
    { nombre: 'Kenl', archivo: 'kenl.ico' },
    { nombre: 'Odwalla', archivo: 'Odwalla_color_logo-150x89-1.ico' },
    { nombre: '9Lives', archivo: '9live_color_logo.ico' },
    { nombre: 'Amici', archivo: 'CariAmici_color_logo.ico' },
    { nombre: 'Zimpi', archivo: 'zimpi.ico' },
    { nombre: 'Exact', archivo: 'exact_color_logo.ico' },
    { nombre: 'Compinches', archivo: 'compinches.ico' },
    { nombre: 'Ganacan', archivo: 'ganacan.ico' },
    { nombre: 'Ganacat', archivo: 'ganacat.ico' },
    { nombre: 'Fishy', archivo: 'Fishy.ico' }
  ];
  
  // Lista de marcas que tienen iconos disponibles (para uso rápido)
  private marcasConIcono: string[] = this.iconosMarcas.map(item => item.nombre);
  
  constructor(
    private productService: ProductService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Cargar las marcas al iniciar el componente
    this.loadMarcas();
    
    // Suscripción al estado de autenticación
    this.authSubscription = this.authService.currentUser$.subscribe(user => {
      this.isLoggedIn = !!user;
      // Verificar si el usuario es administrador
      this.isAdmin = user?.rol === 'ROLE_ADMIN';
    });
  }
  
  ngOnDestroy(): void {
    // Cancelar suscripciones para evitar pérdidas de memoria
    this.authSubscription.unsubscribe();
  }

  loadMarcas(): void {
    this.productService.getAllMarcas().subscribe({
      next: (marcas) => {
        // Transformar el array de nombres en array de MarcaInfo
        this.marcasInfo = marcas.map(marca => {
          const iconoMarca = this.iconosMarcas.find(item => item.nombre === marca);
          return {
            nombre: marca,
            tieneIcono: this.marcasConIcono.includes(marca),
            necesitaBorde: iconoMarca?.necesitaBorde
          };
        });
      },
      error: (error) => {
        console.error('Error al cargar las marcas:', error);
      }
    });
  }

  getIconPath(marca: string): string {
    // Buscar la configuración del icono para esta marca
    const iconoMarca = this.iconosMarcas.find(item => item.nombre === marca);
    
    if (iconoMarca) {
      return `assets/Images/IconsMarcas/${iconoMarca.archivo}`;
    }
    
    // Si no se encuentra un archivo específico, devolver un valor por defecto
    return '';
  }
  
  closeSidebar(): void {
    this.onClose.emit();
  }
}
