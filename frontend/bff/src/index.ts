/**
 * BFF skeleton — expand as needed for production deployment.
 * Not used in development (MSW handles all requests).
 */
import express from 'express';
import cors from 'cors';

const app  = express();
const PORT = process.env.PORT ?? 3001;

app.use(cors({ origin: process.env.FRONTEND_URL ?? 'http://localhost:5173', credentials: true }));
app.use(express.json());

// Health check
app.get('/health', (_req, res) => res.json({ status: 'ok' }));

// TODO: Add auth middleware, CSRF, rate limiting, tenant routing, proxy routes

app.listen(PORT, () => console.log(`BFF listening on :${PORT}`));
