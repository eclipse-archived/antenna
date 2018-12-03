### Create config.xml

The `config.xml` file configures Antenna's behaviour when it handles licenses, sources and artifacts.

In the config file you can:

* define when your build will fail:
    * on incomplete sources (default false)
    * on missing sources (default false)
    * on missing license text (default false)
    * on forbidden licenses (default false)
    * on missing license information (default false)
* override values of artifacts
* define sources as valid
* set licenses as forbidden.

It is possible to have multiple such files. For example one covering
all your project and one for each single project.

If your configuration files declare conflicting configurations, an exception is thrown. The conflicts can be found in the processing report.

Possible causes for the Exception:

* Different overwrite values for one artifact
* Different licenses are declared in the setFinalLicense section
* Conflicting values for the parameters:`failOnIncompleteSources`,
`failOnMissingSources`, `failOnForbiddenLicense`,
`failOnMissingLicenseInformation`, `failOnMissingLicenseText` (the
default value for those parameters is `false`).

If `failOnMissingSources` or `failOnIncompleteSources` is defined as `true` and an Exception occurs during source validation your build will fail.

#### Overview of parameters


* `overrides`: This section can be used to override values of an
artifact.
* `addArtifact`: This section can be used to add artifacts to the
artifacts list.
* `removeArtifact`: This section can be used to define that an artifact
shall be ignored during the whole process of Antenna.
* `sourceResolving`: This section can be used to define:
    * that the source for an artifact shall not be downloaded.
* `sourceValidation`: In this section parameter for the license
validation can be set.
* `setFinalLicenses`: In this section the final license (license, that
will occure in the disclosure document) can be defined.


#### How to create a config.xml file

Overview:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="config.xsd">
	<disableP2Resolving>false</disableP2Resolving>

    <overrides>
        <override>
            <artifactSelector>
                <filename>ezmorph-1.0.6.jar</filename>
            </artifactSelector>
            <overrideValue>
                <artifact>
                    <mavenCoordinates>
                        <artifactId>test</artifactId>
                        <groupId>test</groupId>
                        <version>test</version>
                    </mavenCoordinates>
                </artifact>
            </overrideValue>
        </override>
    </overrides>

    <removeArtifact>      
        <artifactSelector>
            <filename>aopalliance-1.0.jar</filename>
        </artifactSelector>
    </removeArtifact>

    <addArtifact>
        <artifact>
            <filename>spring-beans-3.1.1.RELEASE.jar</filename>
            <mavenCoordinates>
                <artifactId>spring-beans</artifactId>
                <groupId>org.springframework</groupId>
                <version>3.1.1.RELEASE</version>
            </mavenCoordinates>
        </artifact>
    </addArtifact>

    <sourceResolving>
        <preferP2>
            <artifactSelector>
                <filename>ezmorph-1.0.6.jar</filename>
            </artifactSelector>          
        </preferP2>
        <ignoreForSourceResolving>
            <artifactSelector>
                <mavenCoordinates>
                    <artifactId>stax2-api</artifactId>
                    <groupId>org.codehaus.woodstox</groupId>
                    <version>3.1.1</version>
                </mavenCoordinates>
            </artifactSelector>
        </ignoreForSourceResolving>
    </sourceResolving>

    <sourceValidation failOnIncompleteSources="false" failOnMissingSources="false">
       <handleSourceAsValid incompleteSources="true" missingSources="true">
            <artifactSelector>
                <filename>batik-gvt-1.7.jar</filename>
            </artifactSelector>
        </handleSourceAsValid>
    </sourceValidation>

    <setFinalLicenses>
        <setFinalLicense>
            <artifactSelector>
                <filename>jcr-2.0.jar</filename>
            </artifactSelector>
            <licenseStatement operator="AND">
                <license>
                    <name>GPL-3.0</name>
                </license>
                <license>
                    <name>Apache-2.0</name>
                </license>
            </licenseStatement>
        </setFinalLicense>
    </setFinalLicenses>

 </config>
