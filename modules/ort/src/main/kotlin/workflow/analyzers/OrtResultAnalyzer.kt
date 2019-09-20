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
package org.eclipse.sw360.antenna.ort.workflow.analyzers

import com.here.ort.model.OrtResult
import com.here.ort.model.readValue

import org.eclipse.sw360.antenna.api.exceptions.AntennaException
import org.eclipse.sw360.antenna.api.workflow.ManualAnalyzer
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult
import org.eclipse.sw360.antenna.model.artifact.Artifact
import org.eclipse.sw360.antenna.ort.resolver.OrtResultArtifactResolver

import org.slf4j.LoggerFactory

import java.io.File
import java.io.IOException

class OrtResultAnalyzer : ManualAnalyzer() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(OrtResultAnalyzer::class.java)
    }

    init {
        workflowStepOrder = 700
    }

    @Throws(AntennaException::class)
    override fun yield(): WorkflowStepResult =
        try {
            WorkflowStepResult(createArtifactList(componentInfoFile))
        } catch (e: IOException) {
            throw AntennaException("Error parsing the ORT result file: ${e.message}")
        }

    override fun getName() = "OrtResult"

    @Throws(IOException::class)
    fun createArtifactList(ortResultFile: File): Collection<Artifact> {
        LOGGER.debug("Creating artifact list from ORT result file '$ortResultFile'.")

        val result = ortResultFile.readValue<OrtResult>()
        if (result.analyzer == null) throw IOException("No analyzer run found in ORT result file.")

        val resolver = OrtResultArtifactResolver(result)
        return result.getPackages().map { (pkg, _) -> resolver.apply(pkg) }.toSet()
    }
}
