# osm-antenna-2.0

Antenna scans artifacts of a project, downloads sources for dependencies, 
validates sources and licenses and creates:

* a third-party disclosure document that lists all dependencies with 
their licenses,
* a sources.zip containing all sources of the dependencies, and
* a processing report.

Learn more about Antenna in [What Antenna Does](antenna-documentation/src/site/markdown/what-antenna-does.md).

### Install and build Antenna
You need to provide some dependencies, which are not now on maven central, e.g.
- org.eclipse.sw360:datahandler:3.1.0
- org.eclipse.sw360:src-licenseinfo:3.1.0

which can be build from the sourcecode on [SW360](https://github.com/eclipse/sw360).

After all dependencies are present, you can build Antenna and deploy it in your local repository via

Please also note that some dependencies of SW360antenna are only available for Java 8. So you need to use Java 8 to build the project.

<pre>
$ <b>mvn install</b>
</pre>

You can activate the following optional profiles (`build-assembly` is active by default):
- `-P build-assembly,ci`: activates also the optional profile for continious integration and includes the system tests. 
- `-P build-assembly,experimental-steps`: activates also the profile containing some experimental steps, which may still be work in progress.
- `-P '!build-assembly'`: to not build the assembly, e.g. only build the library part of Antenna.

### Configure Antenna
Antenna can be used as a Maven plugin or standalone executable. As a 
maven plugin, Antenna's behaviour is configured by adding a `<plugin>` to your
project's `pom.xml` file and adding settings to the `<configuration>`
section. As a standalone executable is configured as an executable jar in the command line. 
Find out how to configure Antenna by reading [How to configure Antenna](antenna-documentation/src/site/markdown/how-to-configure.md). 

#### Configure Antenna for Java 9 or newer
Antenna can be used with Java versions 9 or newer.
However, it requires some additional configuration described in [Tool Configuration](antenna-documentation/src/site/markdown/tool-configuration.md/#additional-configuration-for-java-9-or-newer). 

 *To find answers in the most frequent questions/problems go to [Troubleshooting](antenna-documentation/src/site/markdown/troubleshooting.md).*
