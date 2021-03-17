

THIS PROJECT HAS BEEN ARCHIVED


# <img src="antenna-documentation/src/site/resources/images/logo.png" alt="Eclipse SW360 Antenna" width="385" height="150"/>

[![Eclipse Public License 2.0](https://img.shields.io/badge/license-EPL--2.0-green.svg "Eclipse Public License 2.0")](LICENSE)
[![Build Status Master](https://github.com/eclipse/antenna/workflows/Antenna%20Build/badge.svg?branch=master)](https://github.com/eclipse/antenna/actions?query=workflow%3A%22Antenna+Build%22)
[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=sw360antenna&metric=alert_status)](https://sonarcloud.io/dashboard?id=sw360antenna)

[![Slack Channel](https://img.shields.io/badge/slack-sw360antenna--talk-blue.svg?longCache=true&logo=slack)](https://join.slack.com/t/sw360chat/shared_invite/enQtNzg5NDQxMTQyNjA5LThiMjBlNTRmOWI0ZjJhYjc0OTk3ODM4MjBmOGRhMWRmN2QzOGVmMzQwYzAzN2JkMmVkZTI1ZjRhNmJlNTY4ZGI)
 
Antenna scans artifacts of a project, downloads sources for dependencies, 
validates sources and licenses and creates:

* a third-party attribution document that lists all dependencies with 
their licenses,
* a sources.zip containing all sources of the dependencies, and
* a processing report.

Learn more about Antenna in [What Antenna Does](antenna-documentation/src/site/markdown/index.md.vm).  
Or visit us on our [Eclipse Project Site](https://www.eclipse.org/antenna/) for the latest news of the project.

If you want to contribute to Antenna, please check out the [Contribution guidlines](CONTRIBUTING.md). 

### Modules

The Antenna project consists of a core and multiple modules in `./modules`, which encapsulate related functionality.

Some of these folders contain their own `README.md`, like e.g. `./modules/sw360/README.md` which contain module specific information.

### Install and build Antenna

Please note that some dependencies of SW360antenna are only available for Java 8. So you need to use Java 8 to build the project. 

If you want to build Antenna on the command line, just use Maven like

    $ mvn install

By default, this will run tests. If you want to skip running tests use

    $ mvn install -DskipTests

The build is stable starting with Maven version `3.5.x` or higher, which was 
when the [usage of the `revision` property](https://maven.apache.org/maven-ci-friendly.html) was introduced. 
With a lower version you will get errors when trying to resolve dependencies created in the Antenna build,
since the version is not correctly parsed. A possible error message would look like this:

```  
Failed to process POM for org.eclipse.sw360.antenna:model:jar:1.0.0-SNAPSHOT: 
Non-resolvable parent POM for org.eclipse.sw360.antenna:model:1.0.0-SNAPSHOT: 
Could not find artifact org.eclipse.sw360.antenna:antenna-management:pom:${revision} 
in central (https://repo.maven.apache.org/maven2/) and 'parent.relativePath' points at wrong local POM
[ERROR] org.eclipse.sw360.antenna:model:jar:1.0.0-SNAPSHOT
```

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

### Documentation

For more information please refer to our [documentation](https://eclipse.github.io/antenna/).