```

**artifactSelector**

In most of the sections of the configuration file, an `artifactSelector` is used to identify the required artifact.

The artifact can be identified with:

* filename
* maven coordinates
* bundle coordinates
* hash

To identify an artifact, all of the defined parameters must match.
Wildcard patterns can be used to identify a group of artifacts. For example: `org.eclipse.*` matches all artifacts starting with org.eclipse.

Example:

```xml
<artifactSelector>
    <filename>org.eclipse.*</filename>
</artifactSelector>
```

**overrides**

Values defined in the overrideValue section will replace the values of the artifact, identified by the specified `artifactSelector`.

```xml
<overrides>
<!--Sets the version of the artifact with the filename "c3p0-0.9.1.jar" to 0.9.1-->      
    <override>
    <!--ArtifactSelector that identifies the artifact of which the values will be replaced.-->
        <artifactSelector>
            <filename>c3p0-0.9.1.jar</filename>
        </artifactSelector>
        <!--New values for the specified artifact.-->          
        <overrideValue>
            <artifact>
                <mavenCoordinates>                          
                    <version>0.9.1</version>
                </mavenCoordinates>
            </artifact>
        </overrideValue>
    </override>
</overrides>
```

**removeArtifact**

Artifacts identified by an artifactSelector in the removeArtifact element will not be added to the list of artifacts, which will be resolved by Antenna.

```xml
<removeArtifact>
    <artifactSelector>
        <mavenCoordinates>
            <artifactId>commons-cli</artifactId>
            <groupId>commons-cli</groupId>
            <version>1.2</version>
        </mavenCoordinates>                         
    </artifactSelector>
</removeArtifact>
```

**addArtifact**

Artifacts declared in the `addArtifact` section will be added to the artifacts list, which will be resolved by Antenna.

These artifacts are handled in the same way as the artifacts are declared in the pom. Therefore all other configurations (final license, validation,...) must be added to the other parts of the configuration file if necessary.

If the Match is not specified, it is set to `EXACT` automatically.

```xml
<addArtifact>
    <artifact>
        <filename>aopalliance-1.0.jar</filename>
        <mavenCoordinates>   
            <artifactId>aopalliance</artifactId>
            <groupId>aopalliance</groupId>                           
            <version>1.0</version>
        </mavenCoordinates>
        <declaredLicense>
            <license><name>...</name></license>
        </declaredLicense>
        <isProprietary>false</isProprietary>
        <matchState>EXACT</matchState>
    </artifact>  
</addArtifact>
```

**licenses**

Each artifact can either have one license or more. In case we have more than one licenses, we have to specify them in the `licenseStatement` block including the operator that connects
the licenses:

```xml
<licenseStatement operator="AND">
    <license>
        <name>GPL-3.0</name>
    </license>
    <licenseStatement operator="OR">
        <license>
            <name>MIT</name>
        </license>
        <license>
            <name>Apache-2.0</name>
        </license>
    </licenseStatement>
</licenseStatement>
```

**sourceResolving**

Sources for artifacts specified in the ignore block will not be downloaded.

```xml
<sourceResolving>    
    <ignore>
        <artifactSelector>
            <filename>...</filename>
        </artifactSelector>
    </ignore>
 </sourceResolving>
```

**sourceValidation**

It can be defined if the validation will fail in the case an incomplete source, or an artifact without source is found. The default value is
`false`.

If the validation returns false, Antenna throws a MojoExecutionException and the build fails.

`handleSourceAsValid`: It is possible to define sources as valid for incomplete sources and for missing sources. The default value is `true`.

```xml
<sourceValidation  failOnIncompleteSources="false" failOnMissingSources="false">
    <handleSourceAsValid incompleteSources="true" missingSources="true">
        <artifactSelector>
            <filename>commons-configuration-1.10.jar</filename>
        </artifactSelector>
    </handleSourceAsValid>
</sourceValidation>
```

**setFinalLicenses**

In the setFinalLicenses block you can set the license for an artifact, which will be identified with the specified `artifactSelector`.

The license that is defined in this section overrides all other licenses that are found by the CLM scan.

```xml
<setFinalLicenses>
    <setFinalLicense>
        <artifactSelector>
            <filename>stax-api-1.0.1.jar</filename>
        </artifactSelector>
        <license>
            <name>Apache-2.0</name>
        </license>
    </setFinalLicense>
</setFinalLicenses>
```
