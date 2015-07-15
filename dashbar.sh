#!/bin/bash
mvn clean install
if [ $? -ne 0 ]; then
    echo "[ERROR] MAVEN BUILD FAILURE"
    exit 1
fi
WAR=`find adaptive-assembly-war -iname *.war`
echo "War built -> "$WAR
echo "Cleaning up tomcat"
rm -rf deps/che/assembly-sdk/target/tomcat-ide/webapps/che*
echo "Copying built war to tomcat"
cp $WAR deps/che/assembly-sdk/target/tomcat-ide/webapps/che.war
echo "Forwarding command"
cd deps/che
if [ $1 = "restart" ];
    then
        ./che.sh stop
        ./che.sh jpda start
    else
        ./che.sh $*
fi