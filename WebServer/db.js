var mysql = require('mysql'),
    async = require('async');

exports.connection = function ()
  var mysql = mysql.createConnection({
    host : 'localhost',
    user : 'root',
    password : 'suedmeier',
    database : 'parts_list'
});
