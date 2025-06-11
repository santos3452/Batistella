import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

interface MarcaConIcono {
  nombre: string;
  iconPath: string;
  necesitaBorde?: boolean;
}

@Component({
  selector: 'app-marcas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './marcas.component.html',
  styleUrl: './marcas.component.css'
})
export class MarcasComponent {

  constructor(private router: Router) {}

  // Marcas con iconos más grandes para la página dedicada
  marcasConIconos: MarcaConIcono[] = [
    { nombre: 'Top Nutrition', iconPath: 'assets/Images/IconsMarcas/TopNutrition_Brand_white_vFF.ico' },
    { nombre: 'Ken-L', iconPath: 'assets/Images/IconsMarcas/kenl.ico' },
    { nombre: 'Odwalla', iconPath: 'assets/Images/IconsMarcas/Odwalla_color_logo-150x89-1.ico' },
    { nombre: '9Lives', iconPath: 'assets/Images/IconsMarcas/9live_color_logo.ico' },
    { nombre: 'Amici', iconPath: 'assets/Images/IconsMarcas/CariAmici_color_logo.ico' },
    { nombre: 'Zimpi', iconPath: 'assets/Images/IconsMarcas/zimpi.ico' },
    { nombre: 'Exact', iconPath: 'assets/Images/IconsMarcas/exact_color_logo.ico' },
    { nombre: 'Compinches', iconPath: 'assets/Images/IconsMarcas/compinches.ico' },
    { nombre: 'Ganacan', iconPath: 'assets/Images/IconsMarcas/ganacan.ico' },
    { nombre: 'Ganacat', iconPath: 'assets/Images/IconsMarcas/ganacat.ico' },
    { nombre: 'Fishy', iconPath: 'assets/Images/IconsMarcas/Fishy.ico' }
  ];

  // Método para navegar a productos de una marca específica
  navigateToMarca(nombreMarca: string): void {
    this.router.navigate(['/'], { queryParams: { marca: nombreMarca } });
  }
}
