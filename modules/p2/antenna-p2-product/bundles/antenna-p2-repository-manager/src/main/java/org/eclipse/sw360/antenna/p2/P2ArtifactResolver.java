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

package org.eclipse.sw360.antenna.p2;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.eclipse.sw360.antenna.p2.UriCheck.isRepositoryUriValid;

public class P2ArtifactResolver {
    private IProvisioningAgent provisioningAgent;
    private List<URI> repositories = new ArrayList<>();
    private List<P2Repository> p2Repositories = new ArrayList<>();
    private File targetDirectory;

    public P2ArtifactResolver(IProvisioningAgent agent) {
        this.provisioningAgent = agent;
    }

    public void defineTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void addRepository(URI repository) {
        this.repositories.add(repository);
    }

    public void resolveArtifacts(Collection<P2Artifact> intermediates) throws P2Exception {
        setupTargetDirectory();

        for (URI repository : repositories) {
            if (!isRepositoryUriValid(repository)) {
                return;
            }
            initializeRepository(repository);
        }
        resolveArtifactsInRepositories(intermediates);
    }

    private void setupTargetDirectory() throws P2Exception {
        if (targetDirectory == null) {
            throw new P2Exception("Undefined target directory for resolving p2 files.");
        }
        targetDirectory.mkdirs();
    }

    private void initializeRepository(URI repository) throws P2Exception {
        try {
            initRepository(repository);
        } catch (ProvisionException e) {
            throw new P2Exception("Could not provision repositories " + repository, e);
        }
    }

    private void initRepository(URI repository) throws ProvisionException, P2Exception {
        System.out.println("Initialize artifact repository: " + repository);
        IArtifactRepositoryManager service =
                (IArtifactRepositoryManager) provisioningAgent.getService(IArtifactRepositoryManager.SERVICE_NAME);
        if (service == null) {
            throw new P2Exception("Could not obtain provisioning service");
        }
        IArtifactRepository artifactRepository = service.loadRepository(repository, null);
        System.out.println("Initialized artifact repository: " + repository);

        System.out.println("Initialize metadata repository: " + repository);
        IMetadataRepositoryManager metadataService =
                (IMetadataRepositoryManager) provisioningAgent.getService(IMetadataRepositoryManager.SERVICE_NAME);
        if (metadataService == null) {
            throw new P2Exception("Could not obtain provisioning service");
        }
        IMetadataRepository metadataRepository = metadataService.loadRepository(repository, null);
        System.out.println("Initialized metadata repository: " + repository);

        p2Repositories.add(new P2Repository(metadataRepository, artifactRepository, targetDirectory));
        System.out.println("Initialization of repository successful.");
    }

    private void resolveArtifactsInRepositories(Collection<P2Artifact> intermediates) throws P2Exception {
        for (P2Repository repo : p2Repositories) {
            for (P2Artifact intermediate : intermediates) {
                repo.resolveArtifact(intermediate);
            }
        }
    }
}
