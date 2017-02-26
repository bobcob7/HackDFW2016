var path = require('path');
var express = require('express');
var settings = require('./config/settings');
var environment = require('./config/environment');
var routes = require('./config/routes');
// var models = require('./app/models');

var app = express();

environment(app);
routes(app);

app.listen(settings.port, function() {
  console.log("Listening on port " + settings.port);
  if (done) {
    return done(null, app, server);
  }
}).on('error', function(e) {
  if (e.code == 'EADDRINUSE') {
    console.log('Address in use');
  }
  if (done) {
    return done(e);
  }
});
