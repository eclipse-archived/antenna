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
package org.eclipse.sw360.antenna.ort.resolver

import com.here.ort.model.*
import com.here.ort.model.Package

import org.eclipse.sw360.antenna.model.artifact.Artifact
import org.eclipse.sw360.antenna.model.artifact.facts.*
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates
import org.eclipse.sw360.antenna.model.xml.generated.MatchState
import org.eclipse.sw360.antenna.util.LicenseSupport

import java.io.File
import java.util.function.Function

private fun mapCoordinates(pkg: Package): ArtifactCoordinates<*> {
    val namespace = pkg.id.namespace
    val name = pkg.id.name
    val version = pkg.id.version

    return when (pkg.id.type.toLowerCase()) {
        "nuget", "dotnet" -> mapDotNetCoordinates(name, version)
        "maven" -> mapMavenCoordinates(namespace, name, version)
        "npm" -> mapJavaScriptCoordinates(name, version)
        else -> mapSimpleCoordinates(name, version)
    }
}

private fun mapDotNetCoordinates(name: String, version: String): ArtifactCoordinates<*> =
    DotNetCoordinates.DotNetCoordinatesBuilder()
            .setPackageId(name)
            .setVersion(version)
            .build()

private fun mapMavenCoordinates(namespace: String, name: String, version: String): ArtifactCoordinates<*> =
    MavenCoordinates.MavenCoordinatesBuilder()
            .setGroupId(namespace)
            .setVersion(version)
            .setArtifactId(name)
            .build()

private fun mapJavaScriptCoordinates(name: String, version: String): ArtifactCoordinates<*> =
    JavaScriptCoordinates.JavaScriptCoordinatesBuilder()
            .setName(name)
            .setVersion(version)
            .setArtifactId("$name-$version")
            .build()

private fun mapSimpleCoordinates(name: String, version: String): ArtifactCoordinates<*> =
    GenericArtifactCoordinates(name, version)

private fun mapSourceUrl(pkg: Package): ArtifactSourceUrl? =
    // Antenna does not currently have the concept of VCS-specific clone URLs, so do not take ORT's VCS URL into
    // account, but only the source artifact URL.
    pkg.sourceArtifact.url.takeUnless { it.isEmpty() }?.let {
        ArtifactSourceUrl(it)
    }

private fun mapDeclaredLicense(pkg: Package): DeclaredLicenseInformation? =
    pkg.declaredLicenses.takeUnless { it.isEmpty() }?.let {
        DeclaredLicenseInformation(LicenseSupport.mapLicenses(it))
    }

private fun mapFilename(pkg: Package): ArtifactFilename? =
    // Antenna's artifact filename (or filename entries) are meant to refer to binary artifacts, so only use the
    // filename from ORT's binary artifact URL here.
    pkg.binaryArtifact.takeUnless { it.url.isEmpty() }?.let {
        val fileName = File(it.url).name
        val hash = it.hash.value
        val hashAlgorithm = it.hash.algorithm.toString()
        ArtifactFilename(fileName, hash, hashAlgorithm)
    }

private fun mapHomepage(pkg: Package): ArtifactHomepage? =
    pkg.homepageUrl.takeUnless { it.isEmpty() }?.let {
        ArtifactHomepage(it)
    }

class OrtResultArtifactResolver(result: OrtResult) : Function<Package, Artifact> {
    private val licenseFindings = result.collectLicenseFindings(false)

    private fun mapObservedLicense(pkg: Package): ObservedLicenseInformation? =
        licenseFindings[pkg.id]?.keys?.map { it.license }?.let {
            ObservedLicenseInformation(LicenseSupport.mapLicenses(it))
        }

    private fun mapCopyrights(pkg: Package): CopyrightStatement? =
        licenseFindings[pkg.id]?.keys?.flatMap { it.copyrights }
                ?.map { CopyrightStatement(it.statement) }
                ?.reduce(CopyrightStatement::mergeWith)

    override fun apply(pkg: Package): Artifact =
        Artifact("OrtResult").addFact(ArtifactMatchingMetadata(MatchState.EXACT)).also { a ->
            a.addFact(mapCoordinates(pkg))

            mapSourceUrl(pkg)?.let { a.addFact(it) }
            mapDeclaredLicense(pkg)?.let { a.addFact(it) }
            mapFilename(pkg)?.let { a.addFact(it) }
            mapHomepage(pkg)?.let { a.addFact(it) }

            mapObservedLicense(pkg)?.let { a.addFact(it) }
            mapCopyrights(pkg)?.let { a.addFact(it) }
        }
}
