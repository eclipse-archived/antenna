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
    <classHint>org.eclipse.sw360.antenna.sw360.workflow.generators.SW360Updater</classHint>
    <configuration>
        <entry key="rest.server.url" value="http://localhost:8080/resource/api"/>
        <entry key="auth.server.url" value="http://localhost:8080/authorization/oauth"/>
        <entry key="user.id" value="admin@sw360.org"/>
        <entry key="user.password" value="12345"/>
        <entry key="client.id" value="trusted-sw360-client"/>
        <entry key="client.password" value="sw360-secret"/>
        <entry key="proxy.use" value="true"/>
        <entry key="update_releases" value="false"/>
        <entry key="upload_sources" value="false"/>
    </configuration>
</step>
```

#### Explanation of parameters
* `rest.server.url`: The REST API URL of your SW360 instance.
* `auth.server.url`: The URL to the authentication server of your SW360 instance.
* `user.id`: The username of the SW360 user to be used with the request.
* `user.password`: The password of the SW360 user.
* `client.id`: The REST API uses a two step authentication, this is general client id used.
* `client.password`: The password of the client id.
* `proxy.use`: Enable proxy for communication to SW360.
* `update_releases`: Update already existing releases 
* `update_sources`: Upload sources corresponding to releases to SW360

#### Data Model
You can find a description of the data model mapping in the [SW360 data model](../sw360-data-model.html) section.
