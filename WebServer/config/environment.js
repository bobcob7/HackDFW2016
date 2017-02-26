var path = require('path');
var express = require('express');
var bodyParser = require('body-parser');
var settings = require('./settings');
// var models = require('../app/models/');

module.exports = function(app) {
  // app.use(express.logger({format: 'dev'}));
  app.use(bodyParser.urlencoded({extended: true}));
  app.use(function(req, res, next) {
    models(function(err, db) {
      if (err) return next(err);

      req.models = db.models;
      res.db = db;

      return next();
    });
  }),
  app.use(app.router);
};
