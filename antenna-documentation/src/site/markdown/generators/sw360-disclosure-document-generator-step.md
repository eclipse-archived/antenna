# SW360 Report Generator

This report generator uses functionallity from [sw360](https://github.com/eclipse/sw360) to generate reports.

### How to use
Add this configuration to the workflow.xml

```xml
<workflow>
    <generators>
        <step>
            <name>SW360 Report Generator</name>
            <classHint>org.eclipse.sw360.antenna.workflow.generators.SW360DisclosureDocumentGenerator</classHint>
            <configuration>
                <entry key="disclosure.doc.formats" value="docx,txt,html"/>
            </configuration>
        </step>
    </generators>
</workflow>
```

#### Explanation of parameters

* `disclosure.doc.formats`: comma seperated list of output types. Available output types are:
  - `docx`
  - `txt`
  - `html`
