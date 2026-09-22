const mongoose = require('mongoose');

/**
 * Connects to MongoDB (Atlas in production, or a URI injected by tests locally).
 * Logging is used instead of silent failures so connection problems are visible
 * immediately in the server console / Render logs.
 */
async function connectDB(uri) {
  mongoose.set('strictQuery', true);
  try {
    await mongoose.connect(uri);
    console.log(`[db] Connected to MongoDB: ${mongoose.connection.name}`);
  } catch (err) {
    console.error('[db] MongoDB connection failed:', err.message);
    throw err;
  }
}

async function disconnectDB() {
  await mongoose.disconnect();
}

module.exports = { connectDB, disconnectDB };
