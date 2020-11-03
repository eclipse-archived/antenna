
## <a name="SW360Updater">SW360 Updater</a>

The SW360Updater takes a given csv file with release
information and maps the contents on SW360Release objects
that are posted into the given SW360 instance. 
It has the ability to update and approve release information of already existing releases.


To approve a release the `Clearing State` must be set according to the description in the [SW360 Data Model](../sw360-data-model.html).
A functionality to simply add information without approving it was provided by having a `Clearing State` called `WORK_IN_PROGRESS`.
This allows to add and change already existing information easily. It is important to note that information of releases that
has already been approved can not be changed anymore, even if the `Clearing State` is set back to `WORK_IN_PROGRESS`.

### Properties
Specific properties that need to be set for sources and clearing document handling:

- `sourcesDirectory`: Directory where the sources for uploading are stored
- `removeClearedSources`: A boolean property that controls whether the updater should remove the source attachments of a release from the local sources directory once the release has been cleared. Cleared releases are no longer relevant for the workflow of the compliance tool; so by setting this property to *true*, an automatic cleanup of the sources directory can be enabled. The default value is *false*.
- `removeClearingDocuments`: A boolean property that controls whether clearing documents for releases should be removed after the release has been cleared. The default value is *false*.
- `clearingDocDir`: Directory in which clearing documents are generated

Properties required to correctly parse the given csv file:

- `delimiter`: Delimiter used in the csv file to separate columns (by default it is `,`)
- `encoding`: Encoding of the csv file, normally `UTF-8` 

SW360 specific properties to determine in which way the new metadata is updated:

- `sw360updateReleases`: Boolean value that determines if release data is patched should new information be added to an already existent release
- `sw360uploadSources`: Boolean value that determines if source files of a release are uploaded should they be present
- `sw360deleteObsoleteSources`: Boolean value that determines if the updater should delete existing source attachments before uploading a new one. Typically, there is only a single source attachment. By setting this property to *true*, a new source attachment replaces any existing ones. As SW360 supports multiple source attachments, it is also possible to configure the updater to upload new source attachments in addition to existing ones. Be aware, however, that SW360 currently does not allow overriding an existing source attachment with a file having the same name.
