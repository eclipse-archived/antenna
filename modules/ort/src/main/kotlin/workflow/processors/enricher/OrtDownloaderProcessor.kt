/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
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

import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor
import org.eclipse.sw360.antenna.model.artifact.Artifact
import workflow.processors.enricher.OrtDownloaderProcessorImpl
import java.io.File

class OrtDownloaderProcessor : AbstractProcessor() {
    companion object {
        const val ORT_DOWNLOADER_DIR = "ort-downloader-result"
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
        return OrtDownloaderProcessorImpl(sourcesZipDirectory).process(intermediates)
    }
}
