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
package org.eclipse.sw360.antenna.knowledgebase;

import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class LicenseKnowledgeBaseFactory implements Supplier<ILicenseManagementKnowledgeBase>{
    private static final Logger logger = LoggerFactory.getLogger(LicenseKnowledgeBaseFactory.class);
    private final IProcessingReporter reporter;
    private final AntennaContext context;

    public LicenseKnowledgeBaseFactory(AntennaContext context) {
        this.context = context;
        this.reporter = context.getProcessingReporter();
    }

    @Override
    public ILicenseManagementKnowledgeBase get() {
        Iterator<ILicenseManagementKnowledgeBase> iter = ServiceLoader.load(ILicenseManagementKnowledgeBase.class).iterator();
        if(!iter.hasNext()){
            throw new AntennaExecutionException("Was not able to find any implementation for the ILicenseManagementKnowledgeBase interface.");
        }

        ILicenseManagementKnowledgeBase knowledgeBase = iter.next();

        if(iter.hasNext()){
            StringBuilder implementations = new StringBuilder(knowledgeBase.getClass().getCanonicalName());
            while(iter.hasNext()){
                implementations.append(", ").append(iter.next().getClass().getCanonicalName());
            }
            throw new AntennaExecutionException("Found to many implementations for the ILicenseManagementKnowledgeBase interface: " + implementations);
        }

        logger.info("Found knowledgebase implementation: " + knowledgeBase.getClass().getCanonicalName());
        final Charset encoding = context.getToolConfiguration().getEncoding();
        knowledgeBase.init(reporter, encoding);

        return knowledgeBase;
    }
}
