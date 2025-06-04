import { Component, HostListener, inject, signal } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { LogoComponent } from '../../components/logo/logo.component';
import { MaterialSymbolComponent } from '../../components/material-symbol/material-symbol.component';
import { AuthenticationService } from '../../../core/service/auth.service';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [
        RouterModule,
        LogoComponent,
        MaterialSymbolComponent
    ],
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
    private menuOpenSignal     = signal(false);
    private userMenuOpenSignal = signal(false);
    private isDesktopSignal    = signal(window.innerWidth >= 768);

    readonly menuOpen     = this.menuOpenSignal.asReadonly();
    readonly userMenuOpen = this.userMenuOpenSignal.asReadonly();
    readonly isDesktop    = this.isDesktopSignal.asReadonly();

    private authService = inject(AuthenticationService);
    private router      = inject(Router);

    toggleMenu() {
        this.menuOpenSignal.update(v => !v);
        if (!this.menuOpenSignal()) {
            this.userMenuOpenSignal.set(false);
        }
    }

    toggleUserMenu() {
        this.userMenuOpenSignal.update(v => !v);
    }

    @HostListener('window:resize')
    onResize() {
        const desktop = window.innerWidth >= 768;
        this.isDesktopSignal.set(desktop);
        if (desktop) {
            this.menuOpenSignal.set(false);
            this.userMenuOpenSignal.set(false);
        }
    }

    goToBoards() {
        this.router.navigate(['/board']);
        this.menuOpenSignal.set(false);
    }

    goToSettings() {
        this.router.navigate(['/settings']);
        this.userMenuOpenSignal.set(false);
        this.menuOpenSignal.set(false);
    }

    signOut() {
        this.authService.logout();
        this.userMenuOpenSignal.set(false);
        this.menuOpenSignal.set(false);
    }
}
