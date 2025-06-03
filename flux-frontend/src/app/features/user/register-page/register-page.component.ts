// src/app/features/register-page/register-page.component.ts
import { Component, OnDestroy, signal, inject } from '@angular/core';
import { CommonModule }                          from '@angular/common';
import { FormsModule }                           from '@angular/forms';
import { Router, RouterModule }                  from '@angular/router';
import { UserService, RegisterRequest }          from '../../../core/service/user.service';
import { AuthenticationService }                 from '../../../core/service/auth.service';
import { Subscription }                          from 'rxjs';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register-page.component.html',
  styleUrls: ['./register-page.component.css']
})
export class RegisterPageComponent implements OnDestroy {
  // → Estados como Signals
  usernameSignal       = signal('');
  emailSignal          = signal('');
  passwordSignal       = signal('');
  confirmPasswordSignal = signal('');

  loadingSignal        = signal(false);
  errorSignal          = signal<string | null>(null);
  successSignal        = signal<string | null>(null);

  // Injeção de dependência via inject()
  private userService = inject(UserService);
  private authService = inject(AuthenticationService);
  private router      = inject(Router);

  private registerSub?: Subscription;

  constructor() {
    // Se já estiver logado, redireciona para /board
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/board']);
    }
  }

  register(): void {
    // Limpa mensagens anteriores
    this.errorSignal.set(null);
    this.successSignal.set(null);

    const username       = this.usernameSignal().trim();
    const email          = this.emailSignal().trim();
    const password       = this.passwordSignal();
    const confirmPassword = this.confirmPasswordSignal();

    // Validações básicas
    if (!username || !email || !password) {
      this.errorSignal.set('Preencha todos os campos.');
      return;
    }
    if (password !== confirmPassword) {
      this.errorSignal.set('As senhas não conferem.');
      return;
    }

    this.loadingSignal.set(true);

    const payload: RegisterRequest = { username, email, password };

    this.registerSub = this.userService.register(payload).subscribe({
      next: () => {
        this.loadingSignal.set(false);
        this.successSignal.set(
          'Conta criada com sucesso! Redirecionando para login...'
        );
        // Após 1 segundo, navega para /login
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1000);
      },
      error: () => {
        this.loadingSignal.set(false);
        this.errorSignal.set('Não foi possível registrar. Verifique os dados.');
      }
    });
  }

  ngOnDestroy(): void {
    this.registerSub?.unsubscribe();
  }
}
