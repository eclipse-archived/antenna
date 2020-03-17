## CycloneDX Bill-of-Material Generator

### Purpose

This generator uses the artifacts collected and enriched by Antenna to create a Bill-of-Material (BOM) in [CycloneDX](https://cyclonedx.org/) format.
The result is a file called ``cyclonedx.bom.xml`` and can be found in the Antenna's target directory.
Using that, further processing of information from Antenna can be done in tools supporting this format.

As example, when using [OWASP Dependency-Track](https://owasp.org/www-project-dependency-track/) the information could be used to report on security vulnerabilities for the collected artifacts.

### How to use
Simply add this configuration to ``workflow.xml``:

```xml
<workflow>
    <generators>
        <step>
            <name>CycloneDX Bill-of-Material Generator</name>
            <classHint>com.eclipse.sw360.antenna.cyclonedx.CycloneDXGenerator</classHint>
        </step>
    </generators>
</workflow>
```

