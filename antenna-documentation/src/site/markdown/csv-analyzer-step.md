# CSV analyzer

This analyzer investigates a given *csv* file. The format of this csv file follows this format:

```
"Artifact Id","Group Id","Version","License Short Name","License Long Name","File Name"
commons-csv,org.apache.commons,1.4,Apache-2.0,Apache Software License 2.0,commons-csv.jar
```


### How to use
Add this configuration to the workflow.xml

```xml
<step>
    <name>CSV Analyzer</name>
    <classHint>org.eclipse.sw360.antenna.workflow.sources.analyzer.CsvAnalyzer</classHint>
    <configuration>
        <entry key="file.path" value="${basedir}/reportdata.csv" />
        <entry key="base.dir" value="${project.build.directory}/sources" />
    </configuration>
</step>
```

#### Explanation of parameters

* `file.path`: Destination of a CSV file.


