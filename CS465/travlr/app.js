var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var hbs = require('hbs');
var cors = require('cors');

// Bring in the db connection
require('./app_api/models/db');
require('dotenv').config();

var passport = require('passport');
require('./app_api/config/passport');


if (!globalThis.crypto) globalThis.crypto = require('crypto').webcrypto;
var indexRouter = require('./app_server/routes/index');
var usersRouter = require('./app_api/routes/users');
var apiRouter = require('./app_api/routes/index');

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'app_server/views'));
app.set('view engine', 'hbs');
hbs.registerPartials(path.join(__dirname, 'app_server/views/partials'));

app.use(cors({ origin: 'http://localhost:4200' }));

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// initialize passport before the routes that use it
app.use(passport.initialize());

app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/api', apiRouter);

// 404, forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render error
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;