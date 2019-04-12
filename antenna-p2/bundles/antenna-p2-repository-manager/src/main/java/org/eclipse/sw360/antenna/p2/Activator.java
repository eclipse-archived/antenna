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

package org.eclipse.sw360.antenna.p2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private ServiceRegistration<?> artifactResolverService;
    private static BundleContext bundleContext;

    public static BundleContext getContext() {
        return bundleContext;
    }

    @Override
    public void start(BundleContext context) throws BundleException {
        bundleContext = context;
    }

    @Override
    public void stop(BundleContext context) {
        bundleContext = null;
    }
}
