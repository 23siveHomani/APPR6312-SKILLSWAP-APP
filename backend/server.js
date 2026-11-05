require('dotenv').config();
const createApp = require('./src/app');
const { connectDB } = require('./src/config/db');

const PORT = process.env.PORT || 5000;

async function start() {
  if (!process.env.MONGO_URI) {
    console.error('[server] MONGO_URI is not set. Copy .env.example to .env and fill it in.');
    process.exit(1);
  }

  await connectDB(process.env.MONGO_URI);

  const app = createApp();
  const HOST = process.env.HOST || '0.0.0.0';
  app.listen(PORT, HOST, () => {
    console.log(`[server] SkillSwap API listening on http://${HOST}:${PORT}`);
  });
}

start().catch((err) => {
  console.error('[server] Failed to start:', err);
  process.exit(1);
});
