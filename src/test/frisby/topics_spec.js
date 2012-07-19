var frisby = require('frisby');

frisby.create('Get the topics list')
	.get('http://atlas.metabroadcast.com/3.0/topics.json?limit=5')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSONLength('topics', 5)
.toss();

frisby.create('Restrict by namespace and value')
	.get('http://atlas.metabroadcast.com/3.0/topics.json?namespace=dbpedia&value=http://dbpedia.org/resource/London')
	.expectJSON('topics.*', {
		'namespace': 'dbpedia',
		'value': 'http://dbpedia.org/resource/London'
	})
.toss();

frisby.create('Get a single topic')
	.get('http://atlas.metabroadcast.com/3.0/topics/ccFk.json')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSON('topics.0', {
		'title': 'London',
		'publisher': {'key': 'dbpedia.org'},
		'type': 'place',
		'namespace': 'dbpedia',
		'value': 'http://dbpedia.org/resource/London'
	})
.toss();

frisby.create('Get the content for a topic')
	.get('http://atlas.metabroadcast.com/3.0/topics/ccFk/content.json?limit=2')
	.expectStatus(200)
	.expectHeaderContains('content-type', 'application/json')
	.expectJSONLength('contents', 2)
.toss();