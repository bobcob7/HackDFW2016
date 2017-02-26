var controllers = require('../app/controllers')

module.exports = function(app) {
  app.get('/'                , controllers.home);
  // app.post('/registerBoothQR', controllers.qrcode.create);
};
