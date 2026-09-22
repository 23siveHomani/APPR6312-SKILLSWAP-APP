const admin = require('firebase-admin');

// Lazily initialised so requiring this module fails cleanly (and callers can
// catch it) when no service account has been configured yet - see README
// "Firebase setup" for how to generate firebase-service-account.json.
if (!admin.apps.length) {
  const path = process.env.FIREBASE_SERVICE_ACCOUNT_PATH || './firebase-service-account.json';
  const serviceAccount = require(require('path').resolve(path));
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
}

module.exports = admin;
