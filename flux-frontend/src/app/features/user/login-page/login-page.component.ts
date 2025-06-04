import { Component, OnDestroy, signal, inject } from '@angular/core';
import { CommonModule }                          from '@angular/common';
import { FormsModule }                           from '@angular/forms';
import { Router, RouterModule }                  from '@angular/router';
import { AuthenticationService }                 from '../../../core/service/auth.service';
import { CookieService }                         from 'ngx-cookie-service';
import { Subscription }                          from 'rxjs';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule
  ],
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent implements OnDestroy {
  emailSignal     = signal('');
  passwordSignal  = signal('');
  loadingSignal   = signal(false);
  errorSignal     = signal<string | null>(null);

  private authService   = inject(AuthenticationService);
  private cookieService = inject(CookieService);
  private router        = inject(Router);

  private loginSub?: Subscription;

  constructor() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/board']);
    }
  }

  login(): void {
    this.errorSignal.set(null);

    const email    = this.emailSignal().trim();
    const password = this.passwordSignal();

    if (!email || !password) {
      this.errorSignal.set('Preencha email e senha.');
      return;
    }

    this.loadingSignal.set(true);

    this.loginSub = this.authService.login(email, password).subscribe({
      next: () => {
        this.loadingSignal.set(false);
        this.router.navigate(['/board']);
      },
      error: () => {
        this.loadingSignal.set(false);
        this.errorSignal.set('Email ou senha inválidos.');
      }
    });
  }

  ngOnDestroy(): void {
    this.loginSub?.unsubscribe();
  }
}
