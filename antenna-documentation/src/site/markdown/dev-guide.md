# Overview
Antenna is a tool to automate your open source license compliance processes as much as possible. In the end that is 
 
 - collecting all compliance relevant data
 - process that data and warn if there might be any license compliance related issues
 - generating a set of compliance artifacts (source code bundle, disclosure document, report) for your project. 
 
 To reflect those three different types of tasks Antenna is built arround a workflow engine, which allows to orchestrate a set of ```analyzers``` to gather required information, ```processors``` to arrange, adjust and evaluate that data, and a set of ```generators``` to produce a set of compliance related artifacts.
 
 Since licensing issues can deeply affect the success of your project it is required to be notified about any issues as early as possible in the development process. It is therefore useful to generate that information directly within your build. Antenna can directly be integrated into the build process. This is realized with several so called ```frontends``` to build systems, which allow to invoke the tool and provide it with necessary configuration.
 
 
 The Antenna project is set up in a way that allows to easily create a custom configuration with a preconfigured set of shipped and custom analyzers, processors and generators to fit the needs of your internal compliance processes. E.g. You might use a commercial tool to analyze your dependencies and do not rely on the results of the maven dependency plugin. In that case you can provide a custom analyzer implementation, provide a custom configuration and bundle that as the tool, which can be used by your development teams to scan their projects. The [example projects](https://github.com/bsinno/osm-antenna-2.0/tree/master/example-projects) show how to create such a custom configuration.
 
 
 
### Project structure
 
 Currently, the project is restructured. So this is WIP.
 
 However, currently it is like:
 frontends:
 - ```antenna-cli```
 - ```antenna-maven-plugin```
 
 ```antenna-assembly``` builds the jar for the cli version of antenna.
 
 all the functionality is more or less implemented in ```antenna-core```.
 
 ```antenna-api``` is about to contain the public api of antenna that can be used to add new functionality. Unfortunately, something went terribly wrong in the past and what this module partially contains is not a publc api but a set of interfaces that are of not much help. This needs to be cleaned up.
 
 ```antenna-disclosure-document``` contains the templates and files to generate the pdf disclosure document. Unfortunately, the code that does this is not contained here but in antenna-core.
 
 ```antenna-model``` contains a xsd file that describes more or less the domain model of the application. However, it is not that clear. Some of the classes are solely used in the antenna config others are also in the code. Needs to be cleaned up.
 
 ```antenna-sw360``` is a connector to connect to an sw360 instance to fetch data from there and write the current state of the project to this instance.
 
 ```antenna-testing``` is a pom file that configures the antenna-maven-plugin so that it runs antenna on the antenna project itself. This needs to be replaced with a proper example project.
 
 ```antenna-testing-commons``` provides some common functionality to setup antenna projects and test them.
 
 
 
 


# Development Guide

## Setup

Required tools:

- Maven

If your machine is located behind a proxy keep in mind to configure your proxy settings accordingly. For maven this can be done in `~/.m2/settings.xml`. Gradle needs a `gradle.properties` file in `~/.gradle`.

## How to build

You can build antenna by running

```
mvn clean install
```

on your command line.


To run the application you can have a look into the example project, which shows in its pom xml how to properly configure the antenna-maven-frontend.


## How to debug
To debug antenna it is advisable to use the debug option of maven. For that run your example project with
```
mvnDebug clean package
```
and configure a remote debugger that hooks to the running vm of the example project. For Intelij Idea this is described here https://www.jetbrains.com/help/idea/run-debug-configuration-remote-debug.html

### Antenna Debug mode 

Antenna provides also some further means to debug. If you run e.g. the example project with the with the parameter  `-Ddebug`Antenna will not delete any intermediate results so that they will be available for later inspection. 

## Build the documentation

Just type in your command `$ mvn site:run` .
The site starts up in the port 9000 

Open a browser and type the directory `http://localhost:9000`

## How to implement a custom worflow item

Antenna uses a pipeline workflow. The **sources** yield **Artifact**s that are
processed by the **processors**. After processing the **sinks** produce an
output from the processed **Artifact**s.

The sources and sinks can be chosen completely by the user. There are some
fixed processors that will apply the configuration and add, delete or modify
Artifacts. Also there is the possibility to add further processors to the
workflow.

In order to implement those WorkflowItems your class needs to extend the
corresponding abstract base class from the api module.

* Sources extend org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer
* Processors extend org.eclipse.sw360.antenna.api.workflow.AbstractProcessor
* Sinks extend org.eclipse.sw360.antenna.api.workflow.AbstractGenerator

Common to all three components is the way how they should be instantiated.
There needs to be a default constructor and a configure method.
Since the components are instantiated via reflection the default constructor is used. Followed by a call to the configure method with a Map<String, String> containing the configuration.
See how to configure Antenna to see how the configuration is passed.

