const express = require('express');
const SwapRequest = require('../models/SwapRequest');
const Message = require('../models/Message');
const { requireAuth } = require('../middleware/auth');
const { notifyUser } = require('../services/notificationService');

const router = express.Router();

// POST /requests
router.post('/', requireAuth, async (req, res) => {
  try {
    const { listingId, proposedSlot, offeredInReturn } = req.body;
    if (!listingId || !proposedSlot || !offeredInReturn) {
      return res
        .status(400)
        .json({ error: 'listingId, proposedSlot and offeredInReturn are required' });
    }

    const request = await SwapRequest.create({
      listingId,
      requesterId: req.userId,
      proposedSlot,
      offeredInReturn,
    });

    await notifyUser(listingId, 'New swap request', 'Someone wants to swap skills with you!');

    return res.status(201).json({ request });
  } catch (err) {
    console.error('[requests POST]', err);
    return res.status(500).json({ error: 'Failed to create request' });
  }
});

// PATCH /requests/:id - accept / decline / complete
router.patch('/:id', requireAuth, async (req, res) => {
  try {
    const { status } = req.body;
    if (!['accepted', 'declined', 'completed'].includes(status)) {
      return res.status(400).json({ error: 'status must be accepted, declined or completed' });
    }

    const request = await SwapRequest.findByIdAndUpdate(
      req.params.id,
      { status },
      { new: true }
    );
    if (!request) return res.status(404).json({ error: 'Request not found' });

    await notifyUser(
      request.requesterId,
      'Swap request update',
      `Your request is now ${status}`
    );

    return res.json({ request });
  } catch (err) {
    console.error('[requests PATCH]', err);
    return res.status(500).json({ error: 'Failed to update request' });
  }
});

// GET /requests/:id/messages
router.get('/:id/messages', requireAuth, async (req, res) => {
  const messages = await Message.find({ requestId: req.params.id }).sort('sentAt');
  return res.json({ messages });
});

// POST /requests/:id/messages - also used by the app's WorkManager sync job
// to flush messages that were queued while offline.
router.post('/:id/messages', requireAuth, async (req, res) => {
  try {
    const { text, sentAt } = req.body;
    if (!text) return res.status(400).json({ error: 'text is required' });

    const message = await Message.create({
      requestId: req.params.id,
      senderId: req.userId,
      text,
      sentAt: sentAt || new Date(),
    });

    return res.status(201).json({ message });
  } catch (err) {
    console.error('[requests/:id/messages POST]', err);
    return res.status(500).json({ error: 'Failed to send message' });
  }
});

module.exports = router;
