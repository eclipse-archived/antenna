package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.ComplianceFeatureUtils;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class IRGetNotClearedReleases extends IRForReleases {
    private static final String GET_RELEASES_NOT_CLEARED = "releases-not-cleared";

    @Override
    public String getInfoParameter() {
        return GET_RELEASES_NOT_CLEARED;
    }

    @Override
    public Collection<SW360Release> execute(SW360Connection connection) {
        final List<SW360SparseComponent> components = connection.getComponentAdapter().getComponents();

        final Predicate<SW360Release> releasePredicate = sw360release -> !ComplianceFeatureUtils.isApproved(sw360release);
        return getReleasesFromComponentsByPredicate(connection, components, releasePredicate);
    }

}
