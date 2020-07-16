# Antenna Docker image

In the root folder of the antenna repository there is a Dockerfile that can be used to execute the CLI Antenna tool. 

## Building the Dockerfile

This docker file can be build with this command: 

```
docker build -t <image-name> <path-to-Dockerfile>
```

If you want to have a specific version or commit build, you can add it as a build argument.
This enables you to specify
 - a tag
 - a commit
 - a branch

```
docker build -t antenna-with-commit . --build-arg COMMIT=1.0.0-RC9.1
```

Once the image is created you have a docker image that can be used to execute the CLI Antenna tool

## Running the created docker image

You can run the image with the following command:

```
docker run -it \
 --mount src=<folder-with-antenna-configuration>,target=/antenna,type=bind
```

This mounts the given configuration folder in the docker container where the antenna configuration file 
is searched for by the docker cmd in the folder `\antenna`. In this folder all information the 
configuration file and workflow need from the user should be supplied.

Hence, if you want to have an Antenna run over your whole project, you should mount that project
or at least its source code. 

The resulting files will be saved in the output directory you specified. 
This output directory needs to be the folder or a subfolder of the mounted folder. 
The docker container does not have access to anything outside of the mounted folder.   

## Example Setup

Here an example structure is provided with which the docker image could be run:

```
project-to-scan
├── antennaConfiguration.xml
├── antenna-related-files (referenced from the antennaConfiguration.xml
│   ├── workflow.xml
│   ├── dependencies.csv
│   └── sources
│       └── test-component.jar
└── src (source code of project
    └── main
        ├── java
        │   └── ...
        └── resources
            └── ...
```
With this structure, the docker image could be run with this command:

```
docker run -it \
 --mount src=<path-to-project>/project-to-scan,target=/antenna,type=bind
```

If you are executing the run of the docker image in the project folder itself you can use `pwd` instead.

Another file hierarchy would be:

```
project-to-scan
├── antenna-related-files (referenced from the antennaConfiguration.xml
│   ├── antennaConfiguration.xml
│   ├── workflow.xml
│   ├── dependencies.csv
│   └── sources
│       └── test-component.jar
└── src (source code of project
    └── main
        ├── java
        │   └── ...
        └── resources
            └── ...
```

Since the image expects to find the antenna configuration file at the root of the antenna folder
`antenna/antennaConfiguration.xml` you need to specify the subfolder where your configuration file
is located in the run command. 

```
docker run -it \
 --mount src=<path-to-project>/project-to-scan,target=/antenna,type=bind antenna/antenna-related-files/antennaConfiguration.xml
``` 

If you have a project that needs to be scanned by Antenna, do not simple mount the
`antenna-related-files` folder, because then the project's source code itself would not be mounted
and a proper scan could not be executed.


