## Summary
The project should serve as an example project that shows how antenna is configured and used.

## How to Build 

You need a Maven installation up and running. Please have a look at the project site to download the binaries and 
get a tutorial on how to setup the tool (<https://maven.apache.org/>). You need to point the JAVA_HOME variable to an installed
JDK (JRE if you do not have any java dependencies in your project to resolve).

To build the example project execute the following maven command in the root folder of the project (e.g. 'example-project/')

     mvn clean package
     
The project is build and in its analyze phase the Antenna maven plugin is executed, which reads entries from a json report and generates the source code bundle and the attribution document. You can find both artifacts in 

     target/antenna-maven-plugin
     
## Customize configuration
To customize the configuration you are most likely want to adjust the product, version and the components enumerated in the attribution document. 

To adjust the product name open the pom.xml and search for the tags
     
     <productName>
     <productFullname>
 
and adjust them accordingly.

The product version is configured in the main pom.xml. It can be found the project main folder. Open the file and adjust
     
     <version>

In case the input contains some wrong component entries you can adjust them with the Antenna configuraton file found in src/antennaconf.xml. how to use the configuration options
is documented in the accompanying example file. To find out the correct abbreviation of license types, please have a look
into the mapping file of the antenna-knowledgebase project

    TODO ADD link to antenna-knowledgebase module
    
### Expected result when running the example

Content of the example

The example processes several sources of component information
1. reportData.json
    - commons-lang3
    - jackson-core
    - jackson-annotations
    - commons-math3
2. dependencies.csv
    - commons-csv
3. antennaconf.xml
    - removes 
      - commons-lang-2.0.jar
    - adds
      - ArbitraryCopiedCode
      - log4j-core
      - com.proprietary.software
      - a.test.project.system

Running the example should result in the following artifacts:

1. 3rdparty-licenses.pdf
    - Entries:
      - jackson-core
      - jackson-annotations
      - commons-math3
      - commons-csv
      - ArbitraryCopiedCode
      - log4j-core
      - com-proprietary.software
      - a-test.project.system 
2. Antenna_3rdPartyAnalysisReport.txt
   - 
3. 3rdparty-licenses.html 
4. sources.zip
5. Antenna_3rdPartyAnalysisReport.txt
6. Antenna_artifactInformation.csv