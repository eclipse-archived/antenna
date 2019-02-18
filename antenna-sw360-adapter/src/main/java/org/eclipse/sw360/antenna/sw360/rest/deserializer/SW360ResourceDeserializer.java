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
package org.eclipse.sw360.antenna.sw360.rest.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentListEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360LicenseListEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectListEmbedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseListEmbedded;

import java.io.IOException;

public class SW360ResourceDeserializer extends JsonDeserializer<Embedded> {
    @Override
    public Embedded deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode root = mapper.readTree(p);
        if (root.has("sw360:projects")) {
            return mapper.readValue(root.toString(), SW360ProjectListEmbedded.class);
        } else if (root.has("sw360:components")) {
            return mapper.readValue(root.toString(), SW360ComponentListEmbedded.class);
        } else if (root.has("sw360:releases") && root.has("createdBy")) {
            return mapper.readValue(root.toString(), SW360ComponentEmbedded.class);
        } else if (root.has("sw360:releases")) {
            return mapper.readValue(root.toString(), SW360ReleaseListEmbedded.class);
        } else if (root.has("sw360:licenses")) {
            return mapper.readValue(root.toString(), SW360LicenseListEmbedded.class);
        } else if (root.has("createdBy")) {
            return mapper.readValue(root.toString(), SW360ProjectEmbedded.class);
        }
        return null;
    }
}
