# P2 Resolver

This enricher is used to download sources and jars of dependencies from p2 (eclipse) repositories.

The dependencies should be provided by a different step or some input data. 
The sources and if necessary the jar files of the dependencies are downloaded from the `repository` configured in the workflow step.

### HowTo use

Add this configuration to the workflow.xml

```xml
        <step>
            <name>P2 Resolver</name>
            <classHint>org.eclipse.sw360.antenna.p2resolver.workflow.processors.enricher.P2Resolver</classHint>
            <configuration>
                <entry key="repositories" value="path/to/repository;other/path/to/repo"/>
            </configuration>
        </step>
```

The resolver currently only supports x86_64 platforms for Windows, OSX and *nix based systems.

#### Explanation of parameters
* `repositories`: Semicolon separated list of repositories. Full paths to local repositories as well as `http(s)` based URLs are supported.

### Possible workflows

The dependencies can currently be provided in two ways:

- Provide bundle coordinates in the antennaconf
- Via maven dependencies:
    - Provide maven coordinates in the `reportData.json`
    - Use the MavenArtifactResolver to obtain jars from Maven Central
    - Use the ManifestResolver to add bundle coordinates to the artifact
    - This artifact will then be enriched by the P2 Resolver

### Known limitations

* The P2 executable uses a headless eclipse application under the hood.
Its Linux launcher is linked against glibc, hence it will not work on Linux systems which are not shipped with glibc.
The most prominent example of such a system is probably Alpine Linux commonly used for docker images.
