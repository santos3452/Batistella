import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../Services/Auth/auth.service';

@Component({
  selector: 'app-admin-panel',
  templateUrl: './admin-panel.component.html',
  styleUrls: ['./admin-panel.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class AdminPanelComponent {
  
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}
  
  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
  
  isAdmin(): boolean {
    return this.authService.isAdmin();
  }
} 