import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../Services/Auth/auth.service';
import { UserService } from '../../Services/Auth/user.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ProfileComponent implements OnInit {
  user: any;
  editableUser: any;
  isEditing = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  showDeleteModal = false;
  
  // Campos para el cambio de contraseña
  currentPassword = '';
  newPassword = '';
  confirmNewPassword = '';
  showPasswordFields = false;

  constructor(
    private authService: AuthService,
    private userService: UserService
  ) {}

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
    this.showPasswordFields = false;
    
    if (this.isEditing && this.user) {
      this.editableUser = {...this.user};
    }
  }

  togglePasswordFields(): void {
    this.showPasswordFields = !this.showPasswordFields;
    this.isEditing = false;
    this.errorMessage = '';
    this.successMessage = '';
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmNewPassword = '';
  }

  cancelPasswordChange(): void {
    this.showPasswordFields = false;
    this.errorMessage = '';
    this.successMessage = '';
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmNewPassword = '';
  }

  savePasswordChange() {
    if (this.currentPassword && this.newPassword && this.confirmNewPassword) {
      if (this.newPassword !== this.confirmNewPassword) {
        this.errorMessage = 'Las contraseñas no coinciden';
        return;
      }

      this.userService.changePassword(this.currentPassword, this.newPassword).subscribe({
        next: (response) => {
          this.successMessage = 'Contraseña actualizada exitosamente';
          this.errorMessage = '';
          this.currentPassword = '';
          this.newPassword = '';
          this.confirmNewPassword = '';
          this.showPasswordFields = false;
        },
        error: (error) => {
          console.error('Error en cambio de contraseña:', error);
          this.errorMessage = error.message || 'Error al cambiar la contraseña';
          this.successMessage = '';
        }
      });
    } else {
      this.errorMessage = 'Por favor complete todos los campos';
    }
  }

  async saveChanges(): Promise<void> {
    if (!this.editableUser) return;
    
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const updateData: any = {
        nombre: this.editableUser.nombre,
        apellido: this.editableUser.apellido
      };

      await firstValueFrom(
        this.userService.updateUser(this.editableUser.email, updateData)
      );

      // Si la actualización fue exitosa, solo recargamos
      setTimeout(() => {
        window.location.reload();
      }, 1000);

    } catch (error: any) {
      console.error('Error al actualizar perfil:', error);
      this.isLoading = false;
      
      if (error.error?.message) {
        this.errorMessage = error.error.message;
      } else if (error.status === 403) {
        this.errorMessage = 'No tiene permisos para realizar esta acción. Por favor, vuelva a iniciar sesión.';
      } else if (error.status === 0) {
        this.errorMessage = 'No se pudo conectar con el servidor. Por favor, verifique su conexión.';
      } else {
        this.errorMessage = error.message || 'Error al actualizar el perfil';
      }
    }
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.errorMessage = '';
    this.successMessage = '';
    
    if (this.user) {
      this.editableUser = {...this.user};
    }
  }

  get userInitial(): string {
    if (this.user?.nombre) {
      return this.user.nombre.charAt(0).toUpperCase();
    }
    return 'U';
  }

  deleteAccount(): void {
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    this.showDeleteModal = false;
    this.isLoading = true;
    this.errorMessage = '';
    
    this.userService.deleteUser().subscribe({
      next: () => {
        this.authService.logout();
        window.location.href = '/login';
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.message || 'Error al eliminar la cuenta';
      }
    });
  }

  cancelDelete(): void {
    this.showDeleteModal = false;
  }
} 