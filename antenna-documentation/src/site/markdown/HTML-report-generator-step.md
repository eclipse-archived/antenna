# HTML report generator
This workflow step generates a file `./target/antenna/3rdparty-licenses.html` containing a description of the licensing situation.

### How to use
Add this configuration to the workflow.xml

```xml
<workflow>
    <generators>
        <step>
            <name>HTML Report Generator</name>
            <classHint>org.eclipse.sw360.antenna.workflow.generators.HTMLReportGenerator</classHint>
        </step>
    </generators>
</workflow>
```