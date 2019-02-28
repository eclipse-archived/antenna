## License resolver
The License resolver adds for each artifact the `ConfiguredLicenseInformation`, 
if this is configured in the config.xml.


## HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>License Resolver</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.enricher.LicenseResolver</classHint>
</step>
```