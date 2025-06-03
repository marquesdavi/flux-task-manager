import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { Observable, tap } from 'rxjs';
import {environment} from '../../environments/environment.development';

interface LoginRequest { email: string; password: string; }
interface TokenResponse { token: string; }

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  constructor(
    private http: HttpClient,
    private cookieService: CookieService,
    private router: Router
  ) {}

  login(email: string, password: string): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${environment.api}/auth`, { email, password })
      .pipe(
        tap(res => {
          this.cookieService.set('accessToken', res.token, { path: '/' });
        })
      );
  }

  logout(): void {
    if (confirm('Ao sair, seus dados serão perdidos. Continuar?')) {
      this.cookieService.delete('accessToken', '/');
      this.router.navigate(['/login']);
    }
  }

  get token(): string {
    return this.cookieService.get('accessToken') || '';
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }
}
