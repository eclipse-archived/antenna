# P2 Resolver

This enricher is used to download sources and jars of dependencies from p2 (eclipse) repositories.

The dependencies should be provided by a different step or some input data. 
The sources and if necessary the jar files of the dependencies are downloaded from the `repository` configured in the workflow step.
They are copied or downloaded to the folder given by `filepath`.

### HowTo use

Add this configuration to the workflow.xml

```xml
        <step>
            <name>P2 Resolver</name>
            <classHint>org.eclipse.sw360.antenna.workflow.processors.enricher.P2Resolver</classHint>
            <configuration>
                <entry key="repositories" value="path/to/repository;other/path/to/repo"/>
                <entry key="filepath" value="path/to/folder/for/storing/sources/and/jars"/>
            </configuration>
        </step>
```

The resolver currently only supports x86_64 platforms for Windows, OSX and *nix based systems.

#### Explanation of parameters
* `repositories`: Semicolon separated list of repositories. Full paths to local repositories as well as `http(s)` based URLs are supported.
* `filepath`: Path where downloaded and resolved files will be saved during workflow step execution.

### Possible workflows

The dependencies can currently be provided in two ways:

- Provide bundle coordinates in the antennaconf
- Via maven dependencies:
    - Provide maven coordinates in the `reportData.json`
    - Use the MavenArtifactResolver to obtain jars from Maven Central
    - Use the ManifestResolver to add bundle coordinates to the artifact
    - This artifact will then be enriched by the P2 Resolver
