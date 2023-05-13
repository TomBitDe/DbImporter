set JMX_OPTIONS=-Dcom.sun.management.jmxremote
set JMX_OPTIONS=%JMX_OPTIONS% -Dcom.sun.management.jmxremote.port=9010
set JMX_OPTIONS=%JMX_OPTIONS% -Dcom.sun.management.jmxremote.local.only=false
set JMX_OPTIONS=%JMX_OPTIONS% -Dcom.sun.management.jmxremote.authenticate=false
set JMX_OPTIONS=%JMX_OPTIONS% -Dcom.sun.management.jmxremote.ssl=false

set PATH=C:\"Program Files"\Java\jdk-14.0.1\bin;%PATH%

java -version

REM For debugging log4j2 enable the following line
REM set LOG4J_OPTIONS=-Dlog4j.debug -Dlog4j.configurationFile=file:./log4j2.xml

REM This is no debugging log4j2
set LOG4J_OPTIONS=-Dlog4j.configurationFile=file:./log4j2.xml

java %JMX_OPTIONS% %LOG4J_OPTIONS% -classpath ./target/DbImporter-jar-with-dependencies.jar com.home.dbimportermaven.dbimporter.DbImporter 

pause