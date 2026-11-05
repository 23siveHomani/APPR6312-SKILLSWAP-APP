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

  // SkillSwap Official Privacy Policy Endpoint for Google Play Store compliance
  app.get('/privacy', (req, res) => {
    res.send(`
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>SkillSwap - Privacy Policy</title>
        <style>
          body { font-family: system-ui, -apple-system, sans-serif; line-height: 1.6; max-width: 800px; margin: 40px auto; padding: 0 20px; color: #333; }
          h1 { color: #1976D2; }
          h2 { color: #333; margin-top: 24px; }
          .effective-date { color: #666; font-style: italic; }
        </style>
      </head>
      <body>
        <h1>SkillSwap Privacy Policy</h1>
        <p class="effective-date">Effective Date: November 3, 2025</p>

        <h2>1. Overview</h2>
        <p>SkillSwap ("we", "our", "us") respects your privacy. This Privacy Policy explains how we collect, use, and protect your personal information when you use the SkillSwap Android application and associated REST API services.</p>

        <h2>2. Information We Collect</h2>
        <ul>
          <li><strong>Account Information:</strong> Name, email address, and encrypted credentials (or Google SSO profile details).</li>
          <li><strong>App Preferences:</strong> Language preferences (English, isiZulu, Afrikaans), notification toggles, and sync options.</li>
          <li><strong>Skill Listings & Content:</strong> Titles, categories, descriptions, and media/blob storage URLs provided for skill offers and requests.</li>
          <li><strong>Device & Push Tokens:</strong> Firebase Cloud Messaging (FCM) tokens for real-time push alerts.</li>
        </ul>

        <h2>3. How We Use Your Information</h2>
        <p>We use collected data solely to facilitate peer-to-peer skill exchanges, authenticate account sessions via secure JWT tokens, deliver push notifications, and customize your app experience.</p>

        <h2>4. Data Security & Storage</h2>
        <p>Passwords are salted and hashed using bcrypt on our secure servers. Plaintext passwords are never stored. Client sessions utilize AES-256 EncryptedSharedPreferences backed by the Android Keystore.</p>

        <h2>5. Third-Party Services</h2>
        <p>We integrate with Firebase (Google SSO & Firebase Cloud Messaging) and MongoDB Atlas (cloud database hosting). Data is never sold or shared with third-party advertisers.</p>

        <h2>6. Your Rights & Data Deletion</h2>
        <p>You may update or request deletion of your account and personal data at any time by contacting support or within the app settings.</p>

        <h2>7. Contact Us</h2>
        <p>For privacy inquiries, contact: <strong>support@skillswap.app</strong></p>
      </body>
      </html>
    `);
  });

  app.use('/auth', authRoutes);
  app.use('/users', userRoutes);
  app.use('/listings', listingRoutes);
  app.use('/requests', requestRoutes);

  // 404 fallback
  app.use((req, res) => res.status(404).json({ error: 'Not found' }));

  return app;
}

module.exports = createApp;
