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
package org.eclipse.sw360.antenna.sw360.rest.resources.components;

import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360ComponentType;
import org.eclipse.sw360.antenna.sw360.rest.resources.SW360ResourcesTestUtils;

public class SW360ComponentsTest extends SW360ResourcesTestUtils<SW360Component> {
    @Override
    public SW360Component prepareItem() {
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("Component Name");
        sw360Component.setComponentType(SW360ComponentType.COTS);
        sw360Component.setHomepage("componentName.org");
        sw360Component.setCreatedOn("2019-12-09");
        return sw360Component;
    }

    @Override
    public SW360Component prepareItemWithoutOptionalInput() {
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("Component Name");
        return sw360Component;
    }

    @Override
    public Class<SW360Component> getHandledClassType() {
        return SW360Component.class;
    }
}
