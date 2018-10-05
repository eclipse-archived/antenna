## SW360 Enricher
The `SW360Enricher` processor step currently enriches the artifact with the following two steps:

1. Checks for license information for the artifact in the SW360 database. If present, the license information of the artifact will be overwritten by that of SW360.
2. Checks if the artifact in SW360 contains information about the source download URL. If present the `ArtifactSourceUrl` is added to the artifact.

### HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```xml
<step>
    <name>SW360 Enricher</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.SW360Enricher</classHint>
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