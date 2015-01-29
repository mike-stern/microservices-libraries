microservices-libraries
=======================

Core libraries to help implement microservice applications

Testing
=======

To run integration tests, install consul.

The integration tests do not run in the default build. To run them you must:

 * Activate a profile
 * Specify a bind address
 * Specify a port for the consul agent to listen on

Example:
```
mvn clean test -P microservice-integration-test \
-Dpath.consul=/opt/tomcat/bin/consul \
-Daddress.consul=127.0.0.1  \
-Dport.consul=98312 \
-Dnode.consul=some-development-server.usgs.gov
```
