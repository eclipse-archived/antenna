# Rule engine

The rule engine is used to validate dependencies and enforce policies automatically using Drools rules.

The rule engine should run as a last processor and validate on all enriched artifacts.
Rules can be added in two ways: Either as a builtin package to your assembly or as a specially prepared folder.

### HowTo use

Add this configuration to the workflow.xml

```xml
        <step>
            <name>Drools Policy Engine</name>
            <classHint>org.eclipse.sw360.antenna.workflow.processors.AntennaDroolsChecker</classHint>
            <configuration>
                <entry key="base.dir" value="${project.basedir}"/>
                <entry key="folder.paths" value="relative/path/to/rules"/>
            </configuration>
        </step>
```

#### Explanation of parameters
* `base.dir`: Base dir to allow relative path resolution for additional rule folders.
* `folder.paths`: Semicolon separated list of paths to folders containing rules. Paths are relative to `base.dir`.
Currently, only folders of rules can be added to the policy engine, no zip files or URLs.
* The policy results have different severities **FAIL**, **WARN** and **INFO**.
If you want the build to also fail on e.g. **WARN**, add `<entry key="failOn" value="WARN"/>` to the configuration in the workflow.xml

#### Rule Development
Several helpers are available to let you easily develop rules for the rule engine. 
Here is a guide that explains how to develop the rules: [Rule Development](../rule-development.html).  