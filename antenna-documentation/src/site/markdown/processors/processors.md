## Processors
The second phase of the workflow pipeline continues with processors. The processor phase consists of enricher and validators.
The enrichers enrich the artifacts with missing information by means of external interfaces such as e.g. SW360. The validators validate
specific attributes of the artifacts. Aside from a processor enriching artifact data with data from the `antennaconf.xml`, there are a number of processors possible for Antenna:

### Enrichers
* [Maven Artifact resolver](./artifact-resolver.html)
* [Child Jar resolver](./child-jar-resolver.html)
* [License Knowledgebase resolver](./license-knowledgebase-resolver.html)
* [License resolver](./license-resolver.html)
* [Manifest resolver](manifest-resolver.html)
* [P2 Resolver](./p2-resolver.html)
* [SW360 Enricher](./sw360-enricher.html)

### Validators
* [Coordinates validator](./coordinates-validator.html)
* [Source Validator](./source-validator.html)
* [License Validator](./license-validator.html)
* [Match State Validator](./match-state-validator.html)
* [Security Issue Validator](./security-issue-validator.html)
* [Rule Engine](./rule-engine.html)

> **Note**:  
> Processors that check for compliance (e.g. the rule engine or any validator) might fail due to data errors (e.g. a rule violation).
> To prevent the Antenna build from failing when there is no configuration error and still produce all output products
> the Antenna run will fail after the [generator steps](./../generators/generators.html) have run if any evaluation results
> have a severity that is equal or higher to the `failOn` value that by default is set to`FAIL`.
> There will be a list of fails causing exceptions in the workflow steps along with an `ExecutionException`.

  
> *Note*: If you want to have a validator fail at any severity lower than `FAIL`, you need to add the 
>         `failOn` entry to the configuration of the desired workflow step. 