# Build Process  
Maven is used to build the project. To build the war distribution file, clone the project and execute:
`mvn install`
 
In order for successful compilation:
 
Copy the `DuoKeys.Template` and include your own integration iKeys, sKeys, hKeys:
 
`cp ./src/main/resources/DuoKeys.Template ./src/main/resources/DuoKeys.properties`
 
## Dependencies
The following dependencies are not available through the Maven repositories and must be installed separately:
* [Duo-Java-Client](https://github.com/duosecurity/duo_client_java)
* [DuoWeb-1.1](https://github.com/duosecurity/duo_java)
 
The Registration/Management portion of the Web pages are locked with simple Spring Form Security.
Username: DuoTestUser
Password: 123456

## Tomcat and Apache configuration
You should use mod_proxy_ajp to proxy apache to tomcat so that SSL information gets passed from the browser to tomcat

Within Tomcat, in conf/context.xml update <Context> to be <Context useHttpOnly="true">
