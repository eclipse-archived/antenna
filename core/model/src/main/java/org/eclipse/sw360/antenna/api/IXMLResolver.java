/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.api;

import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.model.xml.generated.AntennaConfig;

import java.io.File;

/**
 * Resolves a xml file.
 */
public interface IXMLResolver {
    /**
     * Resolves a xml file.
     * 
     * @param file
     * @return AntennaConfig with the values of the file. Null if file cannot be
     *         opened.
     * @throws ConfigurationException If file does not contain a valid content
     */
    AntennaConfig resolveXML(File file) throws ConfigurationException;
}
