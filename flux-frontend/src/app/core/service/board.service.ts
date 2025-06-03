import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { Board } from '../models/board';

@Injectable({ providedIn: 'root' })
export class BoardService {
    private readonly baseUrl = `${environment.api}/boards`; // → http://localhost:8080/api/boards

    constructor(private http: HttpClient) {}

    getAll(): Observable<Board[]> {
        return this.http.get<Board[]>(`${this.baseUrl}`);
    }

    create(title: string): Observable<Board> {
        return this.http.post<Board>(`${this.baseUrl}`, { title });
    }

    rename(id: string, title: string): Observable<Board> {
        return this.http.patch<Board>(`${this.baseUrl}/${id}`, { title });
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }

    getById(id: string): Observable<Board> {
        return this.http.get<Board>(`${this.baseUrl}/${id}`);
    }
}
