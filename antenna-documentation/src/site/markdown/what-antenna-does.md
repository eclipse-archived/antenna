# What Antenna Does

Antenna scans artifacts of a project, downloads sources for dependencies, 
validates sources and licenses and creates:

* a third-party disclosure document that lists all dependencies with 
their licenses,
* a sources.zip containing all sources of the dependencies, and
* a processing report.

![Process of Antenna](../images/antenna-process.png)

## Explanation of process

### Validation:

**Source validation**: The source jar is compared to the jar file, 
if it exists. If no jar exists, a NO_JAR message is added to the 
processing report and the sources jar is handled as valid.

**License validation**: It is checked whether a license has a license 
text and if the artifacts list contains forbidden licenses.

### Creation of disclosure document:

All information from the artifacts list is added to the disclosure 
document. The document contains a list of artifacts with filename, 
mavencoordinates and license.

The disclosure document functionality is provided by a separate module. 
If you wish to generate a disclosure document, you must make sure that
this module (named osm-antenna-disclosure-document) is installed on your 
machine.

The full license text is appended for each license.

### Creation of sources zip

All existing sources, that do not have Match State UNKNOWN or SIMILAR,
are added to the zip. The Match State can be overridden in the 
configuration file.

When using Antenna as a Maven plugin, sources will be obtained from Maven. 
However, when using Antenna as a CLI an alternative 
method is available for downloading jar files that functions without 
having Maven installed. To use it, set these properties in your 
project's pom.xml file as described in 
[How to configure Antenna](how-to-configure-antenna.html#Explanation_of_parameters).

### Attach artifacts:
It is possible to attach artifacts created by Antenna directly to the 
build job. For more information read
[How to configure Antenna](how-to-configure-antenna.html).


## System Scope

![System scope](../images/system-scope.png)

## How Antenna resolves the license

### Resolution priority

Antenna will choose among all reported licenses the license with the 
highest priority:

1. Configured license
2. Overwritten license
3. Declared license
