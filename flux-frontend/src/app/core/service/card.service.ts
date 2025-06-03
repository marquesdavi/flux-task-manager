// src/app/shared/services/card.service.ts
import { Injectable } from '@angular/core';
import { HttpClient }   from '@angular/common/http';
import {Observable, of} from 'rxjs';

export interface TaskDetails {
  id: string;
  title: string;
  descriptionBrief: string;
  descriptionFull: string;
  status: 'To Do' | 'Doing' | 'Done';
  startDate: string; // yyyy-MM-dd
  endDate:   string; // yyyy-MM-dd
  dueDate:   string; // yyyy-MM-dd
  imageUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CardService {
  private baseUrl = '/api/cards';

  constructor(private http: HttpClient) { }

  // getById(id: string): Observable<TaskDetails> {
  //   return this.http.get<TaskDetails>(`${this.baseUrl}/${id}`);
  // }
  //
  // update(id: string, payload: Partial<TaskDetails>): Observable<TaskDetails> {
  //   return this.http.put<TaskDetails>(`${this.baseUrl}/${id}`, payload);
  // }

  /** Fake getById: retorna dados mockados após 500ms */
  getById(id: string): Observable<TaskDetails> {
    // Exemplo de dados mockados, você pode variar de acordo com o `id`
    const mock: TaskDetails = {
      id,
      title: `Título do card ${id}`,
      descriptionBrief: `Descrição breve do card ${id}`,
      descriptionFull: `Descrição completa e mais longa do card ${id}. Aqui você pode detalhar tudo.`,
      status: (id === '1' ? 'To Do' : id === '3' ? 'Doing' : 'Done') as any,
      startDate: '2025-05-01',
      endDate: '2025-05-10',
      dueDate: '2025-05-15',
      imageUrl: 'https://via.placeholder.com/400x200.png?text=Imagem+do+Card+' + id
    };

    // Retorna o mock depois de 500ms, para simular latência
    return new Observable<TaskDetails>((subscriber) => {
      setTimeout(() => {
        subscriber.next(mock);
        subscriber.complete();
      }, 500);
    });
  }

  /** Fake update, simplesmente retorna o payload enviado como se fosse atualizado */
  update(id: string, payload: Partial<TaskDetails>): Observable<TaskDetails> {
    const updated: TaskDetails = {
      id,
      title: payload.title ?? `Título do card ${id}`,
      descriptionBrief: payload.descriptionBrief ?? `Descrição breve do card ${id}`,
      descriptionFull: payload.descriptionFull ?? `Descrição completa do card ${id}`,
      status: payload.status ?? 'To Do',
      startDate: payload.startDate ?? '2025-05-01',
      endDate: payload.endDate ?? '2025-05-10',
      dueDate: payload.dueDate ?? '2025-05-15',
      imageUrl: payload.imageUrl
    };
    return of(updated);
  }
}
