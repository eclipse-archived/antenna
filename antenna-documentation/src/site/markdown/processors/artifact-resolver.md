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

#### Note

As described in this [blog post](https://www.alphabot.com/security/blog/2020/java/Your-Java-builds-might-break-starting-January-13th.html) 
a decommissioning of HTTP for several Maven repositories has taken place at the start of the year 2020. 
If you set your own `sourceRepositoryUrl` and encounter an IOException with the reason "HTTPS Required" (exception below)
it is likely that your chosen URL is an HTTP URL and belongs to the decommissioned URLs. 
To get rid of this exception change your URL to a working HTTPS URL. 

```
java.io.IOException: Reason: HTTPS Required
	at org.eclipse.sw360.antenna.util.HttpHelper.downloadFile(HttpHelper.java:49)
	at org.eclipse.sw360.antenna.maven.HttpRequester.tryFileDownload(HttpRequester.java:87)
	at org.eclipse.sw360.antenna.maven.HttpRequester.requestFile(HttpRequester.java:64)
	at org.eclipse.sw360.antenna.maven.workflow.processors.enricher.MavenArtifactResolverImpl.resolve(MavenArtifactResolverImpl.java:142)
```
