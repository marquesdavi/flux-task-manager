import {Component, Input, Output, EventEmitter} from '@angular/core';
import {CdkDrag} from '@angular/cdk/drag-drop';
import {MaterialSymbolComponent} from '../../layout/material-symbol.component/material-symbol.component';


@Component({
  selector: 'app-card',
  standalone: true,
  imports: [
    CdkDrag,
    MaterialSymbolComponent
  ],
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.css']
})
export class CardComponent {
  @Input() title = '';
  @Input() description: string | undefined = '';
  @Input() imageUrl?: string;
  @Output() edit = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
}
