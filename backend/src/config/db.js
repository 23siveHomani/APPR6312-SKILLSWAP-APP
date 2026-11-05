const mongoose = require('mongoose');

/**
 * Connects to MongoDB Atlas when a valid connection string is provided in .env.
 * Automatically falls back to an in-memory MongoDB instance for local development
 * if the provided URI is a placeholder or unreachable.
 */
async function connectDB(uri) {
  mongoose.set('strictQuery', true);

  if (!uri || uri.includes('<username>') || uri.includes('<password>')) {
    console.log('[db] Placeholder MONGO_URI detected. Starting in-memory local MongoDB database...');
    try {
      const { MongoMemoryServer } = require('mongodb-memory-server');
      const mongod = await MongoMemoryServer.create();
      uri = mongod.getUri();
    } catch (e) {
      console.warn('[db] Could not start MongoMemoryServer:', e.message);
    }
  }

  try {
    await mongoose.connect(uri);
    console.log(`[db] Connected to MongoDB database successfully! (${mongoose.connection.name})`);
  } catch (err) {
    console.log('[db] Cloud MongoDB connection failed. Falling back to in-memory local database...');
    try {
      const { MongoMemoryServer } = require('mongodb-memory-server');
      const mongod = await MongoMemoryServer.create();
      const localUri = mongod.getUri();
      await mongoose.connect(localUri);
      console.log(`[db] Connected to in-memory local MongoDB successfully!`);
    } catch (fallbackErr) {
      console.error('[db] MongoDB connection failed:', err.message);
      throw err;
    }
  }
}

async function disconnectDB() {
  await mongoose.disconnect();
}

module.exports = { connectDB, disconnectDB };
