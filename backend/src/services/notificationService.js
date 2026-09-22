const User = require('../models/User');

/**
 * Sends a push notification via Firebase Cloud Messaging to the given user's
 * registered device token. This is deliberately fault-tolerant: if Firebase
 * isn't configured (no service account on this machine/CI run) or the user
 * has no fcmToken yet, it logs and returns rather than throwing - a missing
 * push notification should never break the underlying API action.
 */
async function notifyUser(userId, title, body) {
  try {
    const user = await User.findById(userId);
    if (!user || !user.fcmToken || !user.notificationsEnabled) return;

    let admin;
    try {
      admin = require('../config/firebaseAdmin');
    } catch (e) {
      console.log(`[notify] Firebase not configured, skipping push: "${title}"`);
      return;
    }

    await admin.messaging().send({
      token: user.fcmToken,
      notification: { title, body },
    });
  } catch (err) {
    console.error('[notify] Failed to send push notification:', err.message);
  }
}

module.exports = { notifyUser };
