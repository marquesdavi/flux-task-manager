// src/app/core/service/board.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Board} from '../models/board';

@Injectable({
  providedIn: 'root'
})
export class BoardService {
  private baseUrl = '/api/boards'; // ajuste conforme seu backend

  constructor(private http: HttpClient) {}

  /** Retorna todos os boards  */
  getAll(): Observable<Board[]> {
    return this.http.get<Board[]>(`${this.baseUrl}`);
  }

  /** Cria um novo board */
  create(title: string): Observable<Board> {
    return this.http.post<Board>(`${this.baseUrl}`, { title });
  }

  /** Renomeia um board existente */
  rename(id: string, title: string): Observable<Board> {
    return this.http.patch<Board>(`${this.baseUrl}/${id}`, { title });
  }

  /** Deleta um board por ID */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
