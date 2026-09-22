require('./setup');
const request = require('supertest');
const createApp = require('../src/app');

const app = createApp();

async function registerAndLogin(email) {
  const res = await request(app).post('/auth/register').send({
    name: 'Listing Tester',
    email,
    password: 'correcthorse1',
  });
  return res.body.token;
}

describe('Listings API', () => {
  it('rejects creating a listing without auth', async () => {
    const res = await request(app).post('/listings').send({ title: 'x', category: 'Other' });
    expect(res.statusCode).toBe(401);
  });

  it('creates and then fetches a listing', async () => {
    const token = await registerAndLogin('owner@example.com');

    const create = await request(app)
      .post('/listings')
      .set('Authorization', `Bearer ${token}`)
      .send({
        title: 'Basic Zulu tutoring',
        category: 'Tutoring',
        description: 'One-on-one Zulu conversation practice',
        lng: 18.42,
        lat: -33.92,
      });

    expect(create.statusCode).toBe(201);
    const listingId = create.body.listing._id;

    const fetchAll = await request(app).get('/listings').set('Authorization', `Bearer ${token}`);
    expect(fetchAll.statusCode).toBe(200);
    expect(fetchAll.body.listings.length).toBe(1);

    const fetchOne = await request(app)
      .get(`/listings/${listingId}`)
      .set('Authorization', `Bearer ${token}`);
    expect(fetchOne.statusCode).toBe(200);
    expect(fetchOne.body.listing.title).toBe('Basic Zulu tutoring');
  });

  it('filters listings by category', async () => {
    const token = await registerAndLogin('filter@example.com');

    await request(app).post('/listings').set('Authorization', `Bearer ${token}`).send({
      title: 'Fix a leaky tap',
      category: 'Home & Garden',
    });
    await request(app).post('/listings').set('Authorization', `Bearer ${token}`).send({
      title: 'Python tutoring',
      category: 'Tech Help',
    });

    const res = await request(app)
      .get('/listings?category=Tech Help')
      .set('Authorization', `Bearer ${token}`);

    expect(res.body.listings.length).toBe(1);
    expect(res.body.listings[0].category).toBe('Tech Help');
  });
});
