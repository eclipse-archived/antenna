# Rule development

Several helpers are available to let you easily develop rules for the rule engine.
To simplify basic setup, a  [test project](https://github.com/eclipse/antenna/tree/master/example-projects/rules-test-development-project) has been setup.

### Basic folder structure

The rules folder which is included in a basic tool run must have the following basic structure:

```
rule-folder
├───policies.properties
├───policies.xml
└───rules
    ├───FirstRule.drl
    └───SecondRule.drl
```
Therefore, it makes sense to mimic this structure inside the `src/main/resources` folder of your rule project.

In addition, you can add cucumber tests for your rules which will be explained below.

#### The policies.xml file

The file `policies.properties` contains the policy evaluations which are used by the rules.

An example for a policy evaluation can look like:
```xml
<policies>
    <policy>
        <id>Dummy</id>
        <description>Dummy A1 Policy</description>
        <severity>FAIL</severity>
    </policy>
</policies>
```

Additional policy evaluation can be added using further `<policy></policy>` tags.

* `id` defines the reference for the policy.
It must be unique across all policies as it is used in rule files to attach failed artifacts.
* `description` is a description of the policy which will be displayed when artifacts fail a rule.
* `severity`, one of (i.e. **FAIL**, **WARN** or **INFO**).

**NOTE:** The policy id will be mapped by the rule, but a rule without policy will not get executed.
There will be no warning.
This is a limitation of the Drools-Engine as currently implemented.
The only way to ensure that all rules have correct associated policy ids is to write tests for the rules.

#### The policies.properties file

The file `policies.properties` must contain two properties:

* `policies.name`: a name for the set of properties. This makes distinguishing several folders easy.
* `policies.version`: a version string. This can be any string and should be updated when changing the set of rules.
The `policies.name` and the `policies.version` are logged in the execution of the drools checker and therefore provide traceability.

### Writing rules

Rules are written as [Drools rules](https://www.drools.org/). 
This way, rules have access to the complete `Artifact` model defined in `antenna-model`.
To make access more convenient, helper functions are provided in `antenna-drools-checker` in the file `DroolsRulesUtils.java`.
These helper functions will increase in functionality over time.

Every rule should have a basic similar structure:
```java
rule "Some rule"
no-loop true
when
    a : Artifact(...)
    e : DroolsEvaluationResult( getId() == "SomeId" )
then
    modify (e) { addFailedArtifact(a) };
end
```

Taking one or more artifacts and an ID for a policy evaluation, add any artifact that fails the rule to the policy evaluation result.
The `getId()` must match an existing policy in the `policy.xml` file.
Note that the ID must exist (this is not checked at runtime but should be checked by your tests anyway).

### Writing tests for rules

A testing harness for [Cucumber](https://cucumber.io/) tests is in place.
To use it you must first add the dependencies to your parent pom (see the full `pom.xml` in the test project for an example).
In addition, you need to add the dependency on `antenna-rule-engine-testing`.

Furthermore, in `src/test`, add the following files into a package `org.eclipse.sw360.antenna.droolstesting`:

* `AddRulesAndEvaluations.java`, containing the following code:
```java
package org.eclipse.sw360.antenna.droolstesting;

import cucumber.api.java.en.When;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class AddRulesAndEvaluations {
    private WhenStepsHelpers stepsHelpers;

    public AddRulesAndEvaluations(ScenarioState state) {
        this.stepsHelpers = new WhenStepsHelpers(state);
    }

    @When("^I use the rule \"([^\"]*)\"$")
    public void i_use_the_rule(String rule) throws FileNotFoundException, AntennaException, URISyntaxException {
        stepsHelpers.iUseTheRule(rule, this.getClass());
    }
}
```
This code allows your cucumber features to find your test features.
* `RunCucumberTest.java`, containing the following code:
```java
package org.eclipse.sw360.antenna.droolstesting;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/tests/",
        glue = "org.eclipse.sw360.antenna.droolstesting"
)
public class RunCucumberTest {
    // nothing to do, this is just a wrapper to execute the tests
}
```
You can adapt the path to `features` to where your feature tests are located.
To be more concrete, this will allow running all tests automatically if the folder structure looks like this:

```
src
├───main
│   └───resources
│       └───policies
│           ├───policies.properties
│           ├───policies.xml
│           └───rules
│               ├───FirstRule.drl
│               └───SecondRule.drl
└───test
    ├───java
    │   └───org.eclipse.sw360.antenna.droolstesting
    │       ├───AddRulesAndEvaluations.java
    │       └───RunCucumberTest.java
    └───resources
        └───tests
            └───RuleTest.feature
```

If you want to develop your rules with maven, the surefire-maven plugin must be made aware of the tests by adding:
```xml
    <build>
        <testResources>
            <testResource>
                <directory>src/resources</directory>
            </testResource>
        </testResources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.0</version>
            </plugin>
        </plugins>
    </build>
```

#### Built in functionality for testing

Cucumber tests are called "Features" and divided into "Scenarios".
Each step is divided into "Given", "When", "Then" steps (see the [official documentation](https://cucumber.io))).

The following steps are available to you (this will be enhanced in the future):

##### Given steps

* `Given an artifact with`: Create an artifact with data given by the data table following the `Given` line.
This should be used if only one artifact is needed for the scenario.
* `Given an artifact called "MyArtifact" with`: Create an artifact with data given by the data table following the `Given` line.
This will internally give the artifact a name ("MyArtifact"), which makes it identifiable in the `Then`-step.
This should be used if several artifacts are given in the same scenario.

Note that several artifacts can be given by chainging with `And`, e.g. calling `And an artifact called "MyArtifact2" with`

The configuration of artifacts happens in a data table which is described below.

##### When steps

* `When I use the rule "SomeRule"`: run the rule in the file `SomeRule.drl`.
Currently, only one rule at a time can be tested.

##### Then steps

* `Then the artifact should fail with policy id "SomeId"`: This should only be used if there is one artifact.
It will pass the test if the artifact was added to an evaluation result with id "SomeId", where the evaluation result lies in the `policies.xml` file.
* `Then all artifacts fail with policy id "SomeId"`: This can be used for multiple artifacts in the scenario. 
It will pass the test if all artifacts are listed as failures in an evaluation result with id "SomeId".
* `Then no artifact fails`: Passes the test if no artifact fails any policy id.
* `Then the artifact called "MyArtifact" should fail with policy id "SomeId"`: This can be used when multiple artifacts are present but not all should fail.
It will pass the test if the artifact with name "MyArtifact" (as given in the "Given" step of the same scenario) will be listed as a failure.


#### The artifact data table

A data table to configure an artifact has a variable number of rows and columns.
However, each artifact data table must have the same number of columns for each row.
The following row entries can be configured (see also the `Mappings.java` file inside `antenna-rule-engine-testing`:

* `properietary`: The second column can be either `true` or `false`. This will mark the artifact as proprietary (or not).
* `matchstate`: The second column can be `exact`, `unknown` or `similar` matching the ArtifactMatchState.
* `license`: Add a license to the artifact. 
The license field comes with several additional configuration possibilities:
    * The second column denotes the name of the license
    * The third column can be one of `Declared`, `Observed`, `Overridden` or `Configured` matching the phase where the license was found.
    Configured licenses are the strongest corresponding to declaration in the `antennaconf.xml` (for example).
    * The fourth column can be a license threat-group, e.g. `unknown` or `strict copyleft`.
* `licenseAlternatives`: Add a compound license to the artifact. 
The second column is the name of the compound license, which consists of two license names connected by either `OR` or `AND`.
* `coordinates`: This adds coordinates to the artifact. The following columns can be specified:
    * The second column denotes the type of coordinates, currently either `maven`, `bundle`, `dotnet`, `javascript` or `generic`.
    * Depending on the type of coordinates, the next columns specify artifact ids and version or similar coordinates in descending order of generality, where the last column is the version string.
    For example, maven coordinates have three additional fields groupId, artifactId and version.
    Coordinates may contain wildcards `*` for words.
* `securityIssue`: This adds a security issue to the artifact:
    * The second column is the type of issue (e.g. `acknowledged`, `confirmed`, `not applicable` or `open`).
    * The third column adds the security level (it must be a dot-separated double).

To give an example, here is a valid setup for an artifact:
```
Given an artifact with
      | license       | EPL-2.0 | Declared      | liberal |   |
      | coordinates   | maven   | org.eclipse.* | *       | * |
      | matchstate    | similar |               |         |   |
      | securityIssue | open    | 6.1           |         |   |
```
This will configure an artifact with an EPL-2.0 license (Declared), matchstate "similar" one open security issue of severity 6.1 and maven coordinates starting with groupId starting with "org.eclipse"
