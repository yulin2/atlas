var frisby = require('frisby');

frisby.create('Get a product')
	.get('http://atlas.metabroadcast.com/3.0/products/cgPS.json?apiKey=65249e7a93584e02b13b59521101cff0')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSON('products.0', {
		'gtin': '5051561034305',
		'title': 'Doctor Who Series 6',
		'type': 'dvd'
	})
	.expectJSON('products.0.locations.0', {
		'uri': function(val) { expect(val).toBeDefined(); },
		'availability': function(val) { expect(val).toBeDefined(); },
		'price': function(val) { expect(val).toBeDefined(); },
		'shipping_price': function(val) { expect(val).toBeDefined(); }
	})
.toss();

frisby.create('Get the content for a product')
	.get('http://atlas.metabroadcast.com/3.0/products/cgPS/content.json?apiKey=65249e7a93584e02b13b59521101cff0&limit=2')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSONLength('contents', 2)
.toss();