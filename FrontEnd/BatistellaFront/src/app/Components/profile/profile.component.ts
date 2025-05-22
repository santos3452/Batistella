import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../Services/Auth/auth.service';
import { UserService } from '../../Services/Auth/user.service';
import { LocationService, Location } from '../../Services/Utils/location.service';
import { firstValueFrom, debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { User, Address, UpdateUserDto, UpdateAddressDto } from '../../Models/user';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
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

  // Gestión de direcciones
  showAddressForm = false;
  currentAddresses: Address[] = [];
  newAddress: UpdateAddressDto = {
    id: null,
    calle: '',
    numero: '',
    ciudad: '',
    codigoPostal: 0
  };
  editingAddressIndex: number = -1;

  // Búsqueda de localidades
  locations: Location[] = [];
  filteredLocations: Location[] = [];
  showLocationDropdown = false;
  searchTerm = '';
  private searchTerms = new Subject<string>();
  isLoadingLocations = false;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private locationService: LocationService,
    private router: Router
  ) {
    // Configura el observable de búsqueda con debounce
    this.searchTerms.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(term => {
      this.filterLocations(term);
    });
  }

  ngOnInit(): void {
    // Verificar si el token sigue siendo válido
    if (!this.authService.isAuthenticated()) {
      console.log('Token expirado al acceder al perfil');
      this.router.navigate(['/login']);
      return;
    }
    
    this.isLoading = true;
    
    // Primero obtenemos los datos actuales del usuario
    this.user = this.authService.currentUser;
    if (this.user) {
      // Crear una copia para editar
      this.editableUser = {...this.user};
      this.currentAddresses = this.user.domicilio || [];
    }
    
    // Luego intentamos obtener datos actualizados desde la API
    this.authService.refreshUserInfo().subscribe({
      next: (updatedUser) => {
        console.log('Datos de usuario actualizados desde la API:', updatedUser);
        this.user = updatedUser;
        this.editableUser = {...updatedUser};
        this.currentAddresses = updatedUser.domicilio || [];
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al actualizar datos del usuario:', error);
        this.errorMessage = 'No se pudieron obtener los datos actualizados del usuario';
        this.isLoading = false;
      }
    });

    // Cargar las localidades de Córdoba
    this.loadLocations();
  }

  loadLocations(): void {
    this.isLoadingLocations = true;
    this.locationService.getLocations('cordoba').subscribe({
      next: (locations) => {
        this.locations = locations;
        this.isLoadingLocations = false;
      },
      error: (error) => {
        console.error('Error al cargar localidades:', error);
        this.isLoadingLocations = false;
      }
    });
  }

  onSearchInputChange(term: string): void {
    this.searchTerms.next(term);
  }

  // Función para normalizar textos eliminando acentos
  private normalizeText(text: string): string {
    return text.toLowerCase()
      .normalize('NFD') // Descompone caracteres acentuados
      .replace(/[\u0300-\u036f]/g, '') // Elimina marcas diacríticas (acentos)
      .trim();
  }

  filterLocations(term: string): void {
    this.showLocationDropdown = term.length > 0;
    
    if (term.length === 0) {
      this.filteredLocations = [];
      return;
    }

    const normalizedTerm = this.normalizeText(term);
    
    // Primero filtramos localmente
    const localResults = this.locations
      .filter(location => this.normalizeText(location.nombre).includes(normalizedTerm))
      .slice(0, 10);
    
    this.filteredLocations = localResults;
    
    // Si no hay resultados locales y el término tiene más de 2 caracteres, 
    // intentamos buscar remotamente
    if (localResults.length === 0 && term.length > 2) {
      this.isLoadingLocations = true;
      this.locationService.searchLocations('cordoba', term).subscribe({
        next: (results) => {
          this.filteredLocations = results;
          this.isLoadingLocations = false;
        },
        error: (error) => {
          console.error('Error en búsqueda remota:', error);
          this.isLoadingLocations = false;
        }
      });
    }
  }

  selectLocation(location: Location): void {
    this.newAddress.ciudad = location.nombre;
    this.searchTerm = location.nombre;
    this.showLocationDropdown = false;
  }

  // Actualizar la ciudad de la dirección cuando se pierde el foco
  updateCiudadOnBlur(): void {
    if (this.searchTerm.trim()) {
      this.newAddress.ciudad = this.searchTerm.trim();
    }
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    this.errorMessage = '';
    this.successMessage = '';
    this.showPasswordFields = false;
    this.showAddressForm = false;
    
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
    this.showAddressForm = false;
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
    if (!this.editableUser || !this.user) return;
    
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const updateData: UpdateUserDto = {
        mail: this.editableUser.email,
        password: '', // No estamos cambiando la contraseña aquí
        nombre: this.editableUser.nombre,
        apellido: this.editableUser.apellido,
        adresses: this.currentAddresses.map(addr => ({
          id: addr.id, // Ya es null o un número según corresponda
          calle: addr.calle,
          numero: addr.numero,
          ciudad: addr.ciudad,
          codigoPostal: addr.codigoPostal
        }))
      };

      console.log('Enviando direcciones para actualizar:', updateData.adresses);

      await firstValueFrom(
        this.userService.updateUser(updateData)
      );

      this.successMessage = 'Perfil actualizado exitosamente';
      
      // Recargar después de mostrar el mensaje de éxito
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
    this.showAddressForm = false;
    
    if (this.user) {
      this.editableUser = {...this.user};
      this.currentAddresses = this.user.domicilio || [];
    }
  }

  // Métodos para manejar direcciones
  toggleAddressForm(): void {
    this.showAddressForm = !this.showAddressForm;
    if (this.showAddressForm) {
      this.resetAddressForm();
    }
  }

  resetAddressForm(): void {
    this.newAddress = {
      id: null,
      calle: '',
      numero: '',
      ciudad: '',
      codigoPostal: 0
    };
    this.searchTerm = '';
    this.editingAddressIndex = -1;
  }

  addAddress(): void {
    if (this.validateAddress()) {
      if (this.editingAddressIndex >= 0) {
        // Actualizar dirección existente
        this.currentAddresses[this.editingAddressIndex] = {...this.newAddress};
      } else {
        // Agregar nueva dirección
        this.currentAddresses.push({...this.newAddress});
      }
      this.showAddressForm = false;
      this.resetAddressForm();
    }
  }

  editAddress(index: number): void {
    const address = this.currentAddresses[index];
    this.newAddress = {
      id: address.id,
      calle: address.calle,
      numero: address.numero,
      ciudad: address.ciudad,
      codigoPostal: address.codigoPostal
    };
    this.searchTerm = address.ciudad;
    this.editingAddressIndex = index;
    this.showAddressForm = true;
  }

  removeAddress(index: number): void {
    this.currentAddresses.splice(index, 1);
  }

  validateAddress(): boolean {
    if (!this.newAddress.calle || !this.newAddress.numero || !this.newAddress.ciudad || !this.newAddress.codigoPostal) {
      this.errorMessage = 'Por favor complete todos los campos de la dirección';
      return false;
    }
    this.errorMessage = '';
    return true;
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

  hideLocationDropdown(): void {
    // Pequeño retraso para permitir que el clic en los elementos funcione
    setTimeout(() => {
      this.showLocationDropdown = false;
    }, 150);
  }
} 