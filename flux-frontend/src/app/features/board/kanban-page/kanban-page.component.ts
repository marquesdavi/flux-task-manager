import {
    Component,
    ElementRef,
    OnInit,
    QueryList,
    ViewChildren
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {
    DragDropModule,
    CdkDragDrop,
    moveItemInArray,
    transferArrayItem
} from '@angular/cdk/drag-drop';

import { ColumnHeaderComponent } from '../../../shared/components/column-header/column-header.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { AddCardButtonComponent } from '../../../shared/components/add-card-button/add-card-button.component';

import { CardService } from '../../../core/service/card.service';
import {
    CardCreateRequest,
    CardResponse,
    CardUpdateRequest,
    CardAssignRequest
} from '../../../core/models/card';
import { DialogService } from '../../../core/service/dialog.service';
import { UserSummary } from '../../../core/models/user';
import { UserService } from '../../../core/service/user.service';

interface Task {
    id: string;
    title: string;
    descriptionBrief: string;
    descriptionFull: string;
    imageUrl: string | null;
    status: 'TODO' | 'DOING' | 'DONE';
    priority: 'LOW' | 'MEDIUM' | 'HIGH' | null;
    assigneeEmail: string | null;
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
        { title: 'TODO', tasks: [] },
        { title: 'DOING', tasks: [] },
        { title: 'DONE', tasks: [] }
    ];

    showNewCardInput: boolean[] = [false, false, false];
    newCardTitle: string[] = ['', '', ''];
    newCardError: string[] = ['', '', ''];

    showModal: boolean = false;
    modalTaskId: string | null = null;
    modalData: CardResponse | null = null;
    modalError: string = '';

    assigneeEmailForAssign: string = '';
    assignError: string = '';

    collaborators: UserSummary[] = [];

    filterTitle: string = '';
    filterAssignee: string = '';
    filterStatus: string = '';
    filterPriority: string = '';

    private allTasks: Task[] = [];

    @ViewChildren('newCardInput') newCardInputs!: QueryList<ElementRef<HTMLInputElement>>;
    private boardId!: string;

    constructor(
        private cardService: CardService,
        private route: ActivatedRoute,
        private userService: UserService,
        private dialogService: DialogService
    ) {}

    ngOnInit(): void {
        this.boardId = this.route.snapshot.paramMap.get('id')!;
        this.loadCollaborators();

        this.loadCards();
    }

    private loadCollaborators(): void {
        this.userService.getUsersByBoardId(this.boardId).subscribe({
            next: (users: UserSummary[]) => {
                this.collaborators = users;
            },
            error: () => {
                this.collaborators = [];
            }
        });
    }

    private loadCards(): void {
        this.cardService.getAllByBoard(this.boardId).subscribe({
            next: (cards: CardResponse[]) => {
                this.allTasks = cards.map(card => ({
                    id: card.id,
                    title: card.title,
                    descriptionBrief: card.descriptionBrief,
                    descriptionFull: card.descriptionFull,
                    imageUrl: card.imageUrl,
                    status: card.status as 'TODO' | 'DOING' | 'DONE',
                    priority: card.priority as 'LOW' | 'MEDIUM' | 'HIGH' | null,
                    assigneeEmail: card.assigneeEmail,
                    startDate: card.startDate,
                    endDate: card.endDate,
                    dueDate: card.dueDate
                }));

                this.applyFilters();
            },
            error: () => {
                this.allTasks = [];
                this.columns.forEach(col => (col.tasks = []));
            }
        });
    }

    applyFilters(): void {
        this.columns.forEach(col => (col.tasks = []));
        const titleFilter = this.filterTitle.trim().toLowerCase();

        this.allTasks.forEach(task => {
            if (titleFilter && !task.title.toLowerCase().includes(titleFilter)) {
                return;
            }
            if (this.filterAssignee && task.assigneeEmail !== this.filterAssignee) {
                return;
            }
            if (this.filterStatus && task.status !== this.filterStatus) {
                return;
            }
            if (this.filterPriority && task.priority !== this.filterPriority) {
                return;
            }
            const col = this.columns.find(c => c.title === task.status);
            if (col) {
                col.tasks.push(task);
            }
        });
    }


    formatStatusDisplay(status: 'TODO' | 'DOING' | 'DONE'): string {
        switch (status) {
            case 'TODO':
                return 'A fazer';
            case 'DOING':
                return 'Em andamento';
            case 'DONE':
                return 'Pronto';
        }
    }

    drop(event: CdkDragDrop<Task[]>): void {
        const dragged = event.item.data as Task;
        if (event.previousContainer === event.container) {
            moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
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
            priority: dragged.priority!,
            endDate: dragged.endDate,
            dueDate: dragged.dueDate,
            imageUrl: dragged.imageUrl ?? undefined
        };
        this.cardService.update(dragged.id, updatePayload).subscribe({
            next: (updated: CardResponse) => {
                const idxAll = this.allTasks.findIndex(t => t.id === updated.id);
                if (idxAll > -1) {
                    this.allTasks[idxAll] = {
                        id: updated.id,
                        title: updated.title,
                        descriptionBrief: updated.descriptionBrief,
                        descriptionFull: updated.descriptionFull,
                        imageUrl: updated.imageUrl,
                        status: updated.status as 'TODO' | 'DOING' | 'DONE',
                        priority: updated.priority as 'LOW' | 'MEDIUM' | 'HIGH' | null,
                        assigneeEmail: updated.assigneeEmail,
                        startDate: updated.startDate,
                        endDate: updated.endDate,
                        dueDate: updated.dueDate
                    };
                }
                this.applyFilters();
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
                    status: created.status as 'TODO' | 'DOING' | 'DONE',
                    priority: created.priority as 'LOW' | 'MEDIUM' | 'HIGH' | null,
                    assigneeEmail: created.assigneeEmail,
                    startDate: created.startDate,
                    endDate: created.endDate,
                    dueDate: created.dueDate
                };

                this.allTasks.unshift(newTask);
                this.applyFilters();

                this.showNewCardInput[colIndex] = false;
                this.newCardTitle[colIndex] = '';
                this.newCardError[colIndex] = '';
            },
            error: () => {
                this.newCardError[colIndex] = 'Não foi possível criar card.';
            }
        });
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
                this.assigneeEmailForAssign = resp.assigneeEmail || '';
                this.assignError = '';
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
        this.assigneeEmailForAssign = '';
        this.assignError = '';
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
            priority: this.modalData.priority as 'LOW' | 'MEDIUM' | 'HIGH',
            endDate: this.modalData.endDate,
            dueDate: this.modalData.dueDate,
            imageUrl: this.modalData.imageUrl ?? undefined
        };
        this.cardService.update(this.modalTaskId, payload).subscribe({
            next: (updated: CardResponse) => {
                const idxAll = this.allTasks.findIndex(t => t.id === updated.id);
                if (idxAll > -1) {
                    this.allTasks[idxAll] = {
                        id: updated.id,
                        title: updated.title,
                        descriptionBrief: updated.descriptionBrief,
                        descriptionFull: updated.descriptionFull,
                        imageUrl: updated.imageUrl,
                        status: updated.status as 'TODO' | 'DOING' | 'DONE',
                        priority: updated.priority as 'LOW' | 'MEDIUM' | 'HIGH' | null,
                        assigneeEmail: updated.assigneeEmail,
                        startDate: updated.startDate,
                        endDate: updated.endDate,
                        dueDate: updated.dueDate
                    };
                }
                this.applyFilters();
                this.closeModal();
            },
            error: () => {
                this.modalError = 'Não foi possível salvar as alterações.';
            }
        });
    }

    assignUser(): void {
        this.assignError = '';
        if (!this.assigneeEmailForAssign?.trim()) {
            this.assignError = 'E-mail não pode ficar em branco.';
            return;
        }
        if (!this.modalTaskId) {
            this.assignError = 'Nenhum card selecionado.';
            return;
        }
        const body: CardAssignRequest = {
            email: this.assigneeEmailForAssign.trim()
        };
        this.cardService.assign(this.modalTaskId, body).subscribe({
            next: (updated: CardResponse) => {
                const idxAll = this.allTasks.findIndex(t => t.id === updated.id);
                if (idxAll > -1) {
                    this.allTasks[idxAll].assigneeEmail = updated.assigneeEmail;
                }
                this.assigneeEmailForAssign = updated.assigneeEmail || '';
                this.applyFilters();
                this.assignError = '';
            },
            error: err => {
                this.assignError = err.error?.message || 'Falha ao atribuir responsável.';
            }
        });
    }

    async deleteCard(cardId: string) {
        if (!await this.dialogService.confirm('Você tem certeza que deseja excluir este card?')) {
            return;
        }
        this.cardService.delete(cardId).subscribe({
            next: () => {
                this.allTasks = this.allTasks.filter(t => t.id !== cardId);
                this.applyFilters();
            },
            error: () => {
                this.dialogService.alert('Falha ao excluir card.');
            }
        });
    }
}
