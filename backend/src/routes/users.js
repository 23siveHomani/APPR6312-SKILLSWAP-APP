const express = require('express');
const User = require('../models/User');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();

// PATCH /users/me
// Body: any subset of { name, languagePref, notificationsEnabled, syncOnWifiOnly, fcmToken }
// This backs the Settings screen (4.5 in Part 1): language, notification
// preferences, sync behaviour, and FCM token registration.
router.patch('/me', requireAuth, async (req, res) => {
  try {
    const allowed = ['name', 'languagePref', 'notificationsEnabled', 'syncOnWifiOnly', 'fcmToken'];
    const updates = {};
    for (const key of allowed) {
      if (req.body[key] !== undefined) updates[key] = req.body[key];
    }

    if (updates.languagePref && !['en', 'zu', 'af'].includes(updates.languagePref)) {
      return res.status(400).json({ error: 'languagePref must be one of en, zu, af' });
    }

    const user = await User.findByIdAndUpdate(req.userId, updates, {
      new: true,
      runValidators: true,
    });
    if (!user) return res.status(404).json({ error: 'User not found' });

    return res.json({ user: user.toSafeJSON() });
  } catch (err) {
    console.error('[users/me PATCH]', err);
    return res.status(500).json({ error: 'Failed to update settings' });
  }
});

router.get('/me', requireAuth, async (req, res) => {
  const user = await User.findById(req.userId);
  if (!user) return res.status(404).json({ error: 'User not found' });
  return res.json({ user: user.toSafeJSON() });
});

module.exports = router;
