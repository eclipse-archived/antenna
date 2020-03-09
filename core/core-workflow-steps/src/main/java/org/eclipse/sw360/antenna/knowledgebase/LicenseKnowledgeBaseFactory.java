/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.knowledgebase;

import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class LicenseKnowledgeBaseFactory implements Supplier<List<ILicenseManagementKnowledgeBase>> {
    private static final Logger logger = LoggerFactory.getLogger(LicenseKnowledgeBaseFactory.class);
    private final IProcessingReporter reporter;
    private final AntennaContext context;

    public LicenseKnowledgeBaseFactory(AntennaContext context) {
        this.context = context;
        this.reporter = context.getProcessingReporter();
    }

    @Override
    public List<ILicenseManagementKnowledgeBase> get() {
        List<ILicenseManagementKnowledgeBase> services = new ArrayList<>();

        final Charset encoding = context.getToolConfiguration().getEncoding();
        ServiceLoader.load(ILicenseManagementKnowledgeBase.class, getClass().getClassLoader())
                .iterator()
                .forEachRemaining(service -> {
                    logger.debug("Found ILicenseManagementKnowledgeBase implementation: {}", service.getId());
                    service.init(reporter, encoding);
                    services.add(service);
                });

        return services;
    }
}
