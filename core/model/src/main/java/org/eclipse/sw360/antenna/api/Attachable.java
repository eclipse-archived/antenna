/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.api;

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;

import java.io.File;

/**
 * An instance of this class describes an Artifact which will be attached to the
 * current build.
 */
public class Attachable implements IAttachable {
    private final String type;
    private final String classifier;
    private final File file;

    public Attachable(String type, String classifier, File file){
        if(type == null){
            throw new AntennaExecutionException("Type in Attachable must not be null");
        }
        if(classifier == null){
            throw new AntennaExecutionException("Classifier in Attachable must not be null");
        }
        this.type = type;
        this.classifier = classifier;
        this.file = file;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public File getFile() {
        return file;
    }
}
