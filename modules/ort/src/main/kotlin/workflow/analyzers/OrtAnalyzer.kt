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

import com.here.ort.analyzer.Analyzer
import com.here.ort.analyzer.PackageManager
import com.here.ort.analyzer.curation.ClearlyDefinedPackageCurationProvider
import com.here.ort.model.config.AnalyzerConfiguration

import org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult
import org.eclipse.sw360.antenna.model.artifact.Artifact
import org.eclipse.sw360.antenna.ort.resolver.OrtResultArtifactResolver

import org.slf4j.LoggerFactory

import java.io.File
import java.io.IOException

import kotlin.properties.Delegates

class OrtAnalyzer : AbstractAnalyzer() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(OrtAnalyzer::class.java)

        /**
         * Whether to ignore the versions of third-party tools used by the ORT Analyzer or not.
         */
        private val IGNORE_TOOL_VERSIONS_KEY = "ignore.tool.versions"

        /**
         * Whether to allow projects with dynamic dependency version declarations to be analyzed, e.g. NPM projects
         * without a lock file, or not.
         */
        private val ALLOW_DYNAMIC_VERSIONS_KEY = "allow.dynamic.versions"

        /**
         * Whether to query the ClearlyDefined service for curations for meta-data of analyzed packages or not.
         */
        private val USE_CLEARLY_DEFINED_CURATIONS_KEY = "use.clearly.defined.curations"
    }

    init {
        workflowStepOrder = 700
    }

    private var ignoreToolVersions by Delegates.notNull<Boolean>()
    private var allowDynamicVersions by Delegates.notNull<Boolean>()
    private var useClearlyDefinedCurations by Delegates.notNull<Boolean>()

    override fun configure(configMap: MutableMap<String, String>) {
        ignoreToolVersions = getBooleanConfigValue(IGNORE_TOOL_VERSIONS_KEY, configMap)
        allowDynamicVersions = getBooleanConfigValue(ALLOW_DYNAMIC_VERSIONS_KEY, configMap)
        useClearlyDefinedCurations = getBooleanConfigValue(USE_CLEARLY_DEFINED_CURATIONS_KEY, configMap)
    }

    override fun yield() = WorkflowStepResult(runOrtAnalyzer(context.project.basedir))

    override fun getName() = "Ort"

    @Throws(IOException::class)
    fun runOrtAnalyzer(inputDir: File): Collection<Artifact> {
        val absoluteInputDir = inputDir.absoluteFile

        LOGGER.debug("Running ORT Analyzer on the project in directory '$absoluteInputDir'.")

        val analyzerConfig = AnalyzerConfiguration(ignoreToolVersions, allowDynamicVersions)
        val analyzer = Analyzer(analyzerConfig)
        val curationProvider = ClearlyDefinedPackageCurationProvider().takeIf { useClearlyDefinedCurations }
        val result = analyzer.analyze(absoluteInputDir, PackageManager.ALL, curationProvider, null)

        result.analyzer?.let {
            if (it.result.hasErrors) {
                val errors = it.result.collectErrors()

                LOGGER.warn("The ORT Analyzer had ${errors.size} issue(s):")

                errors.forEach { (id, issues) ->
                    LOGGER.warn("$id: ${issues.joinToString("\n")}")
                }
            }
        } ?: throw IOException("No analyzer run found in ORT result file.")

        val resolver = OrtResultArtifactResolver(result)
        return result.getPackages().map { (pkg, _) -> resolver.apply(pkg) }.toSet()
    }
}
