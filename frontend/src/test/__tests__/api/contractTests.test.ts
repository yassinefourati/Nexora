import { describe, it, expect } from 'vitest';
import { z } from 'zod';

const userSchema = z.object({ id:z.number(), name:z.string().min(1), email:z.string().email(), role:z.enum(['admin','editor','viewer']) });
const usersPageSchema = z.object({ data:z.array(userSchema), total:z.number().nonnegative(), page:z.number().positive() });
const auditSchema = z.object({ id:z.string(), user:z.string(), action:z.string(), resource:z.string(), resourceId:z.string(), ip:z.string(), timestamp:z.string().datetime(), severity:z.enum(['info','warning','error']) });
const statsSchema = z.object({ users:z.number(), revenue:z.number(), orders:z.number(), activeUsers:z.number() });
const notifSchema = z.object({ id:z.string(), title:z.string(), message:z.string(), read:z.boolean(), type:z.enum(['info','warning','success','error']), createdAt:z.string().datetime() });

describe('API contract — users', () => {
  it('users page matches schema', () => expect(() => usersPageSchema.parse({ data:[{ id:1, name:'Alice', email:'a@b.com', role:'admin' }], total:1, page:1 })).not.toThrow());
  it('missing email fails', () => expect(() => userSchema.parse({ id:1, name:'X', role:'admin' })).toThrow());
  it('invalid role fails', () => expect(() => userSchema.parse({ id:1, name:'X', email:'x@x.com', role:'superuser' })).toThrow());
});

describe('API contract — dashboard', () => {
  it('stats match schema', () => expect(() => statsSchema.parse({ users:100, revenue:5000, orders:20, activeUsers:50 })).not.toThrow());
  it('missing field fails', () => expect(() => statsSchema.parse({ users:100, revenue:5000, orders:20 })).toThrow());
});

describe('API contract — audit', () => {
  it('entry matches schema', () => expect(() => auditSchema.parse({ id:'a1', user:'Alice', action:'login', resource:'Session', resourceId:'1', ip:'1.1.1.1', timestamp:'2024-01-01T00:00:00.000Z', severity:'info' })).not.toThrow());
  it('invalid severity fails', () => expect(() => auditSchema.parse({ id:'a1', user:'Alice', action:'x', resource:'X', resourceId:'1', ip:'1.1.1.1', timestamp:'2024-01-01T00:00:00.000Z', severity:'debug' })).toThrow());
});

describe('API contract — notifications', () => {
  it('notification matches schema', () => expect(() => notifSchema.parse({ id:'n1', title:'T', message:'M', read:false, type:'info', createdAt:'2024-01-01T00:00:00.000Z' })).not.toThrow());
});
