exports.index = (req, res) => {
  res.render('index', { title: 'Travlr Getaways' });
};

exports.travel = (req, res) => {
  res.render('travel', { title: 'Travel - Travlr Getaways' });
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