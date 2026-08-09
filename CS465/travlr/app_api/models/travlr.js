const mongoose = require('mongoose');

// schema definition for a trip by Travlr Getaways.
// type and field control prevent broken or malformed entries
const tripSchema = new mongoose.Schema({
  code: {
    type: String,
    required: true,
    unique: true 
  },
  name: {
    type: String,
    required: true,
    index: true
  },
  length: {
    type: String,
    required: true
  },
  start: {
    type: Date,
    required: true
  },
  resort: {
    type: String,
    required: true
  },
  perPerson: {
    type: String,
    required: true
  },
  image: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  }
});

//name trip maps to the trips collection in MongoDB
module.exports = mongoose.model('trip', tripSchema);
