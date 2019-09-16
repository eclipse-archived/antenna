/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.xml;

import org.eclipse.sw360.antenna.model.xml.generated.AntennaConfig;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class XMLResolverTest {

    @Test
    public void test() throws URISyntaxException {
        XMLResolverJaxB resolver = new XMLResolverJaxB(StandardCharsets.UTF_8);
        URL xml = resolver.getClass().getResource("/antennaconf.xml");
        AntennaConfig config = resolver.resolveXML(new File(xml.toURI()));
        assertThat(config).isNotNull();
    }
}
