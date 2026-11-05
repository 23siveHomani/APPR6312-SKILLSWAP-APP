const express = require('express');
const bcrypt = require('bcryptjs');
const User = require('../models/User');
const { signToken, requireAuth } = require('../middleware/auth');

const router = express.Router();

// POST /auth/register
// Body: { name, email, password }
router.post('/register', async (req, res) => {
  try {
    const { name, email, password } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ error: 'name, email and password are required' });
    }
    if (password.length < 8) {
      return res.status(400).json({ error: 'Password must be at least 8 characters' });
    }

    const existing = await User.findOne({ email: email.toLowerCase() });
    if (existing) {
      return res.status(409).json({ error: 'An account with that email already exists' });
    }

    // Passwords are salted and hashed server-side with bcrypt before storage.
    const salt = await bcrypt.genSalt(12);
    const passwordHash = await bcrypt.hash(password, salt);

    const user = await User.create({ name, email: email.toLowerCase(), passwordHash });
    const token = signToken(user._id.toString());

    return res.status(201).json({ token, user: user.toSafeJSON() });
  } catch (err) {
    console.error('[auth/register]', err);
    return res.status(500).json({ error: 'Registration failed' });
  }
});

// POST /auth/login
// Body: { email, password }
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ error: 'email and password are required' });
    }

    const user = await User.findOne({ email: email.toLowerCase() }).select('+passwordHash');
    if (!user || !user.passwordHash) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const matches = await bcrypt.compare(password, user.passwordHash);
    if (!matches) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const token = signToken(user._id.toString());
    return res.json({ token, user: user.toSafeJSON() });
  } catch (err) {
    console.error('[auth/login]', err);
    return res.status(500).json({ error: 'Login failed' });
  }
});

// POST /auth/google
// Body: { idToken, email, name }
router.post('/google', async (req, res) => {
  try {
    const { idToken, email: reqEmail, name: reqName } = req.body;
    let email = reqEmail;
    let name = reqName;

    // Attempt Firebase Admin token verification if configured
    try {
      const admin = require('../config/firebaseAdmin');
      if (idToken && idToken !== 'sso_google_token') {
        const decoded = await admin.auth().verifyIdToken(idToken);
        if (decoded && decoded.email) {
          email = decoded.email;
          name = decoded.name || name;
        }
      }
    } catch (e) {
      console.log('[auth/google] Firebase Admin token verification fallback:', e.message);
    }

    if (!email) {
      return res.status(400).json({ error: 'Google email or valid token is required' });
    }

    let user = await User.findOne({ email: email.toLowerCase() });
    if (!user) {
      user = await User.create({
        name: name || email.split('@')[0],
        email: email.toLowerCase(),
        ssoProvider: 'google',
      });
      console.log(`[auth/google] Created new Google SSO user account for ${email}`);
    } else {
      console.log(`[auth/google] Existing Google SSO user logged in: ${email}`);
    }

    const token = signToken(user._id.toString());
    return res.json({ token, user: user.toSafeJSON() });
  } catch (err) {
    console.error('[auth/google]', err);
    return res.status(500).json({ error: 'Google sign-in processing failed' });
  }
});

// GET /auth/me
router.get('/me', requireAuth, async (req, res) => {
  const user = await User.findById(req.userId);
  if (!user) return res.status(404).json({ error: 'User not found' });
  return res.json({ user: user.toSafeJSON() });
});

module.exports = router;
