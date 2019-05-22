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
package org.eclipse.sw360.antenna.model.artifact.facts;

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFactWithPayload;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtifactSoftwareHeritageID extends ArtifactFactWithPayload<String> {

    private ArtifactSoftwareHeritageID(String softwareHeritageID) {
        super(softwareHeritageID);
    }

    public static class Builder {
        private String softwareHeritageID;

        public Builder(String softwareHeritageID) {
            this.softwareHeritageID = softwareHeritageID;
        }

        public ArtifactSoftwareHeritageID build() throws AntennaException {
            if(softwareHeritageID == null || !isValid(softwareHeritageID)) {
                throw new AntennaException(softwareHeritageID
                        + " does not match expected format for SoftwareHeritageID: "
                        + getExpectedFormat());
            }
            return new ArtifactSoftwareHeritageID(softwareHeritageID);
        }

        private boolean isValid(String swhID) {
            final List<String> objectTypes = Stream.of("snp", "rel", "rev", "dir", "cnt").collect(Collectors.toList());
            String[] swhElements = swhID.split(":");

            if (swhElements.length == 4
                    && swhElements[0].equals("swh")
                    && isParsableInteger(swhElements[1])
                    && objectTypes.contains(swhElements[2])
                    && swhElements[3].length() == 40) {
                return true;
            } else {
                return false;
            }
        }

        private boolean isParsableInteger(String string) {
            try {
                Integer.parseInt(string);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    @Override
    public String getFactContentName() {
        return "Software Heritage URL";
    }

    @Override
    public Class<? extends ArtifactFact> getKey() {
        return ArtifactSoftwareHeritageID.class;
    }

    public static String getExpectedFormat() {
        return "swh:1:rel:*";
    }

}