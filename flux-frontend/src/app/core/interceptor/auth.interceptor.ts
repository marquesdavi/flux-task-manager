import {
    HttpInterceptor,
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpErrorResponse
} from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthenticationService } from '../service/auth.service';
import { LoginModalService } from '../service/login-modal.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    private authService = inject(AuthenticationService);
    private loginModal  = inject(LoginModalService);

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = this.authService.token;
        const authReq = token
            ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
            : req;

        return next.handle(authReq).pipe(
            catchError((err: HttpErrorResponse) => {
                if (err.status === 401) {
                    this.loginModal.open();
                }
                return throwError(() => err);
            })
        );
    }
}
