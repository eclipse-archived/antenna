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
package org.eclipse.sw360.antenna.frontend;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ContextExtension;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.configuration.MultipleConfigsResolver;
import org.eclipse.sw360.antenna.core.AntennaCore;
import org.eclipse.sw360.antenna.report.Reporter;

public class AntennaFrontendHelper {
    private final IProject project;
    private ToolConfiguration toolConfig;
    private ContextExtension contextExtension = new ContextExtension();

    public AntennaFrontendHelper(IProject project) {
        this.project = project;
    }

    public AntennaFrontendHelper setToolConfiguration(ToolConfiguration toolConfig) {
        this.toolConfig = toolConfig;
        return this;
    }

    public AntennaFrontendHelper putGeneric(Object object) {
        contextExtension.put(object);
        return this;
    }

    public AntennaContext buildAntennaContext() {
        IProcessingReporter reporter = new Reporter(toolConfig.getAntennaTargetDirectory(), toolConfig.getEncoding());

        AntennaContext.ContextBuilder contextBuilder = new AntennaContext.ContextBuilder()
                .setProject(project)
                .setConfiguration(new MultipleConfigsResolver().resolveConfigs(toolConfig))
                .setProcessingReporter(reporter)
                .setToolConfiguration(toolConfig)
                .setContextExtensions(contextExtension);

        AntennaContext context = contextBuilder.buildContext();
        boolean debug = System.getProperty("debug") != null && !"false".equals(System.getProperty("debug"));
        context.setDebug(debug);

        return context;
    }

    public AntennaCore buildAntennaCore() {
        AntennaContext context = buildAntennaContext();
        return buildAntennaCore(context);
    }

    public AntennaCore buildAntennaCore(AntennaContext context) {
        return new AntennaCore(context);
    }
}
