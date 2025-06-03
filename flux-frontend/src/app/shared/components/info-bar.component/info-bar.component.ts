import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-info-bar',
  standalone: true,
  templateUrl: './info-bar.component.html',
  styleUrls: ['./info-bar.component.css']
})
export class InfoBarComponent {
  @Input() title = '';
}
