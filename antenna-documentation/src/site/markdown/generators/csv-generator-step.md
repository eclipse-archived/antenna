# CSV report generator
### How to use
Add this configuration to the workflow.xml

```xml
<workflow>
    <generators>
        <step>
            <name>CSV Report Generator</name>
            <classHint>org.eclipse.sw360.antenna.workflow.generators.CSVGenerator</classHint>
        </step>
    </generators>
</workflow>
```

### How does the output look like:
The output for the example project (`./target/antenna/Antenna_artifactInformation.csv`) looks like:

```csv
artifactName;artifactId;groupId;mavenVersion;bundleVersion;license 
;system;com.proprietary.software;1.0.0;;A proprietary License 
;commons-csv;org.apache.commons;1.4;;Apache Software License 2.0 
;log4j-core;org.apache.logging.log4j;2.6.2;;Apache License 2.0 
;commons-math3;org.apache.commons;3.2;;Apache License 2.0 
;jackson-core;com.fasterxml.jackson.core;2.8.4;2.8.4;Apache License 2.0 
;jackson-annotations;com.fasterxml.jackson.core;2.8.4;2.8.4;Apache License 2.0 
ArbitraryCopiedCode;;;;;Creative Commons Attribution Share Alike 3.0 Unported 
;system;a.test.project;1.0.0;;htmlArea-1.0 
```

### Magic string for output handlers

When configuring output handlers, the output of the CSV report generator can be referred to by `artifact-information`.
