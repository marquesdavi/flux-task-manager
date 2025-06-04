export interface Board {
    id: string;          // UUID
    title: string;
    ownerId: number;
    ownerEmail: string;
    collaboratorsEmails: string[];
    createdAt: string;          // date-time ISO
    updatedAt: string;          // date-time ISO
}
