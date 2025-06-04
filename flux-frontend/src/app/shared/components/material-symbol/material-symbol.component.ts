import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-material-symbol',
  standalone: true,
  host: {
    'class': 'material-symbols-outlined',
    'style.display': 'inline-flex',
    '[style.fontSize]': 'size',
    '[style.height]': 'size',
    '[style.width]': 'size',
    'style.lineHeight': '1',
    'style.alignItems': 'center',
    'style.justifyContent': 'center'
  },
  templateUrl: './material-symbol.component.html',
  styleUrls: ['./material-symbol.component.css']
})
export class MaterialSymbolComponent {
  @Input() size: string = '24px';
  @Input() color: string = '#93B0c8';
}
