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
 * The classes in this package implement the analysis phase of the Antenna
 * workflow.
 * <p>
 * An Analyzer is a type of object that kicks off an analysis, which gathers
 * information about the artifacts in a project. An analysis is typically
 * performed at the request of an external tool. There are several such tools
 * available, and each Analyzer in this package provides a way to perform an
 * analysis using one of these tools.
 * <p>
 * The {@link org.eclipse.sw360.antenna.core.AntennaCore} calls upon an Analyzer to execute an
 * analysis. However, because there are different ways an analysis can be
 * carried out, the details of what type of analysis is taking place are hidden
 * from the {@code AntennaCore} behind the
 * {@link org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer} abstraction.
 */
package org.eclipse.sw360.antenna.workflow.analyzers;