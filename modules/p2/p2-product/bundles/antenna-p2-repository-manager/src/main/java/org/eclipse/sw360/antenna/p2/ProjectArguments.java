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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ProjectArguments {
    private List<URI> repositories = new ArrayList<>();
    private List<P2Artifact> p2Artifacts = new ArrayList<>();
    private File downloadArea;

    public void addRepository(URI repository) {
        this.repositories.add(repository);
    }

    public void addArtifact(P2Artifact p2Artifact) {
        this.p2Artifacts.add(p2Artifact);
    }

    public void setDownloadArea(File downloadArea) {
        this.downloadArea = downloadArea;
    }

    public List<URI> getRepositories() {
        return repositories;
    }

    public List<P2Artifact> getP2Artifacts() {
        return p2Artifacts;
    }

    public File getDownloadArea() {
        return downloadArea;
    }

}
