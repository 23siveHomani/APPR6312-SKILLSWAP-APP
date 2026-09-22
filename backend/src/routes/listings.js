const express = require('express');
const SkillListing = require('../models/SkillListing');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();

// GET /listings?category=Tutoring&lng=18.42&lat=-33.92&radiusKm=10
// Powers the Home/Browse Skills screen (4.2): filter by category and distance.
router.get('/', requireAuth, async (req, res) => {
  try {
    const { category, lng, lat, radiusKm } = req.query;
    const filter = {};
    if (category) filter.category = category;

    if (lng && lat) {
      filter.location = {
        $near: {
          $geometry: { type: 'Point', coordinates: [parseFloat(lng), parseFloat(lat)] },
          $maxDistance: (parseFloat(radiusKm) || 10) * 1000,
        },
      };
    }

    const listings = await SkillListing.find(filter).populate('ownerId', 'name timeCredits');
    return res.json({ listings });
  } catch (err) {
    console.error('[listings GET]', err);
    return res.status(500).json({ error: 'Failed to fetch listings' });
  }
});

// POST /listings
router.post('/', requireAuth, async (req, res) => {
  try {
    const { title, category, description, lng, lat } = req.body;
    if (!title || !category) {
      return res.status(400).json({ error: 'title and category are required' });
    }

    const listing = await SkillListing.create({
      ownerId: req.userId,
      title,
      category,
      description,
      location: { type: 'Point', coordinates: [lng || 0, lat || 0] },
    });

    return res.status(201).json({ listing });
  } catch (err) {
    console.error('[listings POST]', err);
    return res.status(500).json({ error: 'Failed to create listing' });
  }
});

// GET /listings/:id
router.get('/:id', requireAuth, async (req, res) => {
  try {
    const listing = await SkillListing.findById(req.params.id).populate(
      'ownerId',
      'name timeCredits'
    );
    if (!listing) return res.status(404).json({ error: 'Listing not found' });
    return res.json({ listing });
  } catch (err) {
    return res.status(400).json({ error: 'Invalid listing id' });
  }
});

module.exports = router;
