## Analyzers
The first phase of the workflow pipeline begins with the analyzers.
They all provide a list of artifact information, which is consolidated into a big list and passed to the next phase of the workflow pipeline.
Antenna already provides the three following analyzers aside from an analyzer adding artifacts from the `antennaconf.xml`:

- [CSV analyzer](./csv-analyzer-step.html)
- [JSON analyzer](./json-analyzer-step.html)
- [Maven Dependency Tree analyzer](./mvn-dep-tree-analyzer-step.html)
- [ORT Result analyzer](./ort-result-analyzer-step.html)