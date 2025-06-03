import {
  Component,
  OnDestroy,
  Renderer2,
  ViewChild
} from '@angular/core';
import {
  NavigationEnd,
  Router,
  RouterModule
} from '@angular/router';
import { filter, Subscription } from 'rxjs';
import {NavbarComponent} from '../../components/navbar.component/navbar.component';
import {FooterComponent} from '../../components/footer.component/footer.component';


@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    RouterModule,
    NavbarComponent,
    FooterComponent
  ],
  templateUrl: './app-layout.component.html',
  styleUrls: ['./app-layout.component.css']
})
export class AppLayoutComponent implements OnDestroy {
  private routeSub: Subscription;
  @ViewChild('layoutWrapper', { read: null, static: true }) wrapper!: any;

  constructor(
    private router: Router,
    private renderer: Renderer2
  ) {
    // Sempre que uma rota finalizar, rolar pro topo e (opcional) fechar menus/overlays
    this.routeSub = this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => {
        window.scrollTo(0, 0);
      });
  }

  ngOnDestroy() {
    this.routeSub.unsubscribe();
  }
}
