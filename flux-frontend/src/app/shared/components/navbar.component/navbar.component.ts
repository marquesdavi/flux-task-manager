// src/app/shared/components/navbar.component/navbar.component.ts
import { Component, HostListener } from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule, Router }    from '@angular/router';
import { LogoComponent }           from '../logo.component/logo.component';
import { MaterialSymbolComponent } from '../../layout/material-symbol.component/material-symbol.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    LogoComponent,
    MaterialSymbolComponent
  ],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  menuOpen     = false;
  userMenuOpen = false;
  isDesktop    = window.innerWidth >= 768;
  userImage    = 'https://via.placeholder.com/150';

  constructor(private router: Router) {}

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
    if (!this.menuOpen) {
      this.userMenuOpen = false;
    }
  }

  toggleUserMenu() {
    this.userMenuOpen = !this.userMenuOpen;
  }

  @HostListener('window:resize')
  onResize() {
    this.isDesktop = window.innerWidth >= 768;
    if (this.isDesktop) {
      this.menuOpen     = false;
      this.userMenuOpen = false;
    }
  }

  /** Navega para /board (lista de boards) */
  goToBoards() {
    this.router.navigate(['/board']);
    this.menuOpen = false;
  }

  /** Navega para /settings */
  goToSettings() {
    this.router.navigate(['/settings']);
    this.userMenuOpen = false;
    this.menuOpen     = false;
  }

  /** Apenas log de exemplo */
  signOut() {
    console.log('Fazendo logout...');
    this.userMenuOpen = false;
    this.menuOpen     = false;
  }
}
