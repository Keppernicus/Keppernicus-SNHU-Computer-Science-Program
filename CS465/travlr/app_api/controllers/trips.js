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

// POST /api/trips
const tripsAddTrip = async (req, res) => {
  try {
    const newTrip = new Trip({
      code: req.body.code,
      name: req.body.name,
      length: req.body.length,
      start: req.body.start,
      resort: req.body.resort,
      perPerson: req.body.perPerson,
      image: req.body.image,
      description: req.body.description
    });

    const trip = await newTrip.save();

    // 201 signals a new resource was created
    return res.status(201).json(trip);
  } catch (err) {
    // schema validation failures and duplicate codes are client errors
    if (err.name === 'ValidationError' || err.code === 11000) {
      return res.status(400).json({ message: err.message });
    }
    return res.status(500).json({ message: err.message });
  }
};

// PUT /api/trips/:tripCode
const tripsUpdateTrip = async (req, res) => {
  try {
    const trip = await Trip.findOneAndUpdate(
      { code: req.params.tripCode },
      {
        code: req.body.code,
        name: req.body.name,
        length: req.body.length,
        start: req.body.start,
        resort: req.body.resort,
        perPerson: req.body.perPerson,
        image: req.body.image,
        description: req.body.description
      },
      // returnDocument:'after' gives back the updated doc; runValidators keeps schema rules in force
      { returnDocument: 'after', runValidators: true }
    ).exec();

    if (!trip) {
      return res
        .status(404)
        .json({ message: `Trip ${req.params.tripCode} not found` });
    }

    return res.status(200).json(trip);
  } catch (err) {
    if (err.name === 'ValidationError' || err.code === 11000) {
      return res.status(400).json({ message: err.message });
    }
    return res.status(500).json({ message: err.message });
  }
};

// DELETE /api/trips/:tripCode
const tripsDeleteTrip = async (req, res) => {
  try {
    const trip = await Trip.findOneAndDelete({ code: req.params.tripCode }).exec();

    if (!trip) {
      return res
        .status(404)
        .json({ message: `Trip ${req.params.tripCode} not found` });
    }

    return res.status(200).json({ message: `Trip ${req.params.tripCode} deleted` });
  } catch (err) {
    return res.status(500).json({ message: err.message });
  }
};

module.exports = {
  tripsList,
  tripsFindByCode,
  tripsAddTrip,
  tripsUpdateTrip,
  tripsDeleteTrip
};
