import { Routes } from '@angular/router';
import { LoginComponent } from './Components/login/login.component';
import { HomeComponent } from './Components/home/home.component';
import { RegisterComponent } from './Components/register/register.component';
import { ProfileComponent } from './Components/profile/profile.component';
import { ResetPasswordComponent } from './Components/reset-password/reset-password.component';
import { AuthGuard } from './Services/Auth/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: '**', redirectTo: '' }
];
