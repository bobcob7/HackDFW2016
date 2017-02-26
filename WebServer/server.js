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
  console.log('SELECT * FROM booths WHERE Id='+req.body.qrcode);
  connection.query('SELECT PartId FROM UBooth INNER JOIN UMod ON UBooth.ModId=UMod.ModId INNER JOIN parts ON UMod.PartId=parts.Id WHERE BoothId=? AND found=0 AND assigned=0 LIMIT 1;', req.body.qrcode,
  function(err, result) {
      if (err) {
        console.log(result);
        throw err;
      }
      else
      {
	if(result.length > 0)
		res.send(result[0].Id.toString());
  }
});
  //console.log(qrcode);
}

function ackPartId (req, res) {
  var partId = req.body.partId;
  console.log('SELECT * FROM booths WHERE Id='+req.body.qrcode);
  connection.query('UPDATE parts SET assigned=1 WHERE available=0 AND assigned=0 AND parts.Id=?;', partId,
  function(err, result) {
      if (err) {
        console.log(result);
        throw err;
      }
  }
});
  //console.log(qrcode);
}

function send_items (req, res) {
  res.send()
};

app.get('/', confirm_request);

app.post('/registerBoothQR/', findBoothQR);

app.post('/ack', ackPartId);

app.listen(3000);
console.log('Listening on port 3000...');

//connection.end();
