/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.ort.workflow.processors.enricher

import com.here.ort.downloader.DownloadException
import com.here.ort.downloader.Downloader
import com.here.ort.utils.encodeOrUnknown
import com.here.ort.utils.packZip
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException
import org.eclipse.sw360.antenna.model.artifact.Artifact
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile
import org.slf4j.LoggerFactory
import org.eclipse.sw360.antenna.ort.utils.ArtifactToPackageMapper
import java.io.File
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename

class OrtDownloaderProcessor : AbstractProcessor() {
    companion object {
        const val ORT_DOWNLOADER_DIR = "ort-downloader-result"
        private val LOGGER = LoggerFactory.getLogger(OrtDownloaderProcessor::class.java)
    }

    init {
        workflowStepOrder = 350
    }

    private lateinit var sourcesZipDirectory: File

    override fun configure(configMap: MutableMap<String, String>) {
        sourcesZipDirectory = context.toolConfiguration.antennaTargetDirectory.resolve(ORT_DOWNLOADER_DIR).toFile()
        if (!sourcesZipDirectory.isDirectory && !sourcesZipDirectory.mkdirs()) {
            throw ConfigurationException("Failed to create directory '${sourcesZipDirectory.absolutePath}' " +
                    "for ORT Downloader result. ")
        }
    }

    override fun process(intermediates: MutableCollection<Artifact>): MutableCollection<Artifact> {
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
