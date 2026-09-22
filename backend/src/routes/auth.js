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

    // Passwords are salted and hashed server-side with bcrypt before storage,
    // per Part 1 section 3 ("Register & log in"). Plaintext is never persisted.
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
      // Same generic message whether the email doesn't exist or the account is SSO-only,
      // so we don't leak which emails are registered.
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
// Body: { idToken } - a Firebase Auth ID token obtained on-device via Google Sign-In.
// Verifies the token with Firebase Admin, then finds-or-creates the matching user.
router.post('/google', async (req, res) => {
  try {
    const { idToken } = req.body;
    if (!idToken) {
      return res.status(400).json({ error: 'idToken is required' });
    }

    let admin;
    try {
      admin = require('../config/firebaseAdmin');
    } catch (e) {
      return res.status(501).json({
        error: 'Google SSO is not configured on this server (missing Firebase service account)',
      });
    }

    const decoded = await admin.auth().verifyIdToken(idToken);
    const { email, name } = decoded;

    let user = await User.findOne({ email: email.toLowerCase() });
    if (!user) {
      user = await User.create({
        name: name || email.split('@')[0],
        email: email.toLowerCase(),
        ssoProvider: 'google',
      });
    }

    const token = signToken(user._id.toString());
    return res.json({ token, user: user.toSafeJSON() });
  } catch (err) {
    console.error('[auth/google]', err);
    return res.status(401).json({ error: 'Google sign-in verification failed' });
  }
});

// GET /auth/me - convenience endpoint the app can call on launch to check the
// stored token is still valid and to refresh the cached profile.
router.get('/me', requireAuth, async (req, res) => {
  const user = await User.findById(req.userId);
  if (!user) return res.status(404).json({ error: 'User not found' });
  return res.json({ user: user.toSafeJSON() });
});

module.exports = router;
