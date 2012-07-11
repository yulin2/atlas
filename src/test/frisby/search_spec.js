var frisby = require('frisby');

frisby.create('Do a search')
	.get('http://atlas.metabroadcast.com/3.0/search.json?q=doctor%20who&limit=5')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSONLength('contents', 5)
	.expectJSON('contents.0', {
		// a search for doctor who should always return the tv brand as the first result
		'uri': 'http://www.bbc.co.uk/programmes/b006q2x0'
	})
.toss();