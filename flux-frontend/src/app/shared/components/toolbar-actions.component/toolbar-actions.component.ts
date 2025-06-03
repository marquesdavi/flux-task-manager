import { Component, Input, Output, EventEmitter } from '@angular/core';
import {PrimaryButtonComponent} from '../primary-button.component/primary-button.component';

@Component({
  selector: 'app-toolbar-actions',
  standalone: true,
  imports: [PrimaryButtonComponent],
  templateUrl: './toolbar-actions.component.html',
})
export class ToolbarActionsComponent {
  @Input() actions: { label: string; color: string }[] = [];
  @Output() actionClick = new EventEmitter<string>();
}
