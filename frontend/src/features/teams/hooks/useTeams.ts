import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { TEAMS_BASE_PATH, TEAMS_KEY, type CreateTeamRequest, type UpdateTeamRequest, type Team } from '../api/teamsApi';

function resource() {
  return useCrudResource<Team, CreateTeamRequest, UpdateTeamRequest>(TEAMS_BASE_PATH, TEAMS_KEY);
}

export function useTeams(params: ListParams = {}) {
  return resource().useList(params);
}
export function useTeam(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateTeam() {
  return resource().useCreate();
}
export function useUpdateTeam() {
  return resource().useUpdate();
}
export function useDeleteTeam() {
  return resource().useRemove();
}
