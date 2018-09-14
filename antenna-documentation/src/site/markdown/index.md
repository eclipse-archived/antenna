# About

Antenna scans artifacts of a project, downloads sources for dependencies, 
validates sources and licenses and creates:

* a third-party disclosure document that lists all dependencies with 
their licenses
* a sources.zip containing all sources of the dependencies
* a processing report.

![Process of Antenna](../images/antenna-process.png)


## Explanation of process

### <span style="color:#73ffdb">Phase 1</span>

This phase is done by the [Maven Dependency Tree Analyzer](mvn-dep-tree-analyzer-step.html) or/and an external tool. The tool scans your project and determines which dependencies
and licenses are being used by it. Usually this results in an scan report which contains the findings. Antenna is able to trigger this phase for you
[(see Workflow Configuration)](workflow-configuration.html).


### <span style="color:#73a9ff">Phase 2</span>

#### Download of sources and artefacts

In this phase Antenna downloads the source codes and licences of the artefacts, which are defined in the report of phase 1.
Both, the source code and the licence have to be validated.


##### Validation:

**Source validation**: The source jar is compared to the jar file, 
if it exists. If no jar exists, a NO_JAR message is added to the 
processing report and the sources jar is handled as valid.

**License validation**: It is checked whether a license has a license 
text and if the artifacts list contains forbidden licenses.


### <span style="color:#ff7873">Phase 3</span>

In the last phase Antenna generates the disclosure document and bundles all existing sources into a zip file.

#### Creation of disclosure document:

All information from the artifacts list is added to the disclosure 
document. The document contains a list of artifacts with filename,
maven coordinates and licenses.

The disclosure document functionality is provided by the `antenna-sw360-disclosure-document-generator`.

The full license text is appended for each license.

#### Creation of sources zip

All existing sources, that do not have Match State *UNKNOWN* or *SIMILAR*,
are added to the zip. The Match State can be overridden in the 
configuration file.

When using Antenna as a Maven plugin, sources will be obtained from Maven. 
However, when using Antenna as a CLI an alternative 
method is available for downloading jar files that functions without 
having Maven installed. To use it, set these properties in your 
project's pom.xml file as described in 
[How to configure Antenna](tool-configuration.html#Explanation_of_parameters).

### Attach artifacts
It is possible to attach artifacts created by Antenna directly to the 
build job. For more information read
[How to configure Antenna](tool-configuration.html#Explanation_of_parameters).


## How Antenna resolves the license

### Resolution priority

Antenna will choose among all reported licenses the license with the 
highest priority:

1. Configured license
2. Overwritten license
3. Declared license
