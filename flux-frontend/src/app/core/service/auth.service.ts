import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { Observable, tap } from 'rxjs';
import {environment} from '../../environments/environment';
import {TokenResponse} from '../models/auth';
import {DialogService} from './dialog.service';


@Injectable({ providedIn: 'root' })
export class AuthenticationService {
    constructor(
        private http: HttpClient,
        private cookieService: CookieService,
        private router: Router,
        private dialog: DialogService
    ) {}

    login(email: string, password: string): Observable<TokenResponse> {
        return this.http
            .post<TokenResponse>(`${environment.api}/auth/login`, { email, password })
            .pipe(
                tap(res => {
                    this.cookieService.set('accessToken', res.accessToken, { path: '/' });
                })
            );
    }

    async logout(): Promise<void> {
        if (await this.dialog.confirm('Ao sair, seus dados serão perdidos. Continuar?')) {
            this.cookieService.delete('accessToken', '/');
            await this.router.navigate(['/login']);
        }
    }

    get token(): string {
        return this.cookieService.get('accessToken') || '';
    }

    isLoggedIn(): boolean {
        return !!this.token;
    }
}
