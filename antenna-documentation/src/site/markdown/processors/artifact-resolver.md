## Maven Artifact resolver
The Maven Artifact resolver downloads the sources JAR and binary JAR for each artifact. Depending on the configuration,
it will be downloaded via the Maven command line or an HTTP request.

### HowTo Use
Add the following step into the `<processors>` section of your workflow.xml

```xml
<step>
    <name>Maven Artifact Resolver</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.enricher.MavenArtifactResolver</classHint>
    <configuration>
        <entry key="sourcesRepositoryUrl" value="https://my.url.to/repo"/>
        <entry key="preferredSourceClassifier" value="sources-ext"/>
    </configuration>
</step>
```

#### Explanation of parameters

- `sourcesRepositoryUrl`: *(optional)* valid URL to maven repository (e.g. a company nexus) containing additional source jars for resolution.
- `preferredSourceQualifier`: *(optional)* will be used by the artifact resolver as a qualifier for source jars before trying the usual qualifier `sources`.
This should be used together with `sourcesRepositoryUrl` providing a repository to search for artifacts with the given qualifier.

