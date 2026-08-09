var express = require('express');
var router = express.Router();
var travelController = require('../controllers/travel');

/* GET home page. */
router.get('/', travelController.index);

/* GET travel page. */
router.get('/travel', travelController.travel);

/* GET rooms page. */
router.get('/rooms', travelController.rooms);

/* GET meals page. */
router.get('/meals', travelController.meals);

/* GET news page. */
router.get('/news', travelController.news);

/* GET about page. */
router.get('/about', travelController.about);

/* GET contact page. */
router.get('/contact', travelController.contact);

module.exports = router;