import {Component, OnDestroy, signal, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {UserService} from '../../../core/service/user.service';
import {AuthenticationService} from '../../../core/service/auth.service';
import {Subscription} from 'rxjs';
import {RegisterRequest} from '../../../core/models/user';

@Component({
    selector: 'app-register-page',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './register-page.component.html',
    styleUrls: ['./register-page.component.css']
})
export class RegisterPageComponent implements OnDestroy {
    // Em vez de “username”, agora temos firstName e lastName
    firstNameSignal = signal('');
    lastNameSignal = signal('');
    emailSignal = signal('');
    passwordSignal = signal('');
    confirmPasswordSignal = signal('');

    loadingSignal = signal(false);
    errorSignal = signal<string | null>(null);
    successSignal = signal<string | null>(null);

    private userService = inject(UserService);
    private authService = inject(AuthenticationService);
    private router = inject(Router);

    private registerSub?: Subscription;

    constructor() {
        if (this.authService.isLoggedIn()) {
            this.router.navigate(['/board']);
        }
    }

    register(): void {
        this.errorSignal.set(null);
        this.successSignal.set(null);

        const firstName = this.firstNameSignal().trim();
        const lastName = this.lastNameSignal().trim();
        const email = this.emailSignal().trim();
        const password = this.passwordSignal();
        const confirmPassword = this.confirmPasswordSignal();

        if (!firstName || !lastName || !email || !password) {
            this.errorSignal.set('Preencha todos os campos.');
            return;
        }
        if (password !== confirmPassword) {
            this.errorSignal.set('As senhas não conferem.');
            return;
        }

        this.loadingSignal.set(true);

        const payload: RegisterRequest = {
            firstName,
            lastName,
            email,
            password
        };

        this.registerSub = this.userService.register(payload).subscribe({
            next: () => {
                this.loadingSignal.set(false);
                this.successSignal.set(
                    'Conta criada com sucesso! Redirecionando para login...'
                );
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
