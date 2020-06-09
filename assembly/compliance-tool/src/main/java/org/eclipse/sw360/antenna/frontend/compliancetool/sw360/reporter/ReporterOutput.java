/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Central interface for the report output of the status reporter
 */
public interface ReporterOutput {
    void setResultType(Class type);

    void setFilePath(Path filePath);

    <T> void print(Collection<T> result);
}
