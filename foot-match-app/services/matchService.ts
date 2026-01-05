import { request } from './api';
import type {
  Match,
  CreateMatchRequest,
  UpdateMatchRequest,
  Participant,
  PaginatedResponse,
  MatchStatus,
} from '@/types';

interface MatchFilters {
  status?: MatchStatus;
  location?: string;
  dateFrom?: string;
  page?: number;
  size?: number;
}

export const matchService = {
  async getAll(filters?: MatchFilters): Promise<PaginatedResponse<Match>> {
    const params = new URLSearchParams();
    if (filters?.status) params.append('status', filters.status);
    if (filters?.location) params.append('location', filters.location);
    if (filters?.dateFrom) params.append('dateFrom', filters.dateFrom);
    if (filters?.page !== undefined) params.append('page', String(filters.page));
    if (filters?.size !== undefined) params.append('size', String(filters.size));

    const query = params.toString() ? `?${params.toString()}` : '';
    return request<PaginatedResponse<Match>>(`/api/matches${query}`);
  },

  async getById(id: string): Promise<Match> {
    return request<Match>(`/api/matches/${id}`);
  },

  async create(data: CreateMatchRequest): Promise<Match> {
    return request<Match>(
      '/api/matches',
      {
        method: 'POST',
        body: JSON.stringify(data),
      },
      true
    );
  },

  async update(id: string, data: UpdateMatchRequest): Promise<Match> {
    return request<Match>(
      `/api/matches/${id}`,
      {
        method: 'PUT',
        body: JSON.stringify(data),
      },
      true
    );
  },

  async delete(id: string): Promise<void> {
    return request<void>(
      `/api/matches/${id}`,
      { method: 'DELETE' },
      true
    );
  },

  async join(matchId: string): Promise<Participant> {
    return request<Participant>(
      `/api/matches/${matchId}/join`,
      { method: 'POST' },
      true
    );
  },

  async leave(matchId: string): Promise<void> {
    return request<void>(
      `/api/matches/${matchId}/leave`,
      { method: 'DELETE' },
      true
    );
  },

  async getParticipants(matchId: string): Promise<Participant[]> {
    return request<Participant[]>(`/api/matches/${matchId}/participants`);
  },

  async updateParticipantStatus(
    matchId: string,
    playerId: string,
    status: 'ACCEPTED' | 'REJECTED'
  ): Promise<Participant> {
    return request<Participant>(
      `/api/matches/${matchId}/participants/${playerId}`,
      {
        method: 'PUT',
        body: JSON.stringify({ status }),
      },
      true
    );
  },
};
