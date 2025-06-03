import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import {AuthenticationService} from '../service/auth.service';

@Injectable({
    providedIn: 'root',
})
export class LoginGuard implements CanActivate {
    constructor(
        private authService: AuthenticationService,
        private router: Router
    ) {}

    canActivate(): boolean {
        const isLoggedIn = this.authService.isLoggedIn();
        console.log('LoginGuard - isLoggedIn:', isLoggedIn);
        if (isLoggedIn) {
            this.router.navigate(['/']);
            return false;
        } else {
            return true;
        }
    }
}
