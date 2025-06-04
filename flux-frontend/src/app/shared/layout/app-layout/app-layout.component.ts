import {Component, OnDestroy, inject} from '@angular/core';
import {Router, NavigationEnd, RouterModule} from '@angular/router';
import {filter, Subscription} from 'rxjs';
import {NavbarComponent} from '../navbar/navbar.component';
import {FooterComponent} from '../footer/footer.component';
import {DialogComponent} from '../../components/dialog/dialog.component';
import {LoginModalService} from '../../../core/service/login-modal.service';
import {CookieService} from 'ngx-cookie-service';

@Component({
    selector: 'app-layout',
    standalone: true,
    imports: [
        RouterModule,
        NavbarComponent,
        FooterComponent,
        DialogComponent
    ],
    templateUrl: './app-layout.component.html',
    styleUrls: ['./app-layout.component.css']
})
export class AppLayoutComponent implements OnDestroy {
    private router = inject(Router);
    protected loginModalS = inject(LoginModalService);
    private cookieService: CookieService = inject(CookieService);


    private routeSub: Subscription;
    showLoginModal = this.loginModalS.showModal;

    constructor() {
        this.routeSub = this.router.events
            .pipe(filter(e => e instanceof NavigationEnd))
            .subscribe(() => window.scrollTo(0, 0));
    }

    closeModalAndRedirect() {
        this.cookieService.delete('accessToken', '/');
        this.loginModalS.close();
        this.router.navigate(['/login']);
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
