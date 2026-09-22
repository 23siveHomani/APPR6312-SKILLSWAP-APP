const mongoose = require('mongoose');

// Mirrors the User entity from the Part 1 data dictionary:
// userId, name, email, passwordHash, ssoProvider, languagePref, timeCredits, fcmToken
const userSchema = new mongoose.Schema(
  {
    name: { type: String, required: true, trim: true },
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    // bcrypt hash only - a plaintext password is never stored or returned.
    // Optional because SSO accounts have no password at all.
    passwordHash: { type: String, select: false },
    ssoProvider: { type: String, enum: ['google', null], default: null },
    languagePref: { type: String, enum: ['en', 'zu', 'af'], default: 'en' },
    timeCredits: { type: Number, default: 0 },
    fcmToken: { type: String, default: null },
    notificationsEnabled: { type: Boolean, default: true },
    syncOnWifiOnly: { type: Boolean, default: true },
  },
  { timestamps: true }
);

// Never leak the password hash even if a document is accidentally serialised.
userSchema.methods.toSafeJSON = function toSafeJSON() {
  const obj = this.toObject();
  delete obj.passwordHash;
  delete obj.__v;
  return { userId: obj._id, ...obj, _id: undefined };
};

module.exports = mongoose.model('User', userSchema);
