// src/app/core/services/user.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegisterRequest {
  // Ajuste os campos conforme sua DTO no backend
  username: string;
  email: string;
  password: string;
  // adicione outros campos presentes em RegisterRequest, se houver
}

export interface User {
  id: number;
  username: string;
  email: string;
  // se o backend retorna mais campos de usuário, adicione-os aqui
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly baseUrl = '/api/user';

  constructor(private http: HttpClient) {}

  /** Busca usuário pelo ID */
  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  /** Atualiza os dados do usuário */
  update(id: number, payload: RegisterRequest): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}`, payload);
  }

  /** Exclui a conta do usuário */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  register(req: RegisterRequest): Observable<void> {
    return this.http.post<void>(this.baseUrl, req);
  }
}
