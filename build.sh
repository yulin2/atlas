cd ../common/ && git pull origin master && mvn clean install && mvn eclipse:eclipse
cd ../uriplay-media-reference/ && svn up && mvn clean install && mvn eclipse:eclipse
cd ../uriplay-feeds/ && svn up && mvn clean install && mvn eclipse:eclipse
cd ../uriplay-persistence/ && svn up && mvn clean install && mvn eclipse:eclipse
cd ../uriplay-local-remote-client/ && svn up && mvn clean install && mvn eclipse:eclipse
cd ../uriplay/ && svn up && mvn clean install && mvn eclipse:eclipse
