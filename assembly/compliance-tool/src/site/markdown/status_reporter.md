## <a name="SW360Exporter">SW360 Status Reporter</a>

The SW360 Status Reporter generates a document containig requested information
from a given SW360 instance. 

Similar to the [SW360Exporter](./exporter.html) and the [SW360Updater](./updater.html)
you can start the status reporter by calling the `--reporter` mode.
Without an information request, the status reporter will not work. In order to give an
information request add a parameter `--info=<info-parameter>`.

For example: 
```
   java -jar <path/to/compliancetool>.jar --reporter --info=releases-cleared 
 
```

If you have an information request that requires additional information to get, you 
provide the additional information with the additional parameters belonging to the
information request.

For example: 
```
   java -jar <path/to/compliancetool>.jar --reporter --info=releases-of-project --project_id=<id> 
 
```  

### Output Format
The Status Reporter so far only supports csv as an output format. 

### Help Messages
In order to see a full list of all information requests you can use
the `--help` switch 

```
   java -jar <path/to/compliancetool>.jar --reporter --help 
 
```  

In order to get more information which additional parameters goes with which
information request you can also use the `--help` switch


```
   java -jar <path/to/compliancetool>.jar --reporter --info=<info-parameter> --help
 
```  

### Properties
- `targetDir`: Target directory in which the result file is created in. 
- `basedir`: Base directory of the execution
- `csvFilePath`: name of the csv file
- `proxyHost`: If a proxy is in use, supply the host name
- `proxyPort`: If a proxy is in use, supply the port
- `proxyUse`: If a proxy is in use, this should be set to true
- `sw360restServerUrl`: Link to the rest server url of your SW360 instance
- `sw360authServerUrl`: Link to the authentication server url of your SW360 instance
- `sw360user`: SW360 Username
- `sw360password`: SW360 User Password
- `sw360clientId`: SW360 Client Id
- `sw360clientPassword`: SW360 Client Password