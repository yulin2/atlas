var frisby = require('frisby');

frisby.create('Get a container')
	.get('http://atlas.metabroadcast.com/3.0/content.json?uri=http://www.bbc.co.uk/programmes/b006q2x0')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSON('contents.?', {
		'title': 'Doctor Who',
		'type': 'brand',
		'media_type': 'video',
		'specialization': 'tv',
		'content': function(val) { expect(val).toBeTruthy(); }
	})
.toss();

frisby.create('Get an item')
	.get('http://atlas.metabroadcast.com/3.0/content.json?uri=http://www.bbc.co.uk/programmes/b015p5kc')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSON('contents.?', {
		'episode_number': 13,
		'series_number': 6,
		'broadcasts': function(val) { expect(val).toBeTruthy(); },
		'locations': function(val) { expect(val).toBeTruthy(); },
		'container': {'uri': 'http://www.bbc.co.uk/programmes/b006q2x0'},
		'series_summary': {'uri': 'http://www.bbc.co.uk/programmes/b0103y2x'},
		'type': 'episode',
		'media_type': 'video',
		'specialization': 'tv'
	})
.toss();

frisby.create('Get an item with annotations')
	.get('http://atlas.metabroadcast.com/3.0/content.json?uri=http://www.bbc.co.uk/programmes/b015p5kc&annotations=brand_summary,series_summary')
	.expectJSON('contents.?', {
		'container': {
			'title': 'Doctor Who',
			'uri': 'http://www.bbc.co.uk/programmes/b006q2x0',
			'type': 'brand'
		},
		'series_summary': {
			'title': 'Series 6',
			'series_number': 6,
			'uri': 'http://www.bbc.co.uk/programmes/b0103y2x',
			'type': 'series'
		}
	})
.toss();