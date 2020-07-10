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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.exporter;

import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * A data class storing information about a release and the paths to the
 * source attachments that have been downloaded.
 * </p>
 * <p>
 * This class is used internally by the exporter to keep the information about
 * releases and their source attachments together.
 * </p>
 */
final class ReleaseWithSources {
    /**
     * The release.
     */
    private final SW360Release release;

    /**
     * A set with paths to source attachments that have been downloaded.
     */
    private final Set<Path> sourceAttachmentPaths;

    public ReleaseWithSources(SW360Release release, Set<Path> sourceAttachmentPaths) {
        this.release = release;
        this.sourceAttachmentPaths = Collections.unmodifiableSet(new HashSet<>(sourceAttachmentPaths));
    }

    /**
     * Returns the release.
     *
     * @return the release
     */
    public SW360Release getRelease() {
        return release;
    }

    /**
     * Returns an unmodifiable set with paths to attachments that have been
     * downloaded.
     *
     * @return the set of downloaded attachment paths
     */
    public Set<Path> getSourceAttachmentPaths() {
        return sourceAttachmentPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseWithSources that = (ReleaseWithSources) o;
        return Objects.equals(getRelease(), that.getRelease()) &&
                Objects.equals(getSourceAttachmentPaths(), that.getSourceAttachmentPaths());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRelease(), getSourceAttachmentPaths());
    }

    @Override
    public String toString() {
        return "ReleaseWithSources{" +
                "release=" + release +
                ", sourceAttachmentPaths=" + sourceAttachmentPaths +
                '}';
    }
}
