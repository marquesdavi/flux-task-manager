export interface CardCreateRequest {
    title: string;
    descriptionBrief?: string;
    descriptionFull?: string;
    status: 'TODO' | 'DOING' | 'DONE';
    startDate?: string;  // “yyyy-MM-dd”
    endDate?: string;  // “yyyy-MM-dd”
    dueDate?: string;  // “yyyy-MM-dd”
    imageUrl?: string;
}

export interface CardUpdateRequest {
    title: string;
    descriptionBrief?: string;
    descriptionFull?: string;
    status: 'TODO' | 'DOING' | 'DONE';
    startDate?: string;
    endDate?: string;
    dueDate?: string;
    imageUrl?: string;
}

export interface CardResponse {
    id: string;                // UUID
    title: string;
    descriptionBrief: string;
    descriptionFull: string;
    status: 'TODO' | 'DOING' | 'DONE';
    startDate: string;                // “yyyy-MM-dd”
    endDate: string;                // “yyyy-MM-dd”
    dueDate: string;                // “yyyy-MM-dd”
    imageUrl: string | null;
    assigneeEmail: string | null;
    boardId: string;                // UUID
    createdAt: string;                // date-time ISO
    updatedAt: string;                // date-time ISO
}

export interface CardAssignRequest {
    email: string;
}
