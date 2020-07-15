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
is searched for by the docker cmd. In this folder all information the configuration file and workflow need
from the user should be supplied. 

Hence, if you want to have an Antenna run over your whole project, you should mount that project. 
In this configuration the antenna configuration file should always be at the root of this folder.  

The resulting files will be saved in the output directory you specified. 
This output directory needs to be the folder or a subfolder of the mounted folder. 
The docker container does not have access to anything outside of the mounted folder.   