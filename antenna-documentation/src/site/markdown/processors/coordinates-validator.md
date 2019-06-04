## Coordinates validator
The Coordinates validator checks for each artifact if it contains an `ArtifactCoordinates` object.
There are different ways to specify coordinates, e.g. `MavenCoordinates` for Maven artifacts, 
`JavaScriptCoordinates` for JS components etc. 
Based on the configuration, the behavior of the validator can be controlled.

### HowTo use
Add the following step into your `<processors>` section of your workflow.xml

```
<step>
    <name>Coordinates Validator</name>
    <classHint>org.eclipse.sw360.antenna.validators.workflow.processors.CoordinatesValidator</classHint>
    <configuration>
        <entry key="failOnMissingCoordinates" value="WARN"/>
    </configuration>
</step>
```

#### Explanation of parameters
* `failOnMissingCoordinates`: Specifies the behavior of the validator. 
If any artifact fails the validation, the validator notices the artifact with the configured value. The values **INFO**, **WARN**, **FAIL** are accepcted.