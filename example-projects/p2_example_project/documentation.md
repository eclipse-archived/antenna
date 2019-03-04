## The P2 example project

The example project demonstrates a common workflow for writing an eclipse product:
- All dependencies are gathered in a single dependency project which is run prebuilt and outputs a repository containing all dependencies in correct versions and attached sources.
- The build then generates a second repository which contains everything necessary to run the product.

In addition, there are several optional ideas that are not used in the example project:
- In order to define the dependencies, the project could define a target platform which uses the dependency project for resolution, defining the runtime as well as the development environment.
- In order to define the deployment, features and/or products may be defined directly and then just deployed in the repository using the category.xml file instead of deploying the artifacts directly.

## Building the project 

- In the subfolder `dependency_project`, call `mvn package` (this is the folder that will contain all dependencies including sources)
- In the project folder, call `mvn package` (or any maven lifecycle step). 

This will then use the repository created in the `dependency_project` as source. It will also create a "deployment" repository in the subfolder `repository/target`.
