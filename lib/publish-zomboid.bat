
@rem used to publish the zomboid classes as jar file to your local maven repository
mvn install:install-file -Dfile=zomboid.jar -DgroupId=zomboid -DartifactId=zomboid -Dversion=41.76 -Dpackaging=jar