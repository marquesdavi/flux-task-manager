import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-logo',
    standalone: true,
    templateUrl: './logo.component.html',
    styleUrls: ['./logo.component.css']
})
export class LogoComponent {
  @Input() name = 'Flux';
}
