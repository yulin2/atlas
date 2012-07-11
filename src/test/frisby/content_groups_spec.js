var frisby = require('frisby');

frisby.create('Get the content groups list')
	.get('http://atlas.metabroadcast.com/3.0/content_groups.json?limit=5')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSONLength('content_groups', 5)
.toss();

frisby.create('Get a single content group')
	.get('http://atlas.metabroadcast.com/3.0/content_groups/cbbq.json')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSON('content_groups.0', {
		'content': function(val) { expect(val).toBeTruthy(); },
		'type': 'playlist',
	})
.toss();

frisby.create('Get the content for a content group')
	.get('http://atlas.metabroadcast.com/3.0/content_groups/cbbq/content.json?limit=2')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSONLength('contents', 2)
.toss();