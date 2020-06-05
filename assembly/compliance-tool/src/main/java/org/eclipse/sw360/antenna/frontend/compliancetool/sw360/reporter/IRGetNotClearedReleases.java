package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.util.*;
import java.util.stream.Collectors;

public class IRGetNotClearedReleases extends InfoRequest<SW360Release> {
    private static final String GET_RELEASES_NOT_CLEARED = "releases-not-cleared";

    @Override
    public String getInfoParameter() {
        return GET_RELEASES_NOT_CLEARED;
    }

    @Override
    String helpMessage() {
        return "The info parameter " + GET_RELEASES_NOT_CLEARED + " does not require any additional parameters or settings.";
    }

    @Override
    boolean isValid() {
        return true;
    }

    @Override
    Set<String> getAdditionalParameters() {
        return Collections.emptySet();
    }

    @Override
    void parseAdditionalParameter(Map<String, String> parameters) {
        //no-op since no additional parameters
    }

    @Override
    Collection<SW360Release> execute(SW360Connection connection) {
        final List<SW360SparseComponent> components = connection.getComponentAdapter().getComponents();

        return components.stream()
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
                .filter(sw360release -> !ComplianceFeatureUtils.isApproved(sw360release))
                .collect(Collectors.toSet());
    }
}
