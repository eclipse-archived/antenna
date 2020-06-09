package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360TestUtils;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.LinkObjects;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.Self;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.releases.SW360SparseRelease;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IRGetReleasesOfProjectsTest {

    private final String PROJECT_ID = "12345";
    private final String PROJECT_NAME = "pr_name";
    private final String PROJECT_VERSION = "xxx";

    @Test
    public void executeGetReleasesOfProjectsByProjectIdTest() {
        executeGetReleasesOfProjectsWithParameters(Collections.singletonMap("--project_id", PROJECT_ID));
    }

    @Test
    public void executeGetReleasesOfProjectsByProjectNameAndVersionTest() {
        final Map<String, String> parameters = new HashMap<String, String>(){
            {
                put("--project_name", PROJECT_NAME);
                put("--project_version", PROJECT_VERSION);
            }
        };
        executeGetReleasesOfProjectsWithParameters(parameters);
    }

    /**
     * Executes the execute method of IRGetReleasesOfProjects with given parameters
     * @param parameters map of parameters for the additional parameters
     */
    private void executeGetReleasesOfProjectsWithParameters(Map<String, String> parameters) {
        SW360SparseRelease sparseRelease = SW360TestUtils.mkSW3SparseRelease("test");
        SW360Connection connection = makeSW360Connection(sparseRelease);

        final IRGetReleasesOfProjects irGetReleasesOfProjects = new IRGetReleasesOfProjects();

        irGetReleasesOfProjects.parseAdditionalParameter(parameters);
        final Collection<SW360SparseRelease> sparseReleases = irGetReleasesOfProjects.execute(connection);

        assertThat(sparseReleases).containsExactly(sparseRelease);
    }

    /**
     * creates connection with all projectadpater objects mocked
     * @param sparseRelease sparse release to be contained in result list
     * @return sw360 connection with set projectadapter objects
     */
    private SW360Connection makeSW360Connection(SW360SparseRelease sparseRelease) {
        SW360Connection connection = mock(SW360Connection.class);

        SW360ProjectClientAdapter projectClientAdapter = mock(SW360ProjectClientAdapter.class);
        when(projectClientAdapter.getLinkedReleases(PROJECT_ID, true))
                .thenReturn(Collections.singletonList(sparseRelease));
        final SW360Project sw360Project = new SW360Project();
        Self self = new Self(PROJECT_ID);
        LinkObjects linkObjectsWithSelf = new LinkObjects()
                .setSelf(self);
        sw360Project.setLinks(linkObjectsWithSelf);
        when(projectClientAdapter.getProjectByNameAndVersion(PROJECT_NAME, PROJECT_VERSION))
                .thenReturn(Optional.of(sw360Project));

        when(connection.getProjectAdapter())
                .thenReturn(projectClientAdapter);

        return connection;
    }
}