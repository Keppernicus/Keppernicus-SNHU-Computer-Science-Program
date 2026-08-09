const mongoose = require('./db');
const Trip = require('./travlr');

const fs = require('fs');
const trips = JSON.parse(fs.readFileSync('./data/trips.json', 'utf8'));

const seedDB = async () => {
  await Trip.deleteMany({});
  await Trip.insertMany(trips);
};

seedDB()
  .then(async () => {
    await mongoose.connection.close();
    console.log('Database seeded and connection closed.');
    process.exit(0);
  })
  .catch((err) => {
    console.error('Seed failed:', err.message);
    process.exit(1);
  });