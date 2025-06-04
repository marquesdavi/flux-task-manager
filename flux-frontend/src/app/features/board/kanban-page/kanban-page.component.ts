import { Component, ElementRef, OnInit, QueryList, ViewChildren } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';

import { ColumnHeaderComponent } from '../../../shared/components/column-header/column-header.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { AddCardButtonComponent } from '../../../shared/components/add-card-button/add-card-button.component';

import { CardService } from '../../../core/service/card.service';
import { CardCreateRequest, CardResponse, CardUpdateRequest } from '../../../core/models/card';
import {DialogService} from '../../../core/service/dialog.service';

interface Task {
    id: string;
    title: string;
    descriptionBrief: string;
    descriptionFull: string;
    imageUrl: string | null;
    status: 'TODO' | 'DOING' | 'DONE';
    startDate: string;
    endDate: string;
    dueDate: string;
}

interface Column {
    title: 'TODO' | 'DOING' | 'DONE';
    tasks: Task[];
}

@Component({
    selector: 'app-kanban-page',
    standalone: true,
    imports: [
        FormsModule,
        ColumnHeaderComponent,
        CardComponent,
        AddCardButtonComponent,
        DragDropModule
    ],
    templateUrl: './kanban-page.component.html',
    styleUrls: ['./kanban-page.component.css']
})
export class KanbanPageComponent implements OnInit {
    columns: Column[] = [
        { title: 'TODO',  tasks: [] },
        { title: 'DOING', tasks: [] },
        { title: 'DONE',  tasks: [] }
    ];

    showNewCardInput: boolean[] = [false, false, false];
    newCardTitle: string[] = ['', '', ''];
    newCardError: string[] = ['', '', ''];

    showModal: boolean = false;
    modalTaskId: string | null = null;
    modalData: CardResponse | null = null;
    modalError: string = '';

    @ViewChildren('newCardInput') newCardInputs!: QueryList<ElementRef<HTMLInputElement>>;
    private boardId!: string;

    constructor(
        private cardService: CardService,
        private route: ActivatedRoute,
        private dialogService: DialogService
    ) {}

    ngOnInit(): void {
        this.boardId = this.route.snapshot.paramMap.get('id')!;
        this.loadCards();
    }

    private loadCards(): void {
        this.cardService.getAllByBoard(this.boardId).subscribe({
            next: (cards: CardResponse[]) => {
                this.columns.forEach(col => (col.tasks = []));
                cards.forEach(card => {
                    const task: Task = {
                        id: card.id,
                        title: card.title,
                        descriptionBrief: card.descriptionBrief,
                        descriptionFull: card.descriptionFull,
                        imageUrl: card.imageUrl,
                        status: card.status as 'TODO' | 'DOING' | 'DONE',
                        startDate: card.startDate,
                        endDate: card.endDate,
                        dueDate: card.dueDate
                    };
                    const col = this.columns.find(c => c.title === task.status);
                    if (col) {
                        col.tasks.push(task);
                    }
                });
            },
            error: () => {
                this.columns.forEach(col => (col.tasks = []));
            }
        });
    }

    formatStatusDisplay(status: 'TODO' | 'DOING' | 'DONE'): string {
        switch (status) {
            case 'TODO':   return 'A fazer';
            case 'DOING':  return 'Em andamento';
            case 'DONE':   return 'Pronto';
        }
    }


    drop(event: CdkDragDrop<Task[]>): void {
        const dragged = event.item.data as Task;

        if (event.previousContainer === event.container) {
            moveItemInArray(
                event.container.data,
                event.previousIndex,
                event.currentIndex
            );
            return;
        }

        transferArrayItem(
            event.previousContainer.data,
            event.container.data,
            event.previousIndex,
            event.currentIndex
        );

        const destinationArray = event.container.data as Task[];
        const destinationColumn = this.columns.find(c => c.tasks === destinationArray);
        if (!destinationColumn) {
            return;
        }

        dragged.status = destinationColumn.title;
        const updatePayload: CardUpdateRequest = {
            title: dragged.title,
            status: dragged.status,
            descriptionBrief: dragged.descriptionBrief,
            descriptionFull: dragged.descriptionFull,
            startDate: dragged.startDate,
            endDate: dragged.endDate,
            dueDate: dragged.dueDate,
            imageUrl: dragged.imageUrl ?? undefined
        };


        this.cardService.update(dragged.id, updatePayload).subscribe({
            next: (updated: CardResponse) => {
                const previousColumn = this.columns.find(c =>
                    c.tasks.some(t => t.id === updated.id)
                );
                if (previousColumn) {
                    const idxOld = previousColumn.tasks.findIndex(t => t.id === updated.id);
                    previousColumn.tasks.splice(idxOld, 1);
                }

                const newStatusColumn = this.columns.find(c => c.title === updated.status);
                if (newStatusColumn) {
                    const newTask: Task = {
                        id: updated.id,
                        title: updated.title,
                        descriptionBrief: updated.descriptionBrief,
                        descriptionFull: updated.descriptionFull,
                        imageUrl: updated.imageUrl,
                        status: updated.status,
                        startDate: updated.startDate,
                        endDate: updated.endDate,
                        dueDate: updated.dueDate
                    };
                    newStatusColumn.tasks.unshift(newTask);
                }
            },
            error: () => {
                this.loadCards();
            }
        });
    }

    onAddCardClick(colIndex: number): void {
        this.showNewCardInput[colIndex] = true;
        this.newCardTitle[colIndex] = '';
        this.newCardError[colIndex] = '';
        setTimeout(() => {
            const inputs = this.newCardInputs.toArray();
            inputs[colIndex]?.nativeElement.focus();
        });
    }

    onCreateCard(colIndex: number): void {
        const title = this.newCardTitle[colIndex].trim();
        if (!title) {
            this.newCardError[colIndex] = 'Preencha antes de criar';
            return;
        }
        const payload: CardCreateRequest = {
            title: title,
            status: this.columns[colIndex].title,
            descriptionBrief: '',
            descriptionFull: '',
            startDate: undefined,
            endDate: undefined,
            dueDate: undefined,
            imageUrl: undefined
        };

        this.cardService.create(this.boardId, payload).subscribe({
            next: (created: CardResponse) => {
                const newTask: Task = {
                    id: created.id,
                    title: created.title,
                    descriptionBrief: created.descriptionBrief,
                    descriptionFull: created.descriptionFull,
                    imageUrl: created.imageUrl,
                    status: created.status,
                    startDate: created.startDate,
                    endDate: created.endDate,
                    dueDate: created.dueDate
                };
                this.columns[colIndex].tasks.unshift(newTask);
                this.showNewCardInput[colIndex] = false;
                this.newCardTitle[colIndex] = '';
                this.newCardError[colIndex] = '';
            },
            error: () => {
                this.newCardError[colIndex] = 'Não foi possível criar card.';
            }
        });
    }

    trackById(index: number, item: Task) {
        return item.id;
    }

    onCardInputKeyDown(event: KeyboardEvent, colIndex: number): void {
        if (event.key === 'Enter') {
            this.onCreateCard(colIndex);
        } else if (event.key === 'Escape') {
            this.onCancelCard(colIndex);
        }
    }

    onCancelCard(colIndex: number): void {
        this.showNewCardInput[colIndex] = false;
        this.newCardTitle[colIndex] = '';
        this.newCardError[colIndex] = '';
    }

    openModal(taskId: string): void {
        this.modalError = '';
        this.modalData = null;
        this.modalTaskId = taskId;
        this.cardService.getById(taskId).subscribe({
            next: (resp: CardResponse) => {
                this.modalData = resp;
                this.showModal = true;
            },
            error: () => {
                this.modalError = 'Não foi possível carregar dados do card.';
                this.showModal = false;
            }
        });
    }

    closeModal(): void {
        this.showModal = false;
        this.modalTaskId = null;
        this.modalData = null;
        this.modalError = '';
    }

    saveModal(): void {
        if (!this.modalTaskId || !this.modalData) {
            return;
        }
        if (!this.modalData.title.trim()) {
            this.modalError = 'Título não pode ficar vazio.';
            return;
        }
        const payload: CardUpdateRequest = {
            title: this.modalData.title,
            descriptionBrief: this.modalData.descriptionBrief,
            descriptionFull: this.modalData.descriptionFull,
            status: this.modalData.status,
            startDate: this.modalData.startDate,
            endDate: this.modalData.endDate,
            dueDate: this.modalData.dueDate,
            imageUrl: this.modalData.imageUrl ?? undefined
        };
        this.cardService.update(this.modalTaskId, payload).subscribe({
            next: (updated: CardResponse) => {
                const previousColumn = this.columns.find(c =>
                    c.tasks.some(t => t.id === updated.id)
                );
                if (previousColumn) {
                    const idxOld = previousColumn.tasks.findIndex(t => t.id === updated.id);
                    previousColumn.tasks.splice(idxOld, 1);
                }
                const destinationColumn = this.columns.find(c => c.title === updated.status);
                if (destinationColumn) {
                    const newTask: Task = {
                        id: updated.id,
                        title: updated.title,
                        descriptionBrief: updated.descriptionBrief,
                        descriptionFull: updated.descriptionFull,
                        imageUrl: updated.imageUrl,
                        status: updated.status,
                        startDate: updated.startDate,
                        endDate: updated.endDate,
                        dueDate: updated.dueDate
                    };
                    destinationColumn.tasks.unshift(newTask);
                }
                this.closeModal();
            },
            error: () => {
                this.modalError = 'Não foi possível salvar as alterações.';
            }
        });
    }

    onAction(label: string): void {
        console.log('[onAction] argument:', label);
    }

    async deleteCard(cardId: string) {
        if (!await this.dialogService.confirm('Você tem certeza que deseja excluir este card?')) {
            return;
        }

        this.cardService.delete(cardId).subscribe({
            next: () => {
                this.columns.forEach(col => {
                    const idx = col.tasks.findIndex(t => t.id === cardId);
                    if (idx > -1) {
                        col.tasks.splice(idx, 1);
                    }
                });
            },
            error: () => {
                this.dialogService.alert('Falha ao excluir card.');
            }
        });
    }
}
