import { Component, Input, Output, EventEmitter } from '@angular/core';
import {NgOptimizedImage} from '@angular/common';
import {MaterialSymbolComponent} from '../material-symbol/material-symbol.component';

@Component({
    selector: 'app-card',
    standalone: true,
    imports: [MaterialSymbolComponent, NgOptimizedImage],
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.css']
})
export class CardComponent {
    @Input() title = '';
    @Input() description: string | undefined = '';
    @Input({ transform: (v: string | null) => v }) imageUrl?: string;

    @Output() edit = new EventEmitter<void>();
    @Output() delete = new EventEmitter<void>();

    @Output() clickCard = new EventEmitter<void>();

    onDeleteClick() {
        this.delete.emit();
    }
    onEditClick() {
        this.edit.emit();
    }
    onCardClick() {
        this.clickCard.emit();
    }
}
