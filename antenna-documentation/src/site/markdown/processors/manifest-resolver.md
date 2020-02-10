## Manifest resolver
The Manifest resolver checks for each artifact if a JAR file is found as fact
and the jar file contains a MANIFEST.MF file with the properties `Bundle-SymbolicName` and `Bundle-Version`.
In this case it will add a `BundleCoordinates` fact to the artifact.

It's deactivated in the default antenna workflow configuration, i.e. it has to be activated in the workflow.xml. 
 
## HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>Manifest Resolver</name>
    <classHint>org.eclipse.sw360.antenna.maven.workflow.processors.enricher.ManifestResolver</classHint>
    <deactivated>false</deactivated>
</step>
```
