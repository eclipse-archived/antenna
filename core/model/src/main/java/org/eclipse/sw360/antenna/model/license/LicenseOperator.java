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
package org.eclipse.sw360.antenna.model.license;

/**
 * Simple operator for license expression according to the SPDX license expression standard. The WITH operator is
 * missing, because with expressions are handled by a special class {@link WithLicense}
 */
public enum LicenseOperator {
    AND,
    OR;

    public static LicenseOperator fromValue(String operator) {
        return valueOf(operator.toUpperCase());
    }
}
