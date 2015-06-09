# adaptive-services-dashbar

AdaptiveMe DashBar Services

#Running the dashbar
1. ``./prepare-deps.sh``
2. edit the ``adaptive-data-jpa/src/main/resources/META-INF/spring/database.properties`` with the database info
3. ``./dashbar.sh [start|stop]``


#To debug
1. ``./dashbar.sh jpda start``
2. connect using remote debugger in your favorite IDE


#Alternative

Tomcat plugin is enabled but not fully supported in the moment

1. ``mvn clean install``
2. ``mvn tomcat7:run``

__protip__ You can add the alternative commands in your IDE and DEBUG as a MAVEN execution.