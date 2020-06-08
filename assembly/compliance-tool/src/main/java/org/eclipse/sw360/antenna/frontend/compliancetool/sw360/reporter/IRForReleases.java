package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResource;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class IRForReleases implements InfoRequest<SW360Release> {
    @Override
    public String helpMessage() {
        return "The info parameter " + getInfoParameter() + " does not require any additional parameters or settings.";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Class<SW360Release> getType() {
        return SW360Release.class;
    }

    private Stream<SW360Release> getSw360ReleaseStream(SW360Connection connection, List<SW360SparseComponent> components) {
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
                .map(Optional::get);
    }

    Set<SW360Release> getReleasesByPredicate(SW360Connection connection, List<SW360SparseComponent> components, Predicate<SW360Release> releasePredicate) {
        return getSw360ReleaseStream(connection,components)
                .filter(releasePredicate)
                .collect(Collectors.toSet());
    }
}
