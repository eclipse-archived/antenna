package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ComponentClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360ComponentEmbedded;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360ClearingState;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IRGetClearedReleasesTest {

    @Test
    public void executeGetClearedReleasesTest() {
        SW360Release release = SW360TestUtils.mkSW360Release("test");
        release.setClearingState("PROJECT_APPROVED");
        release.setSw360ClearingState(SW360ClearingState.REPORT_AVAILABLE);
        SW360Connection connection = getSW360Connection(release);

        final IRGetClearedReleases getClearedReleases = new IRGetClearedReleases();
        final Collection<SW360Release> clearedReleases = getClearedReleases.execute(connection);

        assertThat(clearedReleases).containsExactly(release);
    }

    /**
     * create a connection with all mock objects that get
     * called when executing getting the releases.
     *
     * @return mocked sw360 connection
     */
    static SW360Connection getSW360Connection(SW360Release release) {
        SW360Connection connection = mock(SW360Connection.class);

        final String name = "test";
        SW360Component component = SW360TestUtils.mkSW360Component(name);
        Self self = new Self("1234");
        LinkObjects linkObjectsWithSelf = new LinkObjects()
                .setSelf(self);
        component.setLinks(linkObjectsWithSelf);
        SW360ComponentEmbedded componentEmbedded = new SW360ComponentEmbedded();
        SW360SparseRelease sparseRelease = SW360TestUtils.mkSW3SparseRelease(name);
        componentEmbedded.setReleases(Collections.singletonList(sparseRelease));
        component.setEmbedded(componentEmbedded);

        SW360ComponentClientAdapter componentClientAdapter = mock(SW360ComponentClientAdapter.class);
        when(componentClientAdapter.getComponentById(any()))
                .thenReturn(Optional.of((component)));
        SW360SparseComponent sparseComponent = SW360TestUtils.mkSW360SparseComponent(name);
        when(componentClientAdapter.getComponents())
                .thenReturn(Collections.singletonList(sparseComponent));
        when(connection.getComponentAdapter())
                .thenReturn(componentClientAdapter);

        SW360ReleaseClientAdapter releaseClientAdapter = mock(SW360ReleaseClientAdapter.class);
        when(releaseClientAdapter.getReleaseById(any()))
                .thenReturn(Optional.of(release));
        when(connection.getReleaseAdapter())
                .thenReturn(releaseClientAdapter);

        return connection;
    }
}