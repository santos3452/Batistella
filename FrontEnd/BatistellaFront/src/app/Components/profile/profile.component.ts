import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../Services/Auth/auth.service';
import { UserInfo } from '../../Services/Auth/auth.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ProfileComponent implements OnInit {
  user: UserInfo | null = null;
  isEditing: boolean = false;
  editableUser: UserInfo | null = null;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.isLoading = true;
    
    // Primero obtenemos los datos actuales del usuario
    this.user = this.authService.currentUser;
    if (this.user) {
      // Crear una copia para editar
      this.editableUser = {...this.user};
    }
    
    // Luego intentamos obtener datos actualizados desde la API
    this.authService.refreshUserInfo().subscribe({
      next: (updatedUser) => {
        console.log('Datos de usuario actualizados desde la API:', updatedUser);
        this.user = updatedUser;
        this.editableUser = {...updatedUser};
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al actualizar datos del usuario:', error);
        this.errorMessage = 'No se pudieron obtener los datos actualizados del usuario';
        this.isLoading = false;
      }
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    this.errorMessage = '';
    this.successMessage = '';
    
    if (this.isEditing && this.user) {
      // Si entramos en modo edición, crear una copia de los datos para editar
      this.editableUser = {...this.user};
    }
  }

  saveChanges(): void {
    if (!this.editableUser) return;
    
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    // Aquí iría el código para guardar los cambios en el backend
    // Por ahora solo actualizamos el localStorage y el currentUser
    this.user = {...this.editableUser};
    
    localStorage.setItem('currentUser', JSON.stringify(this.user));
    this.authService.updateUserInfo(this.user);
    
    setTimeout(() => {
      this.successMessage = 'Perfil actualizado correctamente';
      this.isLoading = false;
      this.isEditing = false;
    }, 500);
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.errorMessage = '';
    this.successMessage = '';
    
    if (this.user) {
      // Descartar cambios y restaurar los datos originales
      this.editableUser = {...this.user};
    }
  }

  // Helper que devuelve la primera letra del nombre o 'U' si no hay nombre
  get userInitial(): string {
    if (this.user && this.user.nombre && this.user.nombre.length > 0) {
      return this.user.nombre.charAt(0).toUpperCase();
    }
    return 'U';
  }
} 