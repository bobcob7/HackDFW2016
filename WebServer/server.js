var express = require('express');
var mysql = require('mysql');
var app = express();

var db = require('./db');
db.connect(function(err) {
  if(!err) {
    console.log("Database is connected.\n");
  }
  else {
    console.log("Error connecting to database.\n");
  }
});

function confirm_request (req, res) {
  res.sendStatus(200);
};

function send_items (req, res) {
  // res.send()
};

app.get('/', confirm_request);

app.get('/connect', function(req, res) {
  // res.send()
});

app.listen(3000);
console.log('Listening on port 3000...');
