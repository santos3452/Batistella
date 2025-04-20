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
    this.showPasswordFields = false;
    
    if (this.isEditing && this.user) {
      // Si entramos en modo edición, crear una copia de los datos para editar
      this.editableUser = {...this.user};
    }
  }

  togglePasswordFields(): void {
    this.showPasswordFields = !this.showPasswordFields;
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmNewPassword = '';
  }

  async saveChanges(): Promise<void> {
    if (!this.editableUser) return;
    
    this.isLoading = true;
    this.errorMessage = '';

    try {
      // Si se está intentando cambiar la contraseña
      if (this.showPasswordFields) {
        if (!this.currentPassword || !this.newPassword || !this.confirmNewPassword) {
          throw new Error('Por favor complete todos los campos de contraseña');
        }

        if (this.newPassword !== this.confirmNewPassword) {
          throw new Error('Las contraseñas nuevas no coinciden');
        }

        // Verificar la contraseña actual
        const isPasswordValid = await firstValueFrom(
          this.userService.verifyCurrentPassword(
            this.editableUser.email,
            this.currentPassword
          )
        );

        if (!isPasswordValid) {
          throw new Error('La contraseña actual es incorrecta');
        }
      }

      // Preparar los datos para la actualización
      const updateData: any = {
        nombre: this.editableUser.nombre,
        apellido: this.editableUser.apellido
      };

      // Si hay una nueva contraseña, incluirla en la actualización
      if (this.showPasswordFields && this.newPassword) {
        updateData.password = this.newPassword;
      }

      // Realizar la actualización
      await firstValueFrom(
        this.userService.updateUser(this.editableUser.email, updateData)
      );

      // Esperar 1 segundo y recargar
      setTimeout(() => {
        window.location.reload();
      }, 1000);

    } catch (error: any) {
      console.error('Error completo:', error);
      
      // Manejar diferentes tipos de errores
      if (error.error?.message) {
        this.errorMessage = error.error.message;
      } else if (error.status === 403) {
        this.errorMessage = 'No tiene permisos para realizar esta acción. Por favor, vuelva a iniciar sesión.';
      } else if (error.status === 0) {
        this.errorMessage = 'No se pudo conectar con el servidor. Por favor, verifique su conexión.';
      } else {
        this.errorMessage = error.message || 'Error al actualizar el perfil';
      }

      // Si la actualización fue exitosa a pesar del error, solo recargar
      if (error.status === 200 || error.status === 201) {
        setTimeout(() => {
          window.location.reload();
        }, 1000);
      } else {
        this.isLoading = false;
      }
    }
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.errorMessage = '';
    this.showPasswordFields = false;
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmNewPassword = '';
    
    if (this.user) {
      // Descartar cambios y restaurar los datos originales
      this.editableUser = {...this.user};
    }
  }

  get userInitial(): string {
    if (this.user?.nombre) {
      return this.user.nombre.charAt(0).toUpperCase();
    }
    return 'U';
  }
} 