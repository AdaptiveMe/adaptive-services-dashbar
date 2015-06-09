#!/bin/bash
mvn clean install
WAR=`find adaptive-assembly-war -iname *.war`
echo "War built -> "$WAR
echo "Cleaning up tomcat"
rm -rf deps/che/assembly-sdk/target/tomcat-ide/webapps/che*
echo "Copying built war to tomcat"
cp $WAR deps/che/assembly-sdk/target/tomcat-ide/webapps/che.war
echo "Forwarding command"
cd deps/che
./che.sh $*