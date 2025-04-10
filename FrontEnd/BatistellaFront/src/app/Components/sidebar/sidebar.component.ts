import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface Category {
  id: string;
  name: string;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive]
})
export class SidebarComponent {
  @Input() isOpen: boolean = false;
  @Output() onClose = new EventEmitter<void>();
  
  animalCategories: Category[] = [
    { 
      id: 'dog', 
      name: 'Perros', 
      icon: '🐕', 
      color: 'text-blue-500'
    },
    { 
      id: 'cat', 
      name: 'Gatos', 
      icon: '🐈', 
      color: 'text-purple-500'
    },
    { 
      id: 'farm', 
      name: 'Animales de Granja', 
      icon: '🐄', 
      color: 'text-green-500'
    }
  ];
  
  closeSidebar(): void {
    this.onClose.emit();
  }
}
