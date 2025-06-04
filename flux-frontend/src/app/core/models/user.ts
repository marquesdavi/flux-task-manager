export interface RegisterRequest {
  email:     string;
  password:  string;
  firstName: string;
  lastName:  string;
}

export interface User {
  id:        number;
  firstName: string;
  lastName:  string;
  email:     string;
  role:      'ADMIN' | 'USER';
}

export interface UserSummary {
    id:        number;
    firstName: string;
    lastName:  string;
    email:     string;
}
