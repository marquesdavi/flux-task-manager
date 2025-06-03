export interface Board {
  id:        string;          // UUID
  title:     string;
  ownerId:   number;          // id do usuário proprietário (long)
  createdAt: string;          // date-time ISO
  updatedAt: string;          // date-time ISO
}
