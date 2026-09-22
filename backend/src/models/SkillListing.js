const mongoose = require('mongoose');

const skillListingSchema = new mongoose.Schema(
  {
    ownerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    title: { type: String, required: true, trim: true, maxlength: 100 },
    category: {
      type: String,
      enum: ['Home & Garden', 'Tutoring', 'Tech Help', 'Crafts', 'Other'],
      required: true,
    },
    description: { type: String, maxlength: 500, default: '' },
    // GeoJSON Point, as specified in the Part 1 data dictionary, so we can
    // do distance-based search with a 2dsphere index.
    location: {
      type: { type: String, enum: ['Point'], default: 'Point' },
      coordinates: { type: [Number], default: [0, 0] }, // [lng, lat]
    },
  },
  { timestamps: true }
);

skillListingSchema.index({ location: '2dsphere' });

module.exports = mongoose.model('SkillListing', skillListingSchema);
