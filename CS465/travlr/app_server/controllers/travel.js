// var fs = require('fs');
// var trips = JSON.parse(fs.readFileSync('./data/trips.json', 'utf8'));

const tripsEndpoint = 'http://localhost:3000/api/trips';
const options = { method: 'GET', headers: { 'Accept': 'application/json' } };

exports.index = (req, res) => {
  res.render('index', { title: 'Travlr Getaways' });
};

exports.travel = async (req, res) => {
  await fetch(tripsEndpoint, options)
    .then(response => response.json())
    .then(json => {
      let message = null;
      if (!(json instanceof Array)) {
        message = json.message === 'No trips found'
          ? 'No trips exist in our database!'
          : 'API lookup error';
        json = [];
      } else if (!json.length) {
        message = 'No trips exist in our database!';
      }
      res.render('travel', { title: 'Travel - Travlr Getaways', trips: json, message });
    })
    .catch(err => {
      console.log('Error retrieving trips from API:', err.message);
      res.render('travel', { title: 'Travel - Travlr Getaways', trips: [], message: 'API lookup error' });
    });
};

exports.about = (req, res) => {
  res.render('about', { title: 'About - Travlr Getaways' });
};

exports.rooms = (req, res) => {
  res.render('rooms', { title: 'Rooms - Travlr Getaways' });
};

exports.meals = (req, res) => {
  res.render('meals', { title: 'Meals - Travlr Getaways' });
};

exports.news = (req, res) => {
  res.render('news', { title: 'News - Travlr Getaways' });
};

exports.contact = (req, res) => {
  res.render('contact', { title: 'Contact - Travlr Getaways' });
};