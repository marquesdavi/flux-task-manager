import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import {AuthenticationService} from '../service/auth.service';

@Injectable({
    providedIn: 'root',
})
export class AuthGuard implements CanActivate {
    constructor(private authService: AuthenticationService, private router: Router) {}

    canActivate(): boolean {
        const isLoggedIn = this.authService.isLoggedIn();
        console.log('AuthGuard - isLoggedIn:', isLoggedIn);
        if (isLoggedIn) {
            return true;
        } else {
            this.router.navigate(['/login']);
            return false;
        }
    }
}
