import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {
    CardCreateRequest,
    CardUpdateRequest,
    CardResponse, CardAssignRequest
} from '../models/card';
import {environment} from '../../environments/environment';

@Injectable({providedIn: 'root'})
export class CardService {
    private baseUrl = `${environment.api}`;

    constructor(private http: HttpClient) {
    }

    getAllByBoard(boardId: string): Observable<CardResponse[]> {
        return this.http.get<CardResponse[]>(`${this.baseUrl}/boards/${boardId}/cards`);
    }

    create(boardId: string, payload: CardCreateRequest): Observable<CardResponse> {
        return this.http.post<CardResponse>(`${this.baseUrl}/boards/${boardId}/cards`, payload);
    }

    getById(cardId: string): Observable<CardResponse> {
        return this.http.get<CardResponse>(`${this.baseUrl}/cards/${cardId}`);
    }

    update(cardId: string, payload: CardUpdateRequest): Observable<CardResponse> {
        return this.http.patch<CardResponse>(`${this.baseUrl}/cards/${cardId}`, payload);
    }

    delete(cardId: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/cards/${cardId}`);
    }

    assign(cardId: string, payload: CardAssignRequest): Observable<CardResponse> {
        return this.http.patch<CardResponse>(`${this.baseUrl}/cards/${cardId}/assign`, payload);
    }
}
