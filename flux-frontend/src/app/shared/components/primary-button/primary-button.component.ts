import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-primary-button',
  standalone: true,
  templateUrl: './primary-button.component.html',
  styleUrls: ['./primary-button.component.css']
})
export class PrimaryButtonComponent {
  @Input() label = '';
  @Input() colorClass = 'bg-[#1989e5]';
  @Output() action = new EventEmitter<void>();
}
