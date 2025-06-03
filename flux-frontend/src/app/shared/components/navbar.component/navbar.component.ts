import {Component, HostListener, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Router} from '@angular/router';
import {LogoComponent} from '../logo.component/logo.component';
import {MaterialSymbolComponent} from '../../layout/material-symbol.component/material-symbol.component';
import {AuthenticationService} from '../../../core/service/auth.service';

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
    menuOpen = false;
    userMenuOpen = false;
    isDesktop = window.innerWidth >= 768;
    userImage = 'https://via.placeholder.com/150';

    private authService = inject(AuthenticationService);
    private router = inject(Router);

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
            this.menuOpen = false;
            this.userMenuOpen = false;
        }
    }

    goToBoards() {
        this.router.navigate(['/board']);
        this.menuOpen = false;
    }

    goToSettings() {
        this.router.navigate(['/settings']);
        this.userMenuOpen = false;
        this.menuOpen = false;
    }

    signOut() {
        this.authService.logout();
        this.userMenuOpen = false;
        this.menuOpen = false;
    }
}
