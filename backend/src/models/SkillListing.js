const mongoose = require('mongoose');

const skillListingSchema = new mongoose.Schema(
  {
    ownerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    title: { type: String, required: true, trim: true, maxlength: 100 },
    category: {
      type: String,
      required: true,
      trim: true,
    },
    description: { type: String, maxlength: 500, default: '' },
    imageUrl: { type: String, default: null },
    location: {
      type: { type: String, enum: ['Point'], default: 'Point' },
      coordinates: { type: [Number], default: [0, 0] },
    },
  },
  { timestamps: true }
);

skillListingSchema.index({ location: '2dsphere' });

module.exports = mongoose.model('SkillListing', skillListingSchema);
