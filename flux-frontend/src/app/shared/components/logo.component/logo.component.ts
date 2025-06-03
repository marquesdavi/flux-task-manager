import { Component, Input } from '@angular/core';
import {MaterialSymbolComponent} from "../../layout/material-symbol.component/material-symbol.component";

@Component({
    selector: 'app-logo',
    standalone: true,
    templateUrl: './logo.component.html',
    imports: [
        MaterialSymbolComponent
    ],
    styleUrls: ['./logo.component.css']
})
export class LogoComponent {
  @Input() name = 'Flux';
}
