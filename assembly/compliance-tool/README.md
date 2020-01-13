# COMPLIANCE TOOL

This is a frontend of antenna that can be used as a compliance manager tool.
This is especially targeted to create clean and approved metadata in an SW360 instance.

## SW360 Exporter
The SW360 Exporter requests all components from a given SW360 instance and filters the releases of the components to check if they have a state that shows them as "cleared" or "approved".
It then creates a list with the data of those releases and writes them sorted by creation date (from latest to oldest) in a csv file.

### Properties
- `csvFilePath`: Path and name where the csv file should be saved
- `proxyHost`: If a proxy is in use, supply the host name
- `proxyPort`: If a proxy is in use, supply the port
- `proxyUse`: If a proxy is in use, this should be set to false
- `sw360restServerUrl`: Link to the rest server url of your SW360 instance
- `sw360authServerUrl`: Link to the authentication server url of your SW360 instance
- `sw360user`: SW360 Username
- `sw360password`: SW360 User Password
- `sw360clientId`: SW360 Client Id
- `sw360clientPassword`: SW360 Client Password
