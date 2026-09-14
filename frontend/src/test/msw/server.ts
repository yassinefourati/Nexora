import { setupServer } from 'msw/node';
import { handlers } from './handlers';

/** Node-side MSW server for Vitest. Tests call server.use(...) for one-off overrides. */
export const server = setupServer(...handlers);
