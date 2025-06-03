import {Component, OnDestroy, signal, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterModule} from '@angular/router';
import {UserService} from '../../../core/service/user.service';
import {AuthenticationService} from '../../../core/service/auth.service';
import {Subscription} from 'rxjs';
import {RegisterRequest, User} from '../../../core/models/user';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.css'],
})
export class ProfilePageComponent implements OnDestroy {
  firstNameSignal = signal('');
  lastNameSignal = signal('');
  emailSignal = signal('');
  passwordSignal = signal('');

  loadingSignal = signal(false);
  errorSignal = signal<string | null>(null);

  private userService = inject(UserService);
  private authService = inject(AuthenticationService);
  private router = inject(Router);

  private fetchSub?: Subscription;
  private saveSub?: Subscription;
  private deleteSub?: Subscription;

  constructor() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
    } else {
      this.loadUserData();
    }
  }

  private loadUserData(): void {
    this.loadingSignal.set(true);
    this.fetchSub = this.userService.getCurrent().subscribe({
      next: (user: User) => {

        this.firstNameSignal.set(user.firstName);
        this.lastNameSignal.set(user.lastName);
        this.emailSignal.set(user.email);
        this.loadingSignal.set(false);
      },
      error: () => {
        this.errorSignal.set('Não foi possível carregar seus dados.');
        this.loadingSignal.set(false);
      }
    });
  }

  private isFormValid(): boolean {
    const firstName = this.firstNameSignal().trim();
    const lastName = this.lastNameSignal().trim();
    const email = this.emailSignal().trim();

    if (!firstName) {
      this.errorSignal.set('Nome é obrigatório.');
      return false;
    }
    if (!lastName) {
      this.errorSignal.set('Sobrenome é obrigatório.');
      return false;
    }
    if (!email) {
      this.errorSignal.set('Email é obrigatório.');
      return false;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      this.errorSignal.set('Informe um email válido.');
      return false;
    }
    return true;
  }

  save(): void {
    this.errorSignal.set(null);

    if (!this.isFormValid()) {
      return;
    }

    this.loadingSignal.set(true);

    const payload: RegisterRequest = {
      firstName: this.firstNameSignal().trim(),
      lastName: this.lastNameSignal().trim(),
      email: this.emailSignal().trim(),
      password: this.passwordSignal().trim() || ''
    };

    this.saveSub = this.userService.updateCurrent(payload).subscribe({
      next: () => {
        this.loadingSignal.set(false);
        alert('Dados atualizados com sucesso.');
      },
      error: () => {
        this.errorSignal.set('Não foi possível salvar suas alterações.');
        this.loadingSignal.set(false);
      }
    });
  }

  deleteAccount(): void {
    if (!confirm('Tem certeza de que deseja deletar sua conta? Esta ação não pode ser desfeita.')) {
      return;
    }

    this.loadingSignal.set(true);
    this.deleteSub = this.userService.deleteCurrent().subscribe({
      next: () => {
        this.loadingSignal.set(false);
        this.authService.logout();
        this.router.navigate(['/login']);
      },
      error: () => {
        this.errorSignal.set('Não foi possível deletar sua conta.');
        this.loadingSignal.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    this.fetchSub?.unsubscribe();
    this.saveSub?.unsubscribe();
    this.deleteSub?.unsubscribe();
  }
}
