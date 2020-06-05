
## <a name="SW360Updater">SW360 Updater</a>

The SW360Updater takes a given csv file with release
information and maps the contents on SW360Release objects
that are posted into the given SW360 instance. 
It has the ability to update release information of already existing releases. 

### Properties
- `targetDir`: Target directory in which artificial clearing reports are created in. 
- `sourcesDirectory`: Directory where the sources for uploading are stored
- `basedir`: Base directory of the execution
- `csvFilePath`: Path and name to the csv file with the release information
- `delimiter`: Delimiter used in the csv file to separate columns (by default it is `,`)
- `encoding`: Encoding of the csv file, normally `UTF-8` 
- `removeClearedSources`: A boolean property that controls whether the updater should remove the source attachments of a release from the local sources directory once the release has been cleared. Cleared releases are no longer relevant for the workflow of the compliance tool; so by setting this property to *true*, an automatic cleanup of the sources directory can be enabled. The default value is *false*.
- `removeClearingDocuments`: A boolean property that controls whether clearing documents for releases should be removed after the release has been cleared. The default value is *false*.
- `proxyHost`: If a proxy is in use, supply the host name
- `proxyPort`: If a proxy is in use, supply the port
- `proxyUse`: If a proxy is in use, this should be set to true
- `sw360restServerUrl`: Link to the rest server url of your SW360 instance
- `sw360authServerUrl`: Link to the authentication server url of your SW360 instance
- `sw360user`: SW360 Username
- `sw360password`: SW360 User Password
- `sw360clientId`: SW360 Client Id
- `sw360clientPassword`: SW360 Client Password
- `sw360updateReleases`: Boolean value that determines if release data is patched should new information be added to an already existent release
- `sw360uploadSources`: Boolean value that determines if source files of a release are uploaded should they be presen
