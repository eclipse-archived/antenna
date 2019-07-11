## ORT Analyzer
This analyzer runs the Analyzer of the [*OSS Review Toolkit* (ORT)](https://github.com/heremaps/oss-review-toolkit/) on
the project.  

### How to use
Add the following step into the `<analyzers>` section of your workflow.xml

```xml 
        <step>
                    <name>Ort Analyzer</name>
                    <classHint>org.eclipse.sw360.antenna.ort.workflow.analyzers.OrtAnalyzer</classHint>
                    <configuration>
                        <entry key="ignore.tool.versions" value="true" />
                        <entry key="allow.dynamic.versions" value="true" />
                        <entry key="use.clearly.defined.curations" value="true" />
                    </configuration>
        </step>
```

#### Explanation of parameters
* `ignore.tool.versions`: Whether to ignore the versions of third-party tools used by the ORT Analyzer or not.
    Defaults to `false`.
* `allow.dynamic.versions`: Whether to allow projects with dynamic dependency version declarations to be analyzed, e.g.
    NPM projects without a lock file, or not. Defaults to `false`.
* `use.clearly.defined.curations`: Whether to query the ClearlyDefined service for curations for meta-data of analyzed
    packages or not. Defaults to `false`.
