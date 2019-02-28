## License validator
The License validator checks the list of final licenses for each artifact. The list of final
licenses is based on the `ConfiguredLicenseInformation`, `OverriddenLicenseInformation` and 
`DeclaredLicenseInformation` objects. Based on the configuration, the validator can affect the 
build due to forbidden licenses, missing license information or missing license text.

### HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>License Validator</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.LicenseValidator</classHint>
    <configuration>
        <entry key="forbiddenLicenseSeverity" value="FAIL"/>
        <entry key="missingLicenseInformationSeverity" value="WARN"/>
        <entry key="missingLicenseTextSeverity" value="WARN"/>
        <entry key="forbiddenLicenses" value=""/>
        <entry key="ignoredLicenses" value=""/>
    </configuration>
</step>
```

#### Explanation of parameters
* `forbiddenLicenseSeverity`: Specifies the behavior for forbidden licenses. The values **INFO**, **WARN**, **FAIL** are accepcted.
* `missingLicenseInformationSeverity`: Specifies the behavior for missing license information. The values **INFO**, **WARN**, **FAIL** are accepcted.
* `missingLicenseTextSeverity`: Specifies the behavior for missing license text. The values **INFO**, **WARN**, **FAIL** are accepcted.
* `forbiddenLicenses`: A comma separated string with forbidden licenses. 
* `ignoredLicenses`: A comma separated string with ignored licenses. These licenses will be ignored for the license validation. 