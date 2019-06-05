## Child Jar resolver
The Child Jar resolver checks for each artifact if a pathname contains more than one jar/zip/war.

## HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>Child Jar Resolver</name>
    <classHint>org.eclipse.sw360.antenna.p2.workflow.processors.enricher.ChildJarResolver</classHint>
</step>
```