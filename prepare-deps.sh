cd deps
git submodule update --recursive --remote
cd che-core
mvn clean install
cd ..
cd che
mvn clean install
cd ..
cd user-dashboard
mvn clean install
cd ..
cd cli
mvn clean install
cd ..
cd ..

