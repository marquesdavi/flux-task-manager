import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-column-header',
  standalone: true,
  templateUrl: './column-header.component.html',
  styleUrls: ['./column-header.component.css']
})
export class ColumnHeaderComponent {
  @Input() title = '';
}
