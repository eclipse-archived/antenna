## SW360 Updater
The `SW360Updater` generator step is used to update your SW360 instance.   
It checks for all found artifacts if they are already contained in your instance, 
and if so, if they contain all the metadata ${docNameCap} has found already.  
If the `SW360Updater` differs per artifact by 

1. License
2. Component
3. Release

All metadata not contained in your SW360 instance yet, will be created and updated.  
As a final step your whole project will be either created or updated with your current project metadata. 

### HowTo use
Add the following step into the `<generators>` section of your workflow.xml

```xml
<step>
    <name>SW360 Updater</name>
    <classHint>org.eclipse.sw360.antenna.workflow.generators.SW360Updater</classHint>                   
    <configuration>
        <entry key="rest.server.url" value="http://localhost:8080/resource/api"/>
        <entry key="auth.server.url" value="http://localhost:8080/authorization"/>
        <entry key="username" value="admin@sw360.org"/>
        <entry key="password" value="12345"/>
    </configuration>
</step>
```

#### Explanation of parameters
* `rest.server.url`: The REST API URL of your SW360 instance.
* `auth.server.url`: The URL to the authenteication server of your SW360 instance.
* `username`: The username of the user with which you want to send requests to the REST API.
* `password`: The password of the user chosen above.