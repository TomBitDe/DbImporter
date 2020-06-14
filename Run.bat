set JMX_OPTIONS=-Dcom.sun.management.jmxremote
set JMX_OPTIONS=%JMX_OPTIONS% -Dcom.sun.management.jmxremote.port=9010
set JMX_OPTIONS=%JMX_OPTIONS% -Dcom.sun.management.jmxremote.local.only=false
set JMX_OPTIONS=%JMX_OPTIONS% -Dcom.sun.management.jmxremote.authenticate=false
set JMX_OPTIONS=%JMX_OPTIONS% -Dcom.sun.management.jmxremote.ssl=false

set PATH=C:\"Program Files"\Java\jdk-14.0.1\bin;%PATH%

java -version

set LOG4J_OPTIONS=-Dlog4j.configuration=log4j.xml

java %JMX_OPTIONS% %LOG4J_OPTIONS% -classpath ./target/DbImporter-jar-with-dependencies.jar com.home.dbimportermaven.dbimporter.Main 

pause