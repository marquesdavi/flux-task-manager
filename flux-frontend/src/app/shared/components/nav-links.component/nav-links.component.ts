import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-nav-links',
  standalone: true,
  templateUrl: './nav-links.component.html',
})
export class NavLinksComponent {
  @Input() links: { label: string; href: string }[] = [
    { label: 'Boards',   href: '#' },
    { label: 'Templates', href: '#' },
    { label: 'Home',     href: '#' },
  ];
}
