import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoginModalService {
    showModal = signal(false);

    open() {
        this.showModal.set(true);
    }

    close() {
        this.showModal.set(false);
    }
}
