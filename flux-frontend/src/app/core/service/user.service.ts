import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import {RegisterRequest, User, UserSummary} from '../models/user';

@Injectable({ providedIn: 'root' })
export class UserService {
    private readonly baseUrl = `${environment.api}/user`;

    constructor(private http: HttpClient) {}

    register(req: RegisterRequest): Observable<void> {
        return this.http.post<void>(`${this.baseUrl}`, req);
    }

    getCurrent(): Observable<User> {
        return this.http.get<User>(`${this.baseUrl}/me`);
    }

    updateCurrent(payload: RegisterRequest): Observable<void> {
        return this.http.patch<void>(`${this.baseUrl}/me`, payload);
    }

    deleteCurrent(): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/me`);
    }

    getUsersByBoardId(boardId: string): Observable<UserSummary[]> {
        return this.http.get<UserSummary[]>(`${this.baseUrl}/board/${boardId}`);
    }
}
