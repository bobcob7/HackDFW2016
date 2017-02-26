module.exports = function(app) {
  app.dataSources.parts_list.automigrate('Booth', function(err) {
    if (err) throw err;

    app.models.Booth.create([{
      OnSite: "DFW",
      "Module1": {
        "id" : 1,
        "part1": {
          "id": 1,
          "available": 1,
          "assigned": 0,
          "found": 0,
          "placed": 0
        },
        "part2" : {
          "id": 2,
          "available": 1,
          "assigned": 0,
          "found": 0,
          "placed": 0
        }
      },
      "Module2": {
        "id": 2,
        "part1": {
          "id": 1,
          "available": 1,
          "assigned": 0,
          "found": 0,
          "placed": 0
        }
      }
    }, ], function(err, coffeeShops) {
      if (err) throw err;

      console.log('Models created: \n', coffeeShops);
    });
  });
};
