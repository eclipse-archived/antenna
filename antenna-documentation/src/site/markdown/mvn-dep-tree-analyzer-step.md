# Maven Dependency Tree Analyzer

This analyzer investigates the dependency tree derived from the maven [POM](https://maven.apache.org/pom.html) of the project.

This analyzer collects the maven coordinates for each dependent artifact. Collecting license information will be done in a future implementation.

### How to use

Add this configuration to the workflow.xml

```xml
        <step>
            <name>Maven Dependency Tree Analyzer</name>
            <classHint>org.eclipse.sw360.antenna.workflow.analyzers.MvnDependencyTreeAnalyzer</classHint>
        </step>
```