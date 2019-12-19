## ORT Downloader 
The ORT Downloader processor downloads the sources of each artifact if no `ArtifactSourceFile` fact is present.

### HowTo Use
Add the following step into the `<processors>` section of your workflow.xml

```xml
<step>
    <name>ORT Downloader</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.enricher.OrtDownloaderProcessor</classHint>
</step>
```