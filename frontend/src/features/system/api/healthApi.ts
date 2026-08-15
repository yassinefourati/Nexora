import axios from 'axios';
import { env } from '@/core/config/env';

/** Actuator lives at the backend origin, outside the /api/v1 prefix apiClient is scoped to. */
const backendOrigin = env.VITE_API_URL.replace(/\/api\/v1\/?$/, '');

export const actuatorClient = axios.create({ baseURL: backendOrigin, timeout: 10_000 });

export interface HealthComponent {
  status: string;
  details?: Record<string, unknown>;
}

export interface HealthResponse {
  status: string;
  groups?: string[];
  components?: Record<string, HealthComponent>;
}

export const getHealth = () => actuatorClient.get<HealthResponse>('/actuator/health').then((r) => r.data);
