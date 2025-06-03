// src/app/features/board-page/board-page.component.ts
import { Component, ElementRef, QueryList, ViewChildren } from '@angular/core';
import {
  CdkDropList,
  CdkDropListGroup,
  CdkDrag,
  CdkDragDrop,
  moveItemInArray,
  transferArrayItem
} from '@angular/cdk/drag-drop';
import { FormsModule }      from '@angular/forms';
import { v4 as uuidv4 }     from 'uuid';

import { ColumnHeaderComponent }        from '../../../shared/components/column-header.component/column-header.component';
import { CardComponent }                from '../../../shared/components/card.component/card.component';
import { AddCardButtonComponent }       from '../../../shared/components/add-card-button.component/add-card-button.component';
import { DragDropPlaceholderComponent } from '../../../shared/components/drag-drop-placeholder.component/drag-drop-placeholder.component';
import { CardService, TaskDetails }     from '../../../core/service/card.service';

interface Task {
  id: string;
  title: string;
  description?: string;
  imageUrl?: string;
  status: 'To Do' | 'Doing' | 'Done';
  startDate?: string;
  endDate?: string;
  dueDate?: string;
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
    DragDropPlaceholderComponent,
    CdkDropListGroup,
    CdkDropList,
    CdkDrag
  ],
  templateUrl: './board-page.component.html',
  styleUrls: ['./board-page.component.css']
})
export class BoardPageComponent {
  columns: Column[] = [
    { title: 'To Do',  tasks: [] },
    { title: 'Doing', tasks: [] },
    { title: 'Done',  tasks: [] }
  ];

  dragging = false;

  showNewCardInput: boolean[] = [];
  newCardTitle:     string[]  = [];
  newCardError:     string[]  = [];

  showModal: boolean            = false;
  modalTaskId: string | null    = null;
  modalData: TaskDetails | null = null;
  modalError: string            = '';

  @ViewChildren('newCardInput') newCardInputs!: QueryList<ElementRef<HTMLInputElement>>;

  constructor(private cardService: CardService) {
    this.columns.forEach((_, idx) => {
      this.showNewCardInput[idx] = false;
      this.newCardTitle[idx]     = '';
      this.newCardError[idx]     = '';
    });

    this.columns[0].tasks = [
      {
        id: '1',
        title: 'Design Landing Page',
        description: 'Create wireframes e mockups para a nova landing page.',
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuAw0-SBzBUSY3S8mpk4Kd7UDo06j4mVPue27Mg-GAwu5XRA3gDE6txHGRSItr4TYr6IlWj6CkYBRRSqNOMf9GaCuumMXYKh_FPArBhK47rYfiYCGdzwTumPhti1xW2HSdd7NQsBBc4J1-kO6mmXHONPaJT2vY2WxFYONpBZ6odmFDBHUbm_OYRSBAj-qBAfuJzp4EH7h4KVY_xoCJhFmBK-RH1GSNisunMVmZ-NAaOPT0N-U21hiNR0HGpHjlal6D44ikIyzw-BFOA',
        status: 'To Do'
      },
      {
        id: '2',
        title: 'User Research',
        description: 'Conduzir entrevistas para coletar feedback do design atual.',
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuBqckTgtt9N_zyJUZdtFx7vnbV6Ss5ydK2y4yfOjlqCiQhW94pY8jYYOmvhpV-XfvQWwpqJv1AjEFSwTG2mqV-w7z3PvB-pGqWEFtYXxe3dfQqWzp_N1XSUNqIx4ZOwZmorfOoNlsYOJTFI9K3gBfhYaxkFzk1vmfCLUb7HAXbO5l-rH0eS06nfC_3z7mDAlfu2DQF_Of-UCiwDrJ-d4-3QaJN2ufkkVv4hJexHlPKvOx5PtD6F2MX24rqd72gDL_el79ROEoVtrrw',
        status: 'To Do'
      }
    ];

    this.columns[1].tasks = [
      {
        id: '3',
        title: 'Develop API',
        description: 'Implementar endpoints de autenticação no backend.',
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuAxgZG_XIILJGkPVFOjTPh_ELjbwFHbYMdkGWoyx--sZOEHUJ3r4XPcjx0aKY34LDExQ92B8qKqSRelPm4X5pl0BlfL5nDuNd2Qs4ea96Rl_S-nDRaCBfZ81T_0LcE_IxFcvcP5otq_GtZev1DWmNHMORvSuf7jYxiLxYd2w3ZYMnCMalCrHGJbMJUzPY9DMaaY5taXGFkuGzxvq0msWDa45bZLA91IS74uAJBAvYjTv0pYmXp0YTVft36vCs1j0ggCriZ8kdd0ANU',
        status: 'Doing'
      },
      {
        id: '4',
        title: 'Write Documentation',
        description: 'Documentar endpoints de API e instruções de uso.',
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuDDa2pukjRwcjHKp22yHaRG_mM0g9DAZgNMsiA8Vc5XRpyC8U8Z870fwm1-B2S9L9ELFFqw3Hf14LmwU54BmUrGR9-X6uKx6kujkUq2NdB1_V0nczrFsNraguY4dI_XXflEIv6qhN6lFnuYJARg6srFZBqA39_5FpD2kWS9q0ghR_kSyFkpR_gOG-ZpG_mXT-4PX7sWf8B2K1WHULQtljoM4HQWGJMh-B0cVO5AUA1TGmo5GBev2pA2D1bguJ0pja5HyS8OkaLf-zk',
        status: 'Doing'
      }
    ];

    this.columns[2].tasks = [
      {
        id: '5',
        title: 'Project Kickoff',
        description: 'Reunião inicial com a equipe para discutir metas e prazos.',
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuCTjM9B51RS-8_Cwv44dEJdeCZT2JMmgso2qDn59jH95WgOXXg9hIu99SMfXqBJ9SXV0hwGepWC_64h6yj5AS1BrWqtGwqFHSJaAWinA-91MNEUBjxVLXAfG5vTBN0VY9nRRNa0P7wTVYwMPVrBuwRP-0AgY6zMjsST3Tk57r60XGdO1PhO5IGAmPIuXmwcAmPVuKQPirKDSVgQu7HJATXITTdsrBs6SMuhSqHi5tKVfYKFeMvxhiafm0rdbH1iFoB2TpHsYDtOCys',
        status: 'Done'
      },
      {
        id: '6',
        title: 'Design Review',
        description: 'Revisar e finalizar mockups com os stakeholders.',
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuBt3zFk6w-TquSjdL5-QnbZ0855xIAsU_pmQocpGXBeAoenkal4CSHPeFuFX9SEL498DOEqEPL0F-YFdZP39J_dHjpgOeKJEM-tjbWC3s8c83UaYLtc2NWeFBCZZIejK2MngNz4WoJHfMOIDRLymHQYHNg8KfrMHkMiezX9hMOtT5hGWg5EYws6hWXFV0gSah4EhJuq4Qf_MfLrGwmuWxIxhaxqlcAbEAm9cM7QtuwoLE_jBoPC5BnmM58AAuGJVBmFRej_2sTckgs',
        status: 'Done'
      }
    ];
  }

