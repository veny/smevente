         ____                                 _   _____
        / ___| _ __ ___   _____   _____ _ __ | |_| ____|
        \___ \| '_ ` _ \ / _ \ \ / / _ \ '_ \| __|  _|  
         ___) | | | | | |  __/\ V /  __/ | | | |_| |___
        |____/|_| |_| |_|\___| \_/ \___|_| |_|\__|_____|


SMS Event(e) [TBD]


# Dev Env
## Maven
* https://stackoverflow.com/questions/3765903/how-to-include-local-jar-files-in-maven-project

### Build
* mvn compile
* mvn gwt:compile # TODO 'mvn compile' is prerequisite
* mvn war:war


* https://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-to-a-maven-project
mvn install:install-file -Dfile=war/WEB-INF/lib/gwt-validation-0.9b2-SNAPSHOT-without-hibernate.jar -DgroupId=eu.maydu -DartifactId=gwt-validation -Dversion=0.9b2-SNAPSHOT-without-hibernate -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=war/WEB-INF/lib/ColorPicker-GWT-2.1.jar -DgroupId=net.auroris -DartifactId=ColorPicker-GWT -Dversion=2.1 -Dpackaging=jar -DgeneratePom=true

### Eclipse
File / Import...
  Maven / Existing Maven Projects
  
#### Integration with Tomcat Server
project Properties / Project Facets
  enable Dynamic Web Module
? https://stackoverflow.com/questions/2119351/where-does-eclipse-deploy-web-applications-using-wtp

* install Tomcat into /opt
* Window / Preferences / Server / Runtime Environments / Add
  * pick up corresponding version
  * pick up installation directory

## GWT
```
mvn gwt:compile # invokes the GWT compiler
? mvn gwt:devmode 
```

## Tests
-ea -Djava.util.logging.config.file=src/main/resources/logging-devel.properties -DRTE=test -Dorientdb.installCustomFormatter=false