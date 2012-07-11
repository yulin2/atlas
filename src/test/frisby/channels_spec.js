var frisby = require('frisby');

frisby.create('Get the channels list')
	.get('http://atlas.metabroadcast.com/3.0/channels.json?limit=5')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSONLength('channels', 5)
.toss();

frisby.create('Restrict by broadcaster')
	.get('http://atlas.metabroadcast.com/3.0/channels.json?limit=5&broadcaster=bbc.co.uk')
	.expectJSON('channels.*', {
		'broadcaster': {'key': 'bbc.co.uk'}
	})
.toss();

frisby.create('Restrict by media_type')
	.get('http://atlas.metabroadcast.com/3.0/channels.json?limit=5&media_type=audio')
	.expectJSON('channels.*', {
		'media_type': 'audio'
	})
.toss();

frisby.create('Restrict by available_from')
	.get('http://atlas.metabroadcast.com/3.0/channels.json?limit=5&available_from=pressassociation.com')
	.expectJSON('channels.*', {
		'available_from': function(val) { expect(val).toContain({'key': 'pressassociation.com', 'name': 'PA', 'country': 'GB'}); }
	})
.toss();

frisby.create('Get a single channel')
	.get('http://atlas.metabroadcast.com/3.0/channels/cbbh.json')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSON('channels.0', {
		'title': 'BBC One',
		'media_type': 'video',
		'type': 'channel'
	})
	.expectJSON('channels.0.broadcaster', {'key': 'bbc.co.uk'})
	.expectJSON('channels.0.available_from.?', {'key': 'bbc.co.uk'})
	.expectJSON('channels.0.available_from.?', {'key': 'bbcredux.com'})
	.expectJSON('channels.0.available_from.?', {'key': 'pressassociation.com'})
.toss();

frisby.create('Check channel_groups annotation')
	.get('http://atlas.metabroadcast.com/3.0/channels/cbbh.json?annotations=channel_groups')
	.expectJSONLength('channels.0.channel_groups', 1)
.toss();