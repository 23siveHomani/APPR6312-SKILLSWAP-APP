require('./setup');
const request = require('supertest');
const createApp = require('../src/app');

const app = createApp();

describe('POST /auth/register', () => {
  it('creates a new user and returns a JWT', async () => {
    const res = await request(app).post('/auth/register').send({
      name: 'Asive Homani',
      email: 'asive@example.com',
      password: 'correcthorse1',
    });

    expect(res.statusCode).toBe(201);
    expect(res.body.token).toBeDefined();
    expect(res.body.user.email).toBe('asive@example.com');
    // The password hash must never be returned to the client.
    expect(res.body.user.passwordHash).toBeUndefined();
  });

  it('rejects a duplicate email', async () => {
    await request(app).post('/auth/register').send({
      name: 'First',
      email: 'dup@example.com',
      password: 'correcthorse1',
    });

    const res = await request(app).post('/auth/register').send({
      name: 'Second',
      email: 'dup@example.com',
      password: 'anotherpass1',
    });

    expect(res.statusCode).toBe(409);
  });

  it('rejects a password shorter than 8 characters', async () => {
    const res = await request(app).post('/auth/register').send({
      name: 'Short PW',
      email: 'short@example.com',
      password: '123',
    });
    expect(res.statusCode).toBe(400);
  });
});

describe('POST /auth/login', () => {
  beforeEach(async () => {
    await request(app).post('/auth/register').send({
      name: 'Login Test',
      email: 'login@example.com',
      password: 'correcthorse1',
    });
  });

  it('logs in with correct credentials', async () => {
    const res = await request(app)
      .post('/auth/login')
      .send({ email: 'login@example.com', password: 'correcthorse1' });

    expect(res.statusCode).toBe(200);
    expect(res.body.token).toBeDefined();
  });

  it('rejects an incorrect password', async () => {
    const res = await request(app)
      .post('/auth/login')
      .send({ email: 'login@example.com', password: 'wrongpassword' });

    expect(res.statusCode).toBe(401);
  });
});

describe('GET /auth/me', () => {
  it('rejects requests with no token', async () => {
    const res = await request(app).get('/auth/me');
    expect(res.statusCode).toBe(401);
  });

  it('returns the current user profile for a valid token', async () => {
    const register = await request(app).post('/auth/register').send({
      name: 'Me Test',
      email: 'me@example.com',
      password: 'correcthorse1',
    });
    const token = register.body.token;

    const res = await request(app).get('/auth/me').set('Authorization', `Bearer ${token}`);
    expect(res.statusCode).toBe(200);
    expect(res.body.user.email).toBe('me@example.com');
  });
});
