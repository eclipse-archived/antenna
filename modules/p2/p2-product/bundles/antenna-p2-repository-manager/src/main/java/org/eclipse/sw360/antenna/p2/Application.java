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

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import java.util.Arrays;

public class Application implements IApplication {
    private static final String APPLICATION_ARGS = "application.args";
    private static final Integer EXIT_FAILED = 1;

    @Override
    public Object start(IApplicationContext context) {
        String[] arguments = (String[]) context.getArguments().get(APPLICATION_ARGS);
        try {
            ProjectArguments projectArguments = ProjectArgumentExtractor.extractArguments(Arrays.asList(arguments));

            BundleContext bundleContext = Activator.getContext();
            if (bundleContext == null) {
                System.err.println("Bundle Context was not initialized. Abort");
                return EXIT_FAILED;
            }

            startAllBundles(bundleContext);

            final IProvisioningAgent provisioningAgent =
                    bundleContext.getService(bundleContext.getServiceReference(IProvisioningAgent.class));

            if (provisioningAgent == null) {
                System.err.println("Could not obtain provisioning agent. Maybe some eclipse bundles are not started? Abort.");
                return EXIT_FAILED;
            }

            final P2ArtifactResolver artifactsResolver = new P2ArtifactResolver(provisioningAgent);
            projectArguments.getRepositories().forEach(artifactsResolver::addRepository);
            artifactsResolver.defineTargetDirectory(projectArguments.getDownloadArea());
            artifactsResolver.resolveArtifacts(projectArguments.getP2Artifacts());
        } catch (P2Exception ex) {
            System.err.println("Something went wrong extracting arguments from " + String.join(", ", Arrays.asList(arguments)));
            ex.printStackTrace();
            return EXIT_FAILED;
        }
        return EXIT_OK;
    }

    private static void startAllBundles(BundleContext bundleContext) {
        Arrays.stream(bundleContext.getBundles())
                .filter(Application::isNoFragment)
                .forEach(bundle -> {
                    try {
                        bundle.start();
                    } catch (BundleException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static boolean isNoFragment(Bundle bundle) {
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null;
    }

    @Override
    public void stop() {
    }
}