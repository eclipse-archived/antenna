/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * The core package controls the Antenna execution workflow. From here, all steps
 * of the workflow are composed. These steps are:
 * <p>
 * <ul>
 * <li>Analyze</li>
 * <li>Report</li>
 * <li>Bundle</li>
 * </ul>
 * <p>
 * The relationship between classes implementing the workflow is illustrated in
 * the following diagram: <img src="doc-files/antenna_class_diagram.png">
 * <p>
 * The workflow begins execution in {@link org.eclipse.sw360.antenna.core.AntennaCore}. This
 * class works with fields in the
 * {@link org.eclipse.sw360.antenna.api.configuration.AntennaContext} and requires that certain
 * fields have already been set before execution.
 * <p>
 * The simplest way to initialize the Core and start execution is to use a
 * frontend class. For example, you can use Antenna as a Maven Mojo and specify
 * field values by writing them in your project's pom.xml file. The
 * {@link org.eclipse.sw360.antenna.frontend.AntennaMojoFrontend} will automatically read the
 * values out of the file and put them into {@code AntennaContext} ready for the
 * {@code AntennaCore} to use.
 */
package org.eclipse.sw360.antenna.core;
