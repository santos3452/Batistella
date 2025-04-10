import { Injectable } from '@angular/core';
import { Product } from '../Models/product';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  // In a real app, this would be fetched from an API
  private products: Product[] = [
    {
      id: 1,
      name: 'Croquetas Premium para Perro',
      price: 24.99,
      category: 'dog',
      description: 'Alimento completo y balanceado para perros adultos de todas las razas.',
      image: 'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8ZG9nJTIwZm9vZHxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=800&q=60',
    },
    {
      id: 2,
      name: 'Juguete Interactivo para Gato',
      price: 12.99,
      category: 'cat',
      description: 'Juguete con plumas y cascabel para mantener a tu gato activo y entretenido.',
      image: 'https://images.unsplash.com/photo-1574158622682-e40e69881006?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8M3x8Y2F0JTIwdG95fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=800&q=60',
    },
    {
      id: 3,
      name: 'Comedero Automático',
      price: 39.99,
      category: 'farm',
      description: 'Comedero automático para animales de granja con temporizador programable.',
      image: 'https://images.unsplash.com/photo-1500595046743-cd271d694e30?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8ZmFybSUyMGZlZWRlcnxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=800&q=60',
    },
    {
      id: 4,
      name: 'Collar Antiparasitario',
      price: 18.50,
      category: 'dog',
      description: 'Collar que protege a tu perro contra pulgas y garrapatas durante hasta 8 meses.',
      image: 'https://images.unsplash.com/photo-1622020457014-24a745608d1d?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTB8fGRvZyUyMGNvbGxhcnxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=800&q=60',
    },
    {
      id: 5,
      name: 'Arena para Gatos',
      price: 9.99,
      category: 'cat',
      description: 'Arena aglomerante premium para gatos, con control de olores y fácil limpieza.',
      image: 'https://images.unsplash.com/photo-1603360946369-dc9bb6258143?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTR8fGNhdCUyMGxpdHRlcnxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=800&q=60',
    },
    {
      id: 6,
      name: 'Vitaminas para Aves',
      price: 15.75,
      category: 'farm',
      description: 'Suplemento vitamínico para aves de corral que mejora la salud y producción.',
      image: 'https://images.unsplash.com/photo-1552728089-57bdde30beb3?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8Y2hpY2tlbnxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=800&q=60',
    },
    {
      id: 7,
      name: 'Cama Ortopédica',
      price: 49.99,
      category: 'dog',
      description: 'Cama especial con espuma de memoria para perros senior o con problemas articulares.',
      image: 'https://images.unsplash.com/photo-1541599540903-216a46ca1dc0?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8OHx8ZG9nJTIwYmVkfGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=800&q=60',
    },
    {
      id: 8,
      name: 'Rascador para Gatos',
      price: 32.99,
      category: 'cat',
      description: 'Torre rascador con múltiples niveles, hamaca y juguetes colgantes.',
      image: 'https://images.unsplash.com/photo-1606426712210-53d0e5dedab7?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8M3x8Y2F0JTIwdHJlZXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=800&q=60',
    },
    {
      id: 9,
      name: 'Abrevadero Térmico',
      price: 64.50,
      category: 'farm',
      description: 'Abrevadero con calefacción para evitar que el agua se congele en invierno.',
      image: 'https://images.unsplash.com/photo-1516467508483-a7212febe31a?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8N3x8ZmFybXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=800&q=60',
    }
  ];

  constructor(private http: HttpClient) { }

  getProducts(): Observable<Product[]> {
    // In a real app: return this.http.get<Product[]>('/api/products');
    return of(this.products);
  }

  getProductsByCategory(category: string): Observable<Product[]> {
    return of(this.products.filter(product => product.category === category));
  }
}
