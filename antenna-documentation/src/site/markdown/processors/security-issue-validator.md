## Security Issue validator
The Security Issue validator checks the list of security issues of each artifact. Based on the 
configuration, the validator can affect the build due to security issue status and security 
severity.

### HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>Security Issue Validator</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.SecurityIssueValidator</classHint>
    <configuration>
        <entry key="forbiddenSecurityIssueStatusSeverity" value="FAIL"/>
        <entry key="securityIssueSeverityLimitSeverity" value="FAIL" />
        <entry key="forbiddenSecurityIssueStatuses" value="Open" />
        <entry key="securityIssueSeverityLimit" value="5.0" />
        <entry key="ignoreSecurityIssueReferences" value=""/>
    </configuration>
</step>
```

#### Explanation of parameters
* `forbiddenSecurityIssueStatusSeverity`: Specifies the behavior for forbidden security issue statuses. The values **INFO**, **WARN**, **FAIL** are accepcted.
* `securityIssueSeverityLimitSeverity`: Specifies the behavior for security issue serverity limit. The values **INFO**, **WARN**, **FAIL** are accepcted.
* `forbiddenSecurityIssueStatuses`: A comma separated String with forbidden statuses. 
The possible statuses are **Open**, **Acknowledged**, **Not Applicable** and **Confirmed**.
* `securityIssueSeverityLimit`: The limit for the validation of security issues with a given severity. All issues below the value will not be validated.
* `ignoreSecurityIssueReferences`: A comma separated String with security issue references, which will be ignored during validation.