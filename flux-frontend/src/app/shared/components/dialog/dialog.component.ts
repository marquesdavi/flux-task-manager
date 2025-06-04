import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DialogService } from '../../../core/service/dialog.service';

@Component({
    selector: 'app-dialog',
    standalone: true,
    templateUrl: './dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DialogComponent {
    protected dialogService = inject(DialogService);

    onOk() {
        this.dialogService.close();
    }

    onCancel() {
        this.dialogService.close(false);
    }

    onConfirm() {
        this.dialogService.close(true);
    }
}
