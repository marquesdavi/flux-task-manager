import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterModule} from '@angular/router';
import {BoardService} from '../../../core/service/board.service';
import {Board} from '../../../core/models/board';
import {FormsModule} from '@angular/forms';
import {MaterialSymbolComponent} from '../../../shared/components/material-symbol/material-symbol.component';
import {DialogService} from '../../../core/service/dialog.service';

@Component({
    selector: 'app-boards-page',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        MaterialSymbolComponent
    ],
    templateUrl: './board-management-page.component.html',
    styleUrls: ['./board-management-page.component.css']
})
export class BoardManagementPageComponent {
    private boardService = inject(BoardService);
    private router = inject(Router);
    private dialogService: DialogService = inject(DialogService);

    boardsSignal = signal<Board[]>([]);
    loadingSignal = signal(true);

    showNewInputSignal = signal(false);
    newBoardTitleSignal = signal('');
    newBoardErrorSignal = signal('');

    editingIdSignal = signal<string | null>(null);
    editTitleSignal = signal('');
    editErrorSignal = signal('');

    constructor() {
        this.loadBoards();
    }

    private loadBoards(): void {
        this.loadingSignal.set(true);
        this.boardService.getAll().subscribe({
            next: (list: Board[]) => {
                this.boardsSignal.set(list);
                this.loadingSignal.set(false);
            },
            error: () => {
                this.boardsSignal.set([]);
                this.loadingSignal.set(false);
            }
        });
    }

    openNewBoardInput(): void {
        this.newBoardTitleSignal.set('');
        this.newBoardErrorSignal.set('');
        this.showNewInputSignal.set(true);

        setTimeout(() => {
            const input = document.querySelector<HTMLInputElement>('#newBoardInput');
            input?.focus();
        });
    }

    cancelNewBoard(): void {
        this.showNewInputSignal.set(false);
        this.newBoardTitleSignal.set('');
        this.newBoardErrorSignal.set('');
    }

    private validateNewTitle(): boolean {
        const title = this.newBoardTitleSignal().trim();
        if (!title) {
            this.newBoardErrorSignal.set('Informe um nome.');
            return false;
        }
        return true;
    }

    createBoard(): void {
        this.newBoardErrorSignal.set('');
        if (!this.validateNewTitle()) return;

        const title = this.newBoardTitleSignal().trim();
        this.boardService.create(title).subscribe({
            next: (created: Board) => {
                this.boardsSignal.update(arr => {
                    arr.unshift(created);
                    return arr;
                });
                this.cancelNewBoard();
            },
            error: () => {
                this.newBoardErrorSignal.set('Falha ao criar board.');
            }
        });
    }

    onNewBoardKeyDown(event: KeyboardEvent): void {
        if (event.key === 'Enter') {
            this.createBoard();
        } else if (event.key === 'Escape') {
            this.cancelNewBoard();
        }
    }

    startEditing(board: Board): void {
        this.editingIdSignal.set(board.id);
        this.editTitleSignal.set(board.title);
        this.editErrorSignal.set('');

        setTimeout(() => {
            const input = document.querySelector<HTMLInputElement>(`#editBoardInput-${board.id}`);
            input?.focus();
        });
    }

    cancelEditing(): void {
        this.editingIdSignal.set(null);
        this.editTitleSignal.set('');
        this.editErrorSignal.set('');
    }

    private validateEditTitle(): boolean {
        const title = this.editTitleSignal().trim();
        if (!title) {
            this.editErrorSignal.set('Título não pode ficar vazio.');
            return false;
        }
        return true;
    }

    saveEdit(board: Board): void {
        this.editErrorSignal.set('');
        if (!this.validateEditTitle()) return;

        const newTitle = this.editTitleSignal().trim();
        this.boardService.rename(board.id, newTitle).subscribe({
            next: (updated: Board) => {
                this.boardsSignal.update(arr => {
                    const idx = arr.findIndex(b => b.id === updated.id);
                    if (idx > -1) arr[idx] = updated;
                    return arr;
                });
                this.cancelEditing();
            },
            error: () => {
                this.editErrorSignal.set('Falha ao renomear.');
            }
        });
    }

    onEditKeyDown(event: KeyboardEvent, board: Board): void {
        if (event.key === 'Enter') {
            this.saveEdit(board);
        } else if (event.key === 'Escape') {
            this.cancelEditing();
        }
    }

    async deleteBoard(id: string): Promise<void> {
        if (!await this.dialogService.confirm('Deseja realmente excluir este board?')) {
            return;
        }

        this.boardService.delete(id).subscribe({
            next: () => {
                this.boardsSignal.update(arr => {
                    const idx = arr.findIndex(b => b.id === id);
                    if (idx > -1) arr.splice(idx, 1);
                    return arr;
                });
                if (this.editingIdSignal() === id) {
                    this.cancelEditing();
                }
            },
            error: () => {
                this.dialogService.alert('Falha ao excluir board.');
            }
        });
    }

    goToBoard(board: Board): void {
        this.router.navigate(['/boards', board.id]);
    }
}
