import { Component, Input } from '@angular/core';
import { NgClass }          from '@angular/common';

@Component({
  selector: 'app-avatar',
  standalone: true,
  imports: [ NgClass ],
  templateUrl: './avatar.component.html',
  styleUrls: ['./avatar.component.css']
})
export class AvatarComponent {
  @Input() imageUrl = '';
  @Input() size     = '10';
}
