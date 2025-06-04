import { Component, Input, Output, EventEmitter } from '@angular/core';
import {MaterialSymbolComponent} from '../material-symbol/material-symbol.component';
import {NgStyle} from '@angular/common';

@Component({
  selector: 'app-add-card-button',
  standalone: true,
  imports: [
    MaterialSymbolComponent,
    NgStyle
  ],
  templateUrl: './add-card-button.component.html',
  styleUrls: ['./add-card-button.component.css']
})
export class AddCardButtonComponent {
  @Input() label = '+ Add a card';
  @Input() color: string = '#93B0c8';
  @Input() bgColor: string = '#243747';
  @Input() hoverBgColor: string = '#1a2b38';
  @Output() clickAdd = new EventEmitter<void>();

  isHovered = false;
}
