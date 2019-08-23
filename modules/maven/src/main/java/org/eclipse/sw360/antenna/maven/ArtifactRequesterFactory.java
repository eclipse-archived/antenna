/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.maven;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.sw360.antenna.util.ProxySettings;

import java.io.File;
import java.net.URL;
import java.util.Optional;

/**
 * Returns classes for requesting the jars of Artifacts.
 */
public class ArtifactRequesterFactory {
    public static IArtifactRequester getArtifactRequester(Optional<RepositorySystem> optionalRepositorySystem,
                                                          Optional<MavenProject> optionalMavenProject,
                                                          Optional<LegacySupport> optionalLegacySupport,
                                                          File basedir,
                                                          ProxySettings proxySettings,
                                                          boolean isMavenInstalled,
                                                          URL sourcesRepositoryUrl) {
        if (isMavenInstalled) {
            return useMavenIfRunning(optionalRepositorySystem, optionalMavenProject, optionalLegacySupport, Optional.of(sourcesRepositoryUrl))
                    .orElse(new MavenInvokerRequester(basedir, sourcesRepositoryUrl));
        }
        return new HttpRequester(proxySettings, sourcesRepositoryUrl);
    }

    public static IArtifactRequester getArtifactRequester(Optional<RepositorySystem> optionalRepositorySystem,
                                                          Optional<MavenProject> optionalMavenProject,
                                                          Optional<LegacySupport> optionalLegacySupport,
                                                          File basedir,
                                                          ProxySettings proxySettings,
                                                          boolean isMavenInstalled) {
        if (isMavenInstalled) {
            return useMavenIfRunning(optionalRepositorySystem, optionalMavenProject, optionalLegacySupport, Optional.empty())
                    .orElse(new MavenInvokerRequester(basedir));
        }
        return new HttpRequester(proxySettings);
    }

    /*
     * Must only be used if Maven installation can be found on system, will result in ClassNotFoundError otherwise
     */
    private static Optional<IArtifactRequester> useMavenIfRunning(Optional<RepositorySystem> optionalRepositorySystem,
                                                                  Optional<MavenProject> optionalMavenProject,
                                                                  Optional<LegacySupport> optionalLegacySupport,
                                                                  Optional<URL> sourcesRepositoryUrl) {
        if (optionalRepositorySystem.isPresent() &&
                optionalMavenProject.isPresent() &&
                optionalLegacySupport.isPresent()) {
            ArtifactRepository localRepository = optionalLegacySupport.get().getSession().getLocalRepository();
            if (localRepository != null) {
                return Optional.of(new MavenRuntimeRequester(optionalRepositorySystem.get(), localRepository, optionalMavenProject.get().getRemoteArtifactRepositories(), sourcesRepositoryUrl));
            }
        }
        return Optional.empty();
    }


}
