// src/app/features/login-page/login-page.component.ts
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
  // → Estados como Signals
  emailSignal     = signal('');
  passwordSignal  = signal('');
  loadingSignal   = signal(false);
  errorSignal     = signal<string | null>(null);

  // Injeção de dependência com inject()
  private authService   = inject(AuthenticationService);
  private cookieService = inject(CookieService);
  private router        = inject(Router);

  // Subscription para limpar após uso
  private loginSub?: Subscription;

  constructor() {
    // Se já estiver logado, redireciona
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/board']);
    }
  }

  login(): void {
    // Limpa qualquer erro anterior
    this.errorSignal.set(null);

    const email    = this.emailSignal().trim();
    const password = this.passwordSignal();

    // Validações básicas
    if (!email || !password) {
      this.errorSignal.set('Preencha email e senha.');
      return;
    }

    // Sinaliza loading
    this.loadingSignal.set(true);

    // Chama o serviço de autenticação
    this.loginSub = this.authService.login(email, password).subscribe({
      next: () => {
        // O AuthenticationService já grava o cookie internamente
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
