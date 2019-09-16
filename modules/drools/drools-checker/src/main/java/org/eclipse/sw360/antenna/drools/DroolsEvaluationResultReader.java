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
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.drools.xml.generated.Policies;
import org.eclipse.sw360.antenna.util.XmlSettingsReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DroolsEvaluationResultReader {

    private DroolsEvaluationResultReader() {
        // Utility class
    }

    public static List<IEvaluationResult> getEvaluationResult(Path policesPath) {
        if (policesPath.toFile().exists() && policesPath.toFile().isFile()) {
            try (Stream<String> policyLines = Files.lines(policesPath)) {
                String policyXml = policyLines.map(String::trim).collect(Collectors.joining());
                XmlSettingsReader policyReader = new XmlSettingsReader(policyXml);
                return new ArrayList<>(policyReader.getComplexType("policies", Policies.class).getPolicy());
            } catch (ParserConfigurationException| IOException | SAXException e) {
                throw new ExecutionException("Could not read the policies. Details: " + e.getMessage());
            }
        } else if (policesPath.toFile().exists() && !policesPath.toFile().isFile()) {
            throw new ExecutionException("Given path [" + policesPath.normalize().toString() + "] is not a file.");
        } else {
            throw new ExecutionException("Given path [" + policesPath.normalize().toString() + "] does not exist.");
        }
    }
}