  drop(event: CdkDragDrop<Task[]>) {
    const dragged: Task = event.item.data as Task;

    if (event.previousContainer === event.container) {
      moveItemInArray(
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
      const destinoArray = event.container.data as Task[];
      const colunaDestino = this.columns.find(c => c.tasks === destinoArray);
      if (colunaDestino) {
        dragged.status = colunaDestino.title;
      }
    }
  }

  onAddCardClick(colIndex: number) {
    this.showNewCardInput[colIndex] = true;
    this.newCardTitle[colIndex]     = '';
    this.newCardError[colIndex]     = '';
    setTimeout(() => {
      const arr = this.newCardInputs.toArray();
      arr[colIndex]?.nativeElement.focus();
    });
  }

  onCreateCard(colIndex: number) {
    const title = this.newCardTitle[colIndex].trim();
    if (!title) {
      this.newCardError[colIndex] = 'Preencha antes de criar';
      return;
    }
    const newTask: Task = {
      id: uuidv4(),
      title,
      description: '',
      imageUrl: undefined,
      status: this.columns[colIndex].title
    };
    this.columns[colIndex].tasks.unshift(newTask);
    this.showNewCardInput[colIndex] = false;
    this.newCardTitle[colIndex]     = '';
    this.newCardError[colIndex]     = '';
  }

  onCardInputKeyDown(event: KeyboardEvent, colIndex: number) {
    if (event.key === 'Enter') {
      this.onCreateCard(colIndex);
    } else if (event.key === 'Escape') {
      this.onCancelCard(colIndex);
    }
  }

  onCancelCard(colIndex: number) {
    this.showNewCardInput[colIndex] = false;
    this.newCardTitle[colIndex]     = '';
    this.newCardError[colIndex]     = '';
  }

  openModal(taskId: string) {
    this.modalError  = '';
    this.modalData   = null;
    this.modalTaskId = taskId;

    this.cardService.getById(taskId).subscribe({
      next: (resp) => {
        this.modalData = resp;
        this.showModal = true;
      },
      error: () => {
        this.modalError = 'Não foi possível carregar dados do card.';
        this.showModal = false;
      }
    });
  }

  closeModal() {
    this.showModal   = false;
    this.modalTaskId = null;
    this.modalData   = null;
    this.modalError  = '';
  }

  saveModal() {
    if (!this.modalTaskId || !this.modalData) {
      return;
    }
    if (!this.modalData.title.trim()) {
      this.modalError = 'Título não pode ficar vazio.';
      return;
    }

    this.cardService.update(this.modalTaskId, {
      title:            this.modalData.title,
      descriptionBrief: this.modalData.descriptionBrief,
      descriptionFull:  this.modalData.descriptionFull,
      status:           this.modalData.status,
      startDate:        this.modalData.startDate,
      endDate:          this.modalData.endDate,
      dueDate:          this.modalData.dueDate,
      imageUrl:         this.modalData.imageUrl
    }).subscribe({
      next: (updated) => {
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
            id:          updated.id,
            title:       updated.title,
            description: updated.descriptionBrief,
            imageUrl:    updated.imageUrl,
            status:      updated.status
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

  onAction(label: string) {
    console.log('[onAction] argumento:', label);
  }
}
