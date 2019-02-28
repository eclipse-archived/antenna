## Manifest resolver
The Manifest resolver checks the first element of the list of path names for each artifact. If a JAR file is found 
and it contains a MANIFEST.MF file with the properties `Bundle-SymbolicName` and `Bundle-Version`, it will add a 
`BundleCoordinates` fact to the artifact.
 

## HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>Manifest Resolver</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.enricher.ManifestResolver</classHint>
</step>
```
