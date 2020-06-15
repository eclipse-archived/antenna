/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.ort.workflow.processors.enricher

import org.eclipse.sw360.antenna.model.artifact.Artifact
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile
import org.eclipse.sw360.antenna.ort.utils.ArtifactToPackageMapper
import org.eclipse.sw360.antenna.ort.workflow.processors.enricher.OrtDownloaderProcessor
import org.ossreviewtoolkit.downloader.DownloadException
import org.ossreviewtoolkit.downloader.Downloader
import org.ossreviewtoolkit.utils.encodeOrUnknown
import org.ossreviewtoolkit.utils.packZip
import org.slf4j.LoggerFactory
import java.io.File

class OrtDownloaderProcessorImpl(
        private val sourcesZipDirectory : File
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(OrtDownloaderProcessorImpl::class.java)
    }

    fun process(intermediates: MutableCollection<Artifact>): MutableCollection<Artifact> {
        intermediates.filterNot {
            it.sourceFile.isPresent
        }.forEach { artifact ->
            var pkg = ArtifactToPackageMapper().apply(artifact)
            val name = artifact.askFor(ArtifactCoordinates::class.java).takeIf {
                fact -> fact.isPresent
            }?.get()?.mainCoordinate?.canonicalize() ?: artifact.askFor(ArtifactFilename::class.java).takeIf {
                fact -> fact.isPresent
            }?.let { filename ->
                filename.get().bestFilenameEntryGuess.takeIf { filenameEntry ->
                    filenameEntry.isPresent
                }?.get()?.filename
            } ?: "unknown"
            try {
                LOGGER.debug("Download sources via ORT Downloader for '${name}'")
                val ortDownloadDirectory = createTempDir("ortDownloaderDirectory").also { it.deleteOnExit() }
                // In some cases, the protocol of a VcsInfo.url is SSH, which leads to hanging builds,
                // because it's prompting for a password. To avoid this, we are replacing the protocol from ssh
                // to https for github.com hosts.
                if (pkg.vcsProcessed.url.startsWith("ssh://git@github.com")){
                    val newUrl = pkg.vcsProcessed.url.replace("ssh://git@github.com", "https://github.com")
                    pkg = pkg.copy(vcsProcessed = pkg.vcsProcessed.copy(url = newUrl) )
                }
                val downloadResult = Downloader().download(pkg, ortDownloadDirectory)
                if (downloadResult.downloadDirectory.isDirectory) {
                    val zipFile = File(
                            sourcesZipDirectory,
                            "${pkg.id.name.encodeOrUnknown()}-${pkg.id.version.encodeOrUnknown()}.zip"
                    )
                    LOGGER.debug("Pack source directory '${downloadResult.downloadDirectory.absolutePath}' to " +
                            "'${zipFile.absolutePath}.")
                    downloadResult.downloadDirectory.packZip(zipFile)

                    if (zipFile.isFile) {
                        artifact.addFact(ArtifactSourceFile(zipFile.toPath()))
                    }
                }
            } catch (e: DownloadException) {
                LOGGER.warn("Failed to download sources for '${name}'")
            }
        }
        return intermediates
    }
}
