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

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;

import java.util.Set;

public class MetadataRepository {
    private final IMetadataRepository metadataRepository;

    public MetadataRepository(IMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public Set<IInstallableUnit> queryRepository(String symbolicName) {
        IQuery<IInstallableUnit> jarQuery = QueryUtil.createIUQuery(symbolicName);
        IQueryResult<IInstallableUnit> jarFiles = metadataRepository.query(jarQuery, null);
        return jarFiles.toSet();
    }

}
