## Maven Dependency Tree Analyzer
This analyzer investigates the dependency tree derived from the maven [POM](https://maven.apache.org/pom.html) of the project.

This analyzer collects the maven coordinates for each dependent artifact. Collecting license information will be done in a future implementation.

### How to use
Add the following step into the `<analyzers>` section of your workflow.xml

```xml 
        <step>
            <name>Maven dependency analyzer</name>
            <classHint>org.eclipse.sw360.antenna.workflow.analyzers.MvnDependencyTreeAnalyzer</classHint>
        </step>
```