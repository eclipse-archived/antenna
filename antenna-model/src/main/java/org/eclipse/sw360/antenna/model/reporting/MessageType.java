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

package org.eclipse.sw360.antenna.model.reporting;

/**
 * Describes the type of an event, that occurs during the process.
 */
public enum MessageType {
    /**
     * Something went wrong during processing.
     */
    PROCESSING_FAILURE, //
    /**
     * License is not known.
     */
    UNKNOWN_LICENSE, //
    /**
     * license has no text.
     */
    MISSING_LICENSE_TEXT, //
    /**
     * License is forbidden.
     */
    DISALLOWED_LICENSE, //
    /**
     * An artifact has no sources.
     */
    MISSING_SOURCES, //
    /**
     * The MatchState of an artifact is not exact.
     */
    MATCH_STATE_NOT_EXACT, //
    /**
     * The source jar is incomplete.
     */
    INCOMPLETE_SOURCES, //
    /**
     * An artifact has no maven and no bundle coordinates.
     */
    MISSING_COORDINATES, //
    /**
     * An artifact wont be added to the list of artifacts for the process.
     */
    REMOVE_ARTIFACT, //
    /**
     * The artifact is ignored for the downloading process.
     */
    IGNORE_FOR_ARTIFACTRESOLVING, //
    /**
     * Some values of the artifact are replaced with values defined in the
     * configuration.
     */
    OVERRIDE_ARTIFACTVALUES, //
    /**
     * Source or License are handled as valid for the configured attributes.
     */
    HANDLE_AS_VALID, //
    /**
     * A license is declared as forbidden.
     */
    FORBIDDEN_LICENSE, //
    /**
     * Artifact has no license information.
     */
    MISSING_LICENSE_INFORMATION, //
    /**
     * No config.xml was found.
     */
    MISSING_CONFIGURATION_FILE,
    /**
     * Matchstate of the artifact is unknown.
     */
    MATCHSTATE_UNKNOWN,
    /**
     * Some attributes of the artifact were overridden.
     */
    OVERRIDE_ARTIFACT_ATTRIBUTES,
    /**
     * Artifact was added manually within the configuration file.
     */
    ADD_ARTIFACT,
    /**
     * No jar was found.
     */
    NO_JAR,
    /**
     * Artifact has no pathname attached.
     */
    MISSING_PATHNAME,
    /**
     * Artifact is proprietary.
     */
    ARTIFACT_IS_PROPRIETARY,
    /**
     * Configuration is not used.
     */
    UNNECESSARY_CONFIG,
    /**
     * Artifact couldn't be attached to the project.
     */
    ATTACHING_FAILURE,
    /**
     * The clm didn't find any artifacts, or all artifacts were removed in
     * configuration.
     */
    NO_OSS_FOUND,
    /**
     * There are conflicts in the different configuration files.
     */
    CONFLICTING_CONFIGURATIONS,
    /**
     * Rule engine has reported an INFO, WARN or FAIL.
     */
    RULE_ENGINE;
}
