## Source validator
The Source validator checks the source JAR of each artifact. The source JAR can be downloaded in 
the previous enricher steps. Based on the configuration, the validator can affect the build 
due to missing source JAR or incomplete source JAR. 

### HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>Source Validator</name>
    <classHint>org.eclipse.sw360.antenna.validators.workflow.processors.SourceValidator</classHint>
    <configuration>
        <entry key="missingSourcesSeverity" value="WARN"/>
        <entry key="incompleteSourcesSeverity" value="WARN"/>
    </configuration>
</step>
```

#### Explanation of parameters
* `missingSourcesSeverity`: Specifies the behavior for missing source JAR. The values **INFO**, **WARN**, **FAIL** are accepcted.
* `incompleteSourcesSeverity`: Specifies the behavior for incomplete source JAR. The values **INFO**, **WARN**, **FAIL** are accepcted.