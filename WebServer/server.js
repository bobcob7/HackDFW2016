var express = require('express');
var bodyParser = require('body-parser');
var mysql = require('mysql');
var connection = mysql.createConnection({
  host : 'localhost',
  user : 'root',
  password : 'suedmeier',
  database : 'parts_list'
});

var app = express();
app.use(bodyParser.urlencoded({ extended:true }));

connection.connect(function(err) {
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

function findBoothQR (req, res) {
  // var qrcode = req.body.qrcode;
  connection.query('SELECT * FROM parts_list.booths WHERE Id = ?', req.body,
    function(err, result) {
      if (err) {
        console.log(result.insertId);
        throw err;
      }
      res.send('Your QRCode is ' + result.insertId);
  });
  console.log(qrcode);
}

function send_items (req, res) {
  res.send()
};

app.get('/', confirm_request);

app.post('/registerBoothQR/', findBoothQR);

app.listen(3000);
console.log('Listening on port 3000...');

connection.end();
