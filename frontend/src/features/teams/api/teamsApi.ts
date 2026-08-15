import type { components } from '@/shared/types/api.generated';

export type Team = components['schemas']['TeamResponse'];
export type CreateTeamRequest = components['schemas']['CreateTeamRequest'];
export type UpdateTeamRequest = components['schemas']['UpdateTeamRequest'];

export const TEAMS_BASE_PATH = '/teams';
export const TEAMS_KEY = 'teams';
