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
package org.eclipse.sw360.antenna.frontend;

import org.eclipse.sw360.antenna.api.IProject;

import java.io.File;

public abstract class MetaDataStoringProject implements IProject {
    protected File propertiesFile;
    protected Build build;

    public abstract void setProjectId(String id);
    public abstract void setVersion(String version);

    public Build getBuild() {
        return build;
    }

    public String getArtifactId() {
        return getProjectId();
    }

    public File getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(File propertiesFile) {
        this.propertiesFile = propertiesFile;
    }
}
