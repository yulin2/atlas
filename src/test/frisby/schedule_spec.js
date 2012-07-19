var frisby = require('frisby');

frisby.create('Get a schedule')
	.get('http://atlas.metabroadcast.com/3.0/schedule.json?from=now&to=now.plus.6h&publisher=bbc.co.uk&channel_id=cbbh&annotations=channel')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSON('schedule.0.channel', {'id': 'cbbh'})
	.expectJSON('schedule.0', {
		'items': function(val) { expect(val).toBeTruthy(); }
	})
.toss();