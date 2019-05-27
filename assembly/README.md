# antenna-basic-assembly

This part of Antenna demonstrates how an organization would compile its own assembly.
It is build to contain a Maven plugin frontend and a CLI frontend which can be distributed via an artifact repository.

### How to use the assembly
You can use the generated artifacts as
- a maven plugin by referencing using the following code in a pom:
  ```xml
            <plugin>
                <groupId>org.eclipse.sw360.antenna</groupId>
                <artifactId>antenna-maven-plugin</artifactId>
                <version>${org.eclipse.sw360.antenna.version}</version>
                <configuration>
                    <!-- ... -->
                </configuration>
                <!-- ... -->
            </plugin>
  ```
  or
- a CLI interface by using the generated `jar` file in the `antenna-cli`-module.

### How to build your own personalized assembly
Copy the module `assembly` to your own location. Then you can modify the `configuration` to match your needs, e.g. provide your set of WorkflowSteps as dependencies and your default workflow configuration.
To adopt the names of the modules you should prepend every module name with `"<your organization name>-"`.
