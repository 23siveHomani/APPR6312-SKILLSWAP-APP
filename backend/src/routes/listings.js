const express = require('express');
const SkillListing = require('../models/SkillListing');
const User = require('../models/User');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();

async function seedDefaultListings(defaultOwnerId) {
  const count = await SkillListing.countDocuments();
  if (count === 0) {
    let ownerId = defaultOwnerId;
    if (!ownerId) {
      let demoUser = await User.findOne();
      if (!demoUser) {
        demoUser = await User.create({
          name: 'Asive Homani',
          email: 'asive.seed@example.com',
          passwordHash: 'dummy'
        });
      }
      ownerId = demoUser._id;
    }

    await SkillListing.create([
      {
        ownerId,
        title: 'Basic Zulu Conversation Tutoring',
        category: 'Tutoring',
        description: 'One-on-one Zulu conversation practice and pronunciation tips for beginners.',
        imageUrl: 'https://storage.googleapis.com/skillswap-blobs/zulu_tutoring.jpg'
      },
      {
        ownerId,
        title: 'Logo & Graphic Design Help',
        category: 'Design',
        description: 'Need help designing a brand logo, flyer, or social media banner? I can help!',
        imageUrl: 'https://storage.googleapis.com/skillswap-blobs/graphic_design.png'
      },
      {
        ownerId,
        title: 'Android & Computer Tech Support',
        category: 'Tech Help',
        description: 'Assistance setting up Android apps, Wi-Fi connections, or computer troubleshooting.',
        imageUrl: 'https://storage.googleapis.com/skillswap-blobs/tech_support.jpg'
      },
      {
        ownerId,
        title: 'Home Repairs & Handyman Services',
        category: 'Home Repairs',
        description: 'Light plumbing, shelf mounting, or furniture assembly in exchange for skill tutoring.',
        imageUrl: 'https://storage.googleapis.com/skillswap-blobs/home_repair.jpg'
      }
    ]);
    console.log('[seed] Initial sample skill listings seeded automatically!');
  }
}

// GET /listings
router.get('/', requireAuth, async (req, res) => {
  try {
    await seedDefaultListings(req.userId);

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
    const { title, category, description, imageUrl, lng, lat } = req.body;
    if (!title || !category) {
      return res.status(400).json({ error: 'title and category are required' });
    }

    const listing = await SkillListing.create({
      ownerId: req.userId,
      title,
      category,
      description,
      imageUrl,
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
