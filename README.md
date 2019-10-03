# <img src="antenna-documentation/src/site/resources/images/logo.png" alt="Eclipse SW360 Antenna" width="385" height="150"/>

[![Eclipse Public License 2.0](https://img.shields.io/badge/license-EPL--2.0-green.svg "Eclipse Public License 2.0")](LICENSE)
[![Build Status](https://travis-ci.com/eclipse/antenna.svg?branch=master)](https://travis-ci.com/eclipse/antenna)
[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=sw360antenna&metric=alert_status)](https://sonarcloud.io/dashboard?id=sw360antenna)
 
Antenna scans artifacts of a project, downloads sources for dependencies, 
validates sources and licenses and creates:

* a third-party disclosure document that lists all dependencies with 
their licenses,
* a sources.zip containing all sources of the dependencies, and
* a processing report.

Learn more about Antenna in [What Antenna Does](antenna-documentation/src/site/markdown/index.md.vm).

If you want to contribute to Antenna, please check out the [Contribution guidlines](CONTRIBUTING.md). 

### Modules

The Antenna project consists of a core and multiple modules in `./modules`, which encapsulate related functionality.

Some of these folders contain their own `README.md`, like e.g. `./modules/sw360/README.md` which contain module specific information.

#### Optional Modules
Most of the modules are activated and used by default, but the p2-resolver in `./modules/p2/p2-resolver/`, which resolves OSGi sources via P2 repositories, is excluded from the build (since it complicates the build and is unnecessary in most cases).
To enable it, one can call the corresponding prepare script `./modules/p2/prepareDependenciesForP2.sh` (without Bash support one has to follow the steps in the script by hand).
To remove the P2 dependencies again you can use the script `./modules/p2/cleanupDependenciesForP2.sh`.


### Install and build Antenna

Please note that some dependencies of SW360antenna are only available for Java 8. So you need to use Java 8 to build the project.

If you want to build Antenna on the command line, just use Maven like

    $ mvn install

By default, this will run tests. If you want to skip running tests use

    $ mvn install -DskipTests

#### Optional Profiles
You can activate the following optional profiles:
- `-P integration-test`: activates also the optional profile for integration testing in the sw360 module (see also `./modules/sw360/README.md`)
- `-P site-tests`: which activates the site tests in `./documentation/`

### Configure Antenna
Antenna can be used as a Maven plugin, with  Gradle or standalone executable.
As a maven plugin, Antenna's behaviour is configured by adding a `<plugin>` to your project's `pom.xml` file and adding settings to the `<configuration>` section.
Similarly, in Gradle, the same Maven files must be given and the `build.gradle` file needs to include the Antenna configuration.
As a standalone executable, Antenna is configured as an executable jar in the command line.
Find out how to configure Antenna by reading: [How to configure Antenna](antenna-documentation/src/site/markdown/how-to-configure.md.vm).

#### Configure Antenna for Java 9 or newer
Antenna can be used with Java versions 9 or newer.
However, it requires some additional configuration described in [Tool Configuration](antenna-documentation/src/site/markdown/tool-configuration.md.vm/#additional-configuration-for-java-9-or-newer).

 *To find answers in the most frequent questions/problems go to [Troubleshooting](antenna-documentation/src/site/markdown/troubleshooting.md.vm).*
