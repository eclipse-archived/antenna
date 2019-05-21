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

package org.eclipse.sw360.antenna.model.artifact.facts;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFactWithPayload;
import org.eclipse.sw360.antenna.model.xml.generated.Issue;
import org.eclipse.sw360.antenna.model.xml.generated.Issues;

import java.util.Collections;
import java.util.List;

public class ArtifactIssues extends ArtifactFactWithPayload<List<Issue>> {
    public ArtifactIssues(Issues issues) {
        super(issues.getIssue());
    }
    public ArtifactIssues(List<Issue> issues) {
        super(issues);
    }

    public static List<Issue> getDefault() {
        return Collections.emptyList();
    }

    @Override
    public String getFactContentName() {
        return "Issues";
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || get().size() == 0;
    }
}
