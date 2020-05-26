package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.util.*;
import java.util.stream.Collectors;

public class IPGetReleases extends InfoParameter {
    private static final String GET_RELEASES_CLEARED = SW360StatusReporterParameters.REPORTER_PARAMETER_PREFIX + "releases-cleared";

    @Override
    public String getInfoParameter() {
        return GET_RELEASES_CLEARED;
    }

    @Override
    boolean hasAdditionalParameters() {
        return false;
    }

    @Override
    String helpMessage() {
        return null;
    }

    @Override
    boolean isValid() {
        return false;
    }

    @Override
    Set<String> getAdditionalParameters() {
        return Collections.emptySet();
    }

    @Override
    void parseAdditionalParameter(Set<String> parameters) {
        //no-op since no additional parameters
    }

    @Override
    Object execute(SW360Connection connection) {
        final List<SW360SparseComponent> components = connection.getComponentAdapter().getComponents();

        final Set<SW360Release> releases = components.stream()
                .map(SW360HalResource::getId)
                .map(id -> connection.getComponentAdapter().getComponentById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(SW360HalResource::getEmbedded)
                .map(SW360ComponentEmbedded::getReleases)
                .flatMap(Collection::stream)
                .map(release -> connection.getReleaseAdapter().getReleaseById(release.getReleaseId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return releases;
    }
}
