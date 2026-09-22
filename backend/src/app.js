const express = require('express');
const cors = require('cors');

const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const listingRoutes = require('./routes/listings');
const requestRoutes = require('./routes/requests');

function createApp() {
  const app = express();

  app.use(cors());
  app.use(express.json());

  // Simple request logger - satisfies the brief's "make use of logging to
  // show your understanding of your code" for the API side.
  app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.originalUrl}`);
    next();
  });

  app.get('/health', (req, res) => res.json({ status: 'ok' }));

  app.use('/auth', authRoutes);
  app.use('/users', userRoutes);
  app.use('/listings', listingRoutes);
  app.use('/requests', requestRoutes);

  // 404 fallback
  app.use((req, res) => res.status(404).json({ error: 'Not found' }));

  return app;
}

module.exports = createApp;
