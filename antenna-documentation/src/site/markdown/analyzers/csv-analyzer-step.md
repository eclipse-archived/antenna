## CSV analyzer
This analyzer investigates a given *csv* file. The format of this csv file follows this format:

```
"Artifact Id","Group Id","Version","License Short Name","License Long Name","File Name","Coordinate Type","Observed License Short Name","Observed License Long Name","Source URL","Release Tag URL","Software Heritage URL"
commons-csv,org.apache.commons,1.4,Apache-2.0,Apache Software License 2.0,commons-csv.jar,mvn,Apache-2.0,Apache Software License 2.0,http://archive.apache.org/dist/commons/csv/source/commons-csv-1.4-src.zip,url,url
```

### How to use
Add the following step into the `<analyzers>` section of your workflow.xml


```xml
<step>
    <name>CSV Analyzer</name>
    <classHint>org.eclipse.sw360.antenna.workflow.analyzer.CsvAnalyzer</classHint>
    <configuration>
        <entry key="file.path" value="${basedir}/reportdata.csv" />
        <entry key="base.dir" value="${project.build.directory}/sources" />
    </configuration>
</step>
```

#### Explanation of parameters
* `file.path`: Destination of a CSV file.



