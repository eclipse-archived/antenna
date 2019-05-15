## License Knowledgebase resolver
The License Knowledgebase resolver enriches the artifact with license information. 
The license information is obtained from the implementation of the `ILicenseManagementKnowledgeBase` interface,
e.g. `CSVBasedLicenseKnowledgeBase`.

## HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>License Knowledgebase Resolver</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.LicenseKnowledgeBaseResolver</classHint>
</step>
```