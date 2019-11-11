## ORT Result Analyzer
This analyzer uses the information provided in a given result file of a project that was analyzed and / or scanned with
the [*OSS Review Toolkit* (ORT)](https://github.com/heremaps/oss-review-toolkit/).  

For more information on how to use and execute ORT we refer you to its
[Getting Started](https://github.com/heremaps/oss-review-toolkit/blob/master/docs/GettingStarted.md) guide.

### How to use
Add the following step into the `<analyzers>` section of your workflow.xml

```xml 
        <step>
                    <name>Ort Result Analyzer</name>
                    <classHint>org.eclipse.sw360.antenna.ort.workflow.analyzers.OrtResultAnalyzer</classHint>
                    <configuration>
                        <entry key="base.dir" value="${project.basedir}" />
                        <entry key="file.path" value="ort/scan-result.yml" />
                    </configuration>
        </step>
```

#### Explanation of parameters
* `base.dir`: the base dir
* `file.path`: Relative path to the ort result file. 
