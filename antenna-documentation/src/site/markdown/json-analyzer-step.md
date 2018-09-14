# JSON analyzer

This analyzer investigates a given *json* file. The json file contains the components of the project. The component consists
of a hash value, maven coordinates, path to the file, proprietary state and license data. An example declaration can be seen below.


```json
{
  "components": [
    {
      "hash": "b2921b7862e7b26b43aa",
      "componentIdentifier": {
        "coordinates": {
          "artifactId": "commons-lang3",
          "groupId": "org.apache.commons",
          "version": "3.5"
        }
      },
      "proprietary": false,
      "matchState": "exact",
      "pathnames": [
        "commons-lang-2.0.jar"
      ],
      "licenseData": {
        "declaredLicenses": [
          {
            "licenseId": "Apache-2.0",
            "licenseName": "Apache License, v2.0"
          }
        ],
        "observedLicenses": [
          {
            "licenseId": "Apache-1.1",
            "licenseName": "Apache License, v1.1"
          }
        ],
        "overriddenLicenses": []
      }
    }
  ]
}
```

#### Explanation of parameters

* `components`: Array of different components.
* `hash`: Describes the component with a unique hash value.
* `componentIdentifier`: This object holds the **coordinates** object
* `coordinates`: This object consists of the properties **artifactId**, **groupId** and **version**.
* `artifactId`: Generally the name that the project is known by.
* `groupId`: Generally unique name amongst an organization or project is known by.
* `version`: The version number of the component
* `proprietary`: Is the component a non-free software or not.
* `matchState`: Verify if the comparison of component to known components is or is not a match.
* `pathnames`: Paths to the components binaries.
* `licenseData`: This object holds the arrays **declaredLicenses**, **observedLicenses** and **overriddenLicenses**
* `declaredLicenses`: Any license(s) that has been declared by the author.
* `observedLicenses`: Any license(s) found during the scan of the component’s source code.
* `overriddenLicenses`: Any license(s) which should replace one of the above.


### How to use
Add this configuration to the workflow.xml

```xml
<step>
    <name>JSON Analyzer</name>
    <classHint>org.eclipse.sw360.antenna.workflow.sources.analyzer.JsonAnalyzer</classHint>
    <analyzerConfiguration>
        <entry key="file.path" value="${basedir}/ClmReportData.json" />
        <entry key="base.dir" value="${project.build.directory}/sources" />
    </configuration>
</step>
```

#### Explanation of parameters

* `file.path`: Destination of a JSON file that matches the above format.
* `base.path`: Destination to the the source files that the JSON report refers to.