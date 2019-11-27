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
package org.eclipse.sw360.antenna.ort.utils

import com.here.ort.model.*
import org.eclipse.sw360.antenna.model.artifact.Artifact
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactVcsInfo
import java.util.function.Function

const val MAVEN_TYPE_KEY = "Maven"
const val NPM_TYPE_KEY = "NPM"
const val NUGET_TYPE_KEY = "nuget"
const val UNMANAGED_TYPE_KEY = "Unmanaged"

class ArtifactToPackageMapper : Function<Artifact, Package> {
    override fun apply(artifact: Artifact): Package {
        return Package(
                id = mapIdentifier(artifact) ?: Identifier.EMPTY,
                declaredLicenses = sortedSetOf(),
                description = "",
                homepageUrl = "",
                binaryArtifact = RemoteArtifact.EMPTY,
                sourceArtifact = mapSourceRemoteArtifact(artifact) ?: RemoteArtifact.EMPTY,
                vcs = mapVcsInfo(artifact) ?: VcsInfo.EMPTY
        )
    }

    private fun mapIdentifier(artifact: Artifact): Identifier? =
            artifact.askFor(ArtifactCoordinates::class.java).takeIf { it.isPresent }?.let {
                val coordinates = it.get()
                val type = coordinates.mainCoordinate.type.toLowerCase()
                val namespace = coordinates.mainCoordinate.namespace ?: ""
                val name = coordinates.mainCoordinate.name ?: ""
                val version = coordinates.mainCoordinate.version ?: ""
                val ortType = when (type) {
                    "maven" -> MAVEN_TYPE_KEY
                    "npm" -> NPM_TYPE_KEY
                    "nuget" -> NUGET_TYPE_KEY
                    else -> UNMANAGED_TYPE_KEY
                }
                Identifier(type = ortType, namespace = namespace, name = name, version = version)
            }

    private fun mapVcsInfo(artifact: Artifact) : VcsInfo? =
        artifact.askFor(ArtifactVcsInfo::class.java).takeIf { it.isPresent }?.let {
            val vcsInfo = it.get().vcsInfo
            VcsInfo(type = VcsType(vcsInfo.type), url = vcsInfo.url, revision = vcsInfo.revision)
        }

    private fun mapSourceRemoteArtifact(artifact: Artifact) : RemoteArtifact? =
        artifact.askForGet(ArtifactSourceUrl::class.java).takeIf { it.isPresent }?.let {
            RemoteArtifact.EMPTY.copy(url = it.get())
        }


}
