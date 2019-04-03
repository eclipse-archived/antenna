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

package org.eclipse.sw360.antenna.drools;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.drools.xml.generated.Policies;
import org.eclipse.sw360.antenna.util.XmlSettingsReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DroolsEvaluationResultReader {

    private DroolsEvaluationResultReader() {
        // Utility class
    }

    public static List<IEvaluationResult> getEvaluationResult(Path policesPath) throws AntennaException {
        if (policesPath.toFile().exists() && policesPath.toFile().isFile()) {
            try {
                String policyXml = Files.lines(policesPath).map(String::trim).collect(Collectors.joining());
                XmlSettingsReader policyReader = new XmlSettingsReader(policyXml);
                List<IEvaluationResult> result = new ArrayList<>();
                policyReader.getComplexType("policies", Policies.class).getPolicy().forEach(result::add);
                return result;
            } catch (Exception e) {
                throw new AntennaException("Could not read the policies. Details: " + e.getMessage());
            }
        } else if (policesPath.toFile().exists() && !policesPath.toFile().isFile()) {
            throw new AntennaException("Given path [" + policesPath.normalize().toString() + "] is not a file.");
        } else {
            throw new AntennaException("Given path [" + policesPath.normalize().toString() + "] does not exist.");
        }
    }
}
