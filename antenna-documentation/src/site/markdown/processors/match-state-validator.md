## Match State validator
The Match State validator checks the `MatchState` of each artifact. Based on the configuration, 
the validator can affect the build for `MatchState.SIMILAR` and `MatchState.UNKNOWN`. The `MatchState`
specifies the equality between the binary JAR of the artifact and the JAR in the database e.g. SW360.

### HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>Match State Validator</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.MatchStateValidator</classHint>
    <configuration>
        <entry key="severityOfSIMILAR" value="INFO"/>
        <entry key="severityOfUNKNOWN" value="WARN"/>
    </configuration>
</step>
```

#### Explanation of parameters
* `severityOfSIMILAR`: Specifies the behavior for `MatchState.SIMILAR`. The values **INFO**, **WARN**, **FAIL** are accepcted.
* `severityOfUNKNOWN`: Specifies the behavior for `MatchState.UNKNOWN`. The values **INFO**, **WARN**, **FAIL** are accepcted.