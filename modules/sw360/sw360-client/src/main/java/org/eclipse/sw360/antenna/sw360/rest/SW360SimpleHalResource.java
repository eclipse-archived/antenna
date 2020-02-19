package org.eclipse.sw360.antenna.sw360.rest;

import org.eclipse.sw360.antenna.sw360.rest.resource.Embedded;
import org.eclipse.sw360.antenna.sw360.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResource;

public class SW360SimpleHalResource extends SW360HalResource<LinkObjects, Embedded> {
    @Override
    public LinkObjects createEmptyLinks() {
        return new LinkObjects();
    }

    @Override
    public Embedded createEmptyEmbedded() {
        return new EmptyEmbedded();
    }
}
