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

### Magic string for output handlers

When configuring output handlers, the output of the CSV report generator can be referred to by

- `disclosure-sw360-doc-txt` for the txt file (if configured)
- `disclosure-sw360-doc-docx` for the docx file (if configured)
- `disclosure-sw360-doc-html` for the html file (if configured)
