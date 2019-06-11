## ORT Result Analyzer
This analyzer investigates a given *yaml* file that was created by the [*OSS Review Toolkit* (ORT)](https://github.com/heremaps/oss-review-toolkit/) from *heremaps*.

This analyzer collects the dependencies of a given result of a project that was scanned or analyzed with ORT and has the corresponding yaml output format.  
ORT has several possible steps that can be performed. 
This analyzer only supports results created from the *Scan* and *Analyze* steps of the tool.

For more information on how to use and execute the *OSS Review Toolkit* we refer you to their [repository](https://github.com/heremaps/oss-review-toolkit/).

### How to use
Add the following step into the `<analyzers>` section of your workflow.xml

```xml 
        <step>
                    <name>Ort Result Analyzer</name>
                    <classHint>org.eclipse.sw360.antenna.ort.workflow.analyzers.OrtResultAnalyzer</classHint>
                    <configuration>
                        <entry key="base.dir"  value="${project.basedir}"/>
                        <entry key="file.path" value="ort/scan-result.yml"/>
                    </configuration>
        </step>
```

#### Explanation of parameters
* `base.dir`: the base dir
* `file.path`: Relative path to the ort result file. 
Note: The file needs to be in a yaml format, since at the time of writing this analyzer that is standard output format of the ORT result files from the Analyzer or Scanner. 
