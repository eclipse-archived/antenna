# Antenna Docker image

In the root folder of the antenna repository there is a Dockerfile that can be used to execute the CLI Antenna tool. 
This docker file can be build with this command: 

```
docker build -t <image-name> <path-to-Dockerfile>
```

Once the image is created you have a docker image that can be used to execute the CLI Antenna tool

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