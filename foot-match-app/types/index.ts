// User types
export type UserRole = 'PLAYER' | 'ORGANIZER';

export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
}

export interface UserSummary {
  id: string;
  name: string;
}

// Auth types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  role: UserRole;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

// Match types
export type MatchStatus = 'OPEN' | 'CLOSED' | 'CANCELLED' | 'COMPLETED';
export type ParticipantStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface Match {
  id: string;
  title: string;
  description: string | null;
  location: string;
  matchDate: string;
  maxPlayers: number;
  currentPlayers: number;
  status: MatchStatus;
  organizer: UserSummary;
  createdAt: string;
}

export interface CreateMatchRequest {
  title: string;
  description?: string;
  location: string;
  matchDate: string;
  maxPlayers: number;
}

export interface UpdateMatchRequest {
  title: string;
  description?: string;
  location: string;
  matchDate: string;
  maxPlayers: number;
}

export interface Participant {
  id: string;
  player: UserSummary;
  status: ParticipantStatus;
  joinedAt: string;
}

// API types
export interface ApiError {
  code: string;
  message: string;
  timestamp: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
