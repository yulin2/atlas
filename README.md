Build and Run Atlas
===================

The Atlas source-code is hosted at http://github.com/atlasapi and is split across 5 projects:
* http://github.com/atlasapi/atlas
* http://github.com/atlasapi/atlas-persistence
* http://github.com/atlasapi/atlas-model
* http://github.com/atlasapi/atlas-feeds

Additionally, http://github.com/atlasapi/atlas-client hosts the Java client library. http://github.com/atlasapi/atlas is the main entry point and you don't need the others unless you're interested in updating them too.

## MongoDB

Atlas, and specifically atlas-persistence, uses http://www.mongodb.org/ to store its indexed content. Atlas doesn't come packaged with MongoDB so you'll need to make sure it's installed: http://www.mongodb.org/downloads

During test runs, Atlas will run integration tests against a mongo running on port 8585. If one isn't running then it'll try and start one, assuming that mongod is available on its path, so either keep one running or make sure you've added mongo/bin to your path.

The running Atlas instance requires that MongoDB be running on its standard port:27107. Please make sure you've kicked one off.

## Maven

Atlas uses http://maven.apache.org/ for all it's dependency and build management, so you'd better have mvn available on your path! We've included the MetaBroadcast public repo which houses all the dependencies that haven't been mavenised, and all our successful builds deploy to it so it has the latest atlas SNAPSHOTs available. This means you don't have to build the other atlas projects, if you don't want to.

It's worth noting that we don't current have a formal release process and everything's currently a SNAPSHOT release. We're sorry if this is a pain and we have every intention of creating some proper releases soon, when life has calmed down a bit.

## Building and Running

So, to get everything built and ready:

    git clone http://github.com/atlasapi/atlas
    cd atlas
    mvn clean install
    
This will download all the dependencies, compile the code and run the tests (make sure mongo's setup). To actually run the project locally:

    mvn jetty:run
    
This will startup Atlas locally using the lovely http://jetty.codehaus.org/jetty/ and you'll be able to go to:

    http://localhost:8080/2.0/brands.json?uri=http://www.bbc.co.uk/programmes/b006m86d
    
If you have an empty database then this will go off to the BBC, grab the metadata for Eastenders, store it and return it to you.

Enjoy!