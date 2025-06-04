import { Injectable, signal, WritableSignal } from '@angular/core';

interface AlertRequest {
    type: 'alert';
    message: string;
    resolve: () => void;
}

interface ConfirmRequest {
    type: 'confirm';
    message: string;
    resolve: (result: boolean) => void;
}

type DialogRequest = AlertRequest | ConfirmRequest;

@Injectable({ providedIn: 'root' })
export class DialogService {
    private dialogRequestSignal: WritableSignal<DialogRequest | null> = signal<DialogRequest | null>(null);

    readonly dialogRequest$ = this.dialogRequestSignal.asReadonly();

    alert(message: string): Promise<void> {
        return new Promise<void>((resolve) => {
            const req: AlertRequest = {
                type: 'alert',
                message,
                resolve: () => {
                    resolve();
                }
            };
            this.dialogRequestSignal.set(req);
        });
    }

    confirm(message: string): Promise<boolean> {
        return new Promise<boolean>((resolve) => {
            const req: ConfirmRequest = {
                type: 'confirm',
                message,
                resolve: (result: boolean) => {
                    resolve(result);
                }
            };
            this.dialogRequestSignal.set(req);
        });
    }

    close(result?: boolean): void {
        const current = this.dialogRequestSignal();
        if (!current) {
            return;
        }

        if (current.type === 'alert') {
            current.resolve();
        } else {
            current.resolve(result === true);
        }

        this.dialogRequestSignal.set(null);
    }
}
