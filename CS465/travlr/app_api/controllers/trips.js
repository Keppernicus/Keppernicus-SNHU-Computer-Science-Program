const Trip = require('../models/travlr');

// GET /api/trips 
const tripsList = async (req, res) => {
  try {
    const trips = await Trip.find({}).exec();

    if (!trips || trips.length === 0) {
      return res
        .status(404)
        .json({ message: 'No trips found' });
    }

    return res.status(200).json(trips);
  } catch (err) {
    return res.status(500).json({ message: err.message });
  }
};

// GET /api/trips/:tripCode
const tripsFindByCode = async (req, res) => {
  try {
    const trip = await Trip.find({ code: req.params.tripCode }).exec();

    if (!trip || trip.length === 0) {
      return res
        .status(404)
        .json({ message: `Trip ${req.params.tripCode} not found` });
    }

    return res.status(200).json(trip);
  } catch (err) {
    return res.status(500).json({ message: err.message });
  }
};

module.exports = {
  tripsList,
  tripsFindByCode
};
