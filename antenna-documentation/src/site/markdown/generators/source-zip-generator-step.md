## Source ZIP generator
The `SourceZipWriter` generates a ZIP file in the directory specified in the configuration. 
The ZIP file contains all source JARs of the artifacts identified by the analyzers.

### How to use
Add the following step into the `<generators>` section of your workflow.xml

```xml
<step>
    <name>Source Zip Writer</name>    
    <classHint>org.eclipse.sw360.antenna.workflow.generators.SourceZipWriter</classHint>
    <configuration>
        <entry key="source.zip.path" value="${project.build.directory}/sources.zip"/>
    </configuration>
</step>
```

#### Explanation of parameters
* `source.zip.path`: Destination of the ZIP file containing all source JARs. 