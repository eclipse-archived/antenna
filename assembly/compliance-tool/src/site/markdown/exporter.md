## <a name="SW360Exporter">SW360 Exporter</a>

The SW360 Exporter requests all components from a given SW360 instance
and filters the releases of the components to check if they have a state
that shows them as "cleared" or "approved".
It then creates a list with the data of non-cleared releases and writes them
sorted by creation date (from latest to oldest) in a csv file.

The csv file follows the csv format accepted by the [CSV Analyzer](../analyzers/csv-analyzer-step.html). 

Should there already be a csv file of the same name in the target directory, 
the SW360 Exporter will overwrite it. 

### Properties
Sources specific properties:

- `sourcesDirectory`: Directory where the sources downloaded are stored
- `removeUnreferencedSources`: A boolean property that controls whether the exporter should do some cleanup on the sources directory. If set to *true*, the exporter checks after the download of sources whether the directory contains any files that are not referenced by any of the components that have been written to the CSV file. Such files are then removed, so that the directory contains only the sources of components that are currently in focus. The default value of this flag if *false*.