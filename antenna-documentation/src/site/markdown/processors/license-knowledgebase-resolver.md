## License Knowledgebase resolver
The License Knowledgebase resolver enriches the artifact with license information. 
The license information is obtained from the implementation of the `ILicenseManagementKnowledgeBase` interface,
e.g. `CSVBasedLicenseKnowledgeBase`.

## HowTo use
Add the following step into the `<processors>` section of your workflow.xml

```
<step>
    <name>License Knowledgebase Resolver</name>
    <classHint>org.eclipse.sw360.antenna.workflow.processors.LicenseKnowledgeBaseResolver</classHint>
</step>
```

### ILicenseManagementKnowledgeBase implementations
#### SPDXLicenseKnowledgeBase
The `SPDXLicenseKnowledgeBase` uses the `spdx-tools` to obtain the license information from the SPDX license 
database. The implementation uses especially the `ListedLicenses` class from SPDX and this retrieves the license
information from the SPDX website if possible or from the `spdx-tools` binary, which contains all license 
information.

If the `ListedLicenses` class is not able to obtain the information from the SPDX website due to a network 
problem (e.g. proxy environment), it will log an error 

`[ERROR] I/O error opening Json TOC URL, using local TOC file` 

or a warning:

`[WARNING] Unable to open SPDX listed license model.  Using local file copy for SPDX listed licenses.`

There is no reason to worry, because afterwards it will obtain the information from the `spdx-tools` binary. 

#### CSVBasedLicenseKnowledgeBase
The `CSVBasedLicenseKnowledgeBase` obtains the license information from a CSV file with the following format:
```csv
Identifier;Aliases;Name;LicenseURL;OpenSource;DeliverSources;DeliverLicense;CoveredByINSTStandardProcess;ThreatGroup
EPL-1.0;;Eclipse Public License 1.0;http://spdx.org/licenses/EPL-1.0#licenseText;;;;;
```