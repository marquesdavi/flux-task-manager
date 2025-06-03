// src/app/features/board-page/board-page.component.ts

import { Component, ElementRef, OnInit, QueryList, ViewChildren } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';

import { ColumnHeaderComponent }        from '../../../shared/components/column-header.component/column-header.component';
import { CardComponent }                from '../../../shared/components/card.component/card.component';
import { AddCardButtonComponent }       from '../../../shared/components/add-card-button.component/add-card-button.component';

import { CardService }                  from '../../../core/service/card.service';
import { CardCreateRequest, CardResponse, CardUpdateRequest } from '../../../core/models/card';

interface Task {
    id:               string;
    title:            string;
    descriptionBrief: string;
    descriptionFull:  string;
    imageUrl:         string | null;
    status:           'To Do' | 'Doing' | 'Done';
    startDate:        string;
    endDate:          string;
    dueDate:          string;
}

interface Column {
    title: 'To Do' | 'Doing' | 'Done';
    tasks: Task[];
}

@Component({
    selector: 'app-board-page',
    standalone: true,
    imports: [
        FormsModule,
        ColumnHeaderComponent,
        CardComponent,
        AddCardButtonComponent,
        DragDropModule
    ],
    templateUrl: './board-page.component.html',
    styleUrls: ['./board-page.component.css']
})
export class BoardPageComponent implements OnInit {
    columns: Column[] = [
        { title: 'To Do',  tasks: [] },
        { title: 'Doing', tasks: [] },
        { title: 'Done',  tasks: [] }
    ];

    showNewCardInput: boolean[] = [false, false, false];
    newCardTitle:     string[]  = ['', '', ''];
    newCardError:     string[]  = ['', '', ''];

    showModal: boolean            = false;
    modalTaskId: string | null    = null;
    modalData: CardResponse | null = null;
    modalError: string            = '';

    @ViewChildren('newCardInput') newCardInputs!: QueryList<ElementRef<HTMLInputElement>>;
    private boardId!: string;

    constructor(
        private cardService: CardService,
        private route: ActivatedRoute
    ) {}

    ngOnInit(): void {
        this.boardId = this.route.snapshot.paramMap.get('id')!;
        this.loadCards();
    }

    private loadCards(): void {
        this.cardService.getAllByBoard(this.boardId).subscribe({
            next: (cards: CardResponse[]) => {
                this.columns.forEach(col => col.tasks = []);
                cards.forEach(card => {
                    const task: Task = {
                        id:               card.id,
                        title:            card.title,
                        descriptionBrief: card.descriptionBrief,
                        descriptionFull:  card.descriptionFull,
                        imageUrl:         card.imageUrl,
                        status:           card.status,
                        startDate:        card.startDate,
                        endDate:          card.endDate,
                        dueDate:          card.dueDate
                    };
                    const col = this.columns.find(c => c.title === task.status);
                    if (col) { col.tasks.push(task); }
                });
            },
            error: () => {
                this.columns.forEach(col => col.tasks = []);
            }
        });
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

        const destinoArray  = event.container.data as Task[];
        const colunaDestino = this.columns.find(c => c.tasks === destinoArray);
        if (!colunaDestino) {
            return;
        }

        // Atualiza o status local e no servidor:
        dragged.status = colunaDestino.title;
        const updatePayload: CardUpdateRequest = {
            title:            dragged.title,
            status:           dragged.status,
            descriptionBrief: dragged.descriptionBrief,
            descriptionFull:  dragged.descriptionFull,
            startDate:        dragged.startDate,
            endDate:          dragged.endDate,
            dueDate:          dragged.dueDate,
            imageUrl:         dragged.imageUrl ?? undefined
        };

        this.cardService.update(dragged.id, updatePayload).subscribe({
            next: (updated: CardResponse) => {
                // Garante que o UI reflita exatamente a resposta do backend:
                const colunaAntiga = this.columns.find(c =>
                    c.tasks.some(t => t.id === updated.id)
                );
                if (colunaAntiga) {
                    const idxOld = colunaAntiga.tasks.findIndex(t => t.id === updated.id);
                    colunaAntiga.tasks.splice(idxOld, 1);
                }

                const novoStatusCol = this.columns.find(c => c.title === updated.status);
                if (novoStatusCol) {
                    const newTask: Task = {
                        id:               updated.id,
                        title:            updated.title,
                        descriptionBrief: updated.descriptionBrief,
                        descriptionFull:  updated.descriptionFull,
                        imageUrl:         updated.imageUrl,
                        status:           updated.status,
                        startDate:        updated.startDate,
                        endDate:          updated.endDate,
                        dueDate:          updated.dueDate
                    };
                    novoStatusCol.tasks.unshift(newTask);
                }
            },
            error: () => {
                this.loadCards();
            }
        });
    }

    onAddCardClick(colIndex: number): void {
        this.showNewCardInput[colIndex] = true;
        this.newCardTitle[colIndex]     = '';
        this.newCardError[colIndex]     = '';
        setTimeout(() => {
            const arr = this.newCardInputs.toArray();
            arr[colIndex]?.nativeElement.focus();
        });
    }

    onCreateCard(colIndex: number): void {
        const title = this.newCardTitle[colIndex].trim();
        if (!title) {
            this.newCardError[colIndex] = 'Preencha antes de criar';
            return;
        }
        const payload: CardCreateRequest = {
            title:            title,
            status:           this.columns[colIndex].title,
            descriptionBrief: '',
            descriptionFull:  '',
            startDate:        undefined,
            endDate:          undefined,
            dueDate:          undefined,
            imageUrl:         undefined
        };
        this.cardService.create(this.boardId, payload).subscribe({
            next: (created: CardResponse) => {
                const newTask: Task = {
                    id:               created.id,
                    title:            created.title,
                    descriptionBrief: created.descriptionBrief,
                    descriptionFull:  created.descriptionFull,
                    imageUrl:         created.imageUrl,
                    status:           created.status,
                    startDate:        created.startDate,
                    endDate:          created.endDate,
                    dueDate:          created.dueDate
                };
                this.columns[colIndex].tasks.unshift(newTask);
                this.showNewCardInput[colIndex] = false;
                this.newCardTitle[colIndex]     = '';
                this.newCardError[colIndex]     = '';
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
        this.newCardTitle[colIndex]     = '';
        this.newCardError[colIndex]     = '';
    }

    openModal(taskId: string): void {
        this.modalError  = '';
        this.modalData   = null;
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
        this.showModal   = false;
        this.modalTaskId = null;
        this.modalData   = null;
        this.modalError  = '';
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
            title:            this.modalData.title,
            descriptionBrief: this.modalData.descriptionBrief,
            descriptionFull:  this.modalData.descriptionFull,
            status:           this.modalData.status,
            startDate:        this.modalData.startDate,
            endDate:          this.modalData.endDate,
            dueDate:          this.modalData.dueDate,
            imageUrl:         this.modalData.imageUrl ?? undefined
        };
        this.cardService.update(this.modalTaskId, payload).subscribe({
            next: (updated: CardResponse) => {
                const colunaAntiga = this.columns.find(c =>
                    c.tasks.some(t => t.id === updated.id)
                );
                if (colunaAntiga) {
                    const idxOld = colunaAntiga.tasks.findIndex(t => t.id === updated.id);
                    colunaAntiga.tasks.splice(idxOld, 1);
                }
                const colDestino = this.columns.find(c => c.title === updated.status);
                if (colDestino) {
                    const newTask: Task = {
                        id:               updated.id,
                        title:            updated.title,
                        descriptionBrief: updated.descriptionBrief,
                        descriptionFull:  updated.descriptionFull,
                        imageUrl:         updated.imageUrl,
                        status:           updated.status,
                        startDate:        updated.startDate,
                        endDate:          updated.endDate,
                        dueDate:          updated.dueDate
                    };
                    colDestino.tasks.unshift(newTask);
                }
                this.closeModal();
            },
            error: () => {
                this.modalError = 'Não foi possível salvar as alterações.';
            }
        });
    }

    onAction(label: string): void {
        console.log('[onAction] argumento:', label);
    }
}
