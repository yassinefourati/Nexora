import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { TEAMS_BASE_PATH, TEAMS_KEY, type CreateTeamRequest, type UpdateTeamRequest, type Team } from '../api/teamsApi';

function useResource() {
  return useCrudResource<Team, CreateTeamRequest, UpdateTeamRequest>(TEAMS_BASE_PATH, TEAMS_KEY);
}

export function useTeams(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useTeam(id: string | undefined) {
  return useResource().useOne(id);
}
export function useCreateTeam() {
  return useResource().useCreate();
}
export function useUpdateTeam() {
  return useResource().useUpdate();
}
export function useDeleteTeam() {
  return useResource().useRemove();
}
