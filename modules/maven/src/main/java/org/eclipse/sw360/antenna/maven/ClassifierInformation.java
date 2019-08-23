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
package org.eclipse.sw360.antenna.maven;

public class ClassifierInformation {
    public static final ClassifierInformation DEFAULT_JAR = new ClassifierInformation("", false);
    public static final ClassifierInformation DEFAULT_SOURCE_JAR = new ClassifierInformation("sources", true);
    public final String classifier;
    public final boolean isSource;

    public ClassifierInformation(String classifier, boolean isSource) {
        this.classifier = classifier;
        this.isSource = isSource;
    }
}
