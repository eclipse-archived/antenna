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
package org.eclipse.sw360.antenna.sw360.integrationtesting;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import okhttp3.*;
import org.apache.maven.it.Verifier;
import org.eclipse.sw360.antenna.frontend.testing.testProjects.ExampleTestProject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@Category(IntegrationTest.class)
public class AntennaSW360IntegrationTest {
    /*
     * The dependencies for this class are only loaded if the `integration-test` profile is activated
     * Without activating this profile, the class is also excluded from the compilation process.
     */

    private static ExampleTestProject project;

    private OkHttpClient client = new OkHttpClient();

    private static final String SW360_BASE = "http:////localhost";
    private static final int SW360_PORT = 8080;

    private static final String SW360_AUTH_ENDPOINT = "/authorization";
    private static final String SW360_AUTH_USER = "trusted-sw360-client";
    private static final String SW360_AUTH_SECRET = "sw360-secret";

    private static final String SW360_RESOURCE_ENDPOINT = "/resource/api";
    private static final String SW360_USERNAME = "admin@sw360.org";
    private static final String SW360_PASSWORD = "12345";

    private int servicePort;
    private String accessToken;

    private static DockerComposeContainer sw360_deployment =
            new DockerComposeContainer(new File("src/integrationtest/resources/docker-compose.yml"))
                    .withExposedService("test_sw360", SW360_PORT,
                            Wait.forHttp(SW360_RESOURCE_ENDPOINT)
                                    .forStatusCode(200)
                                    .withStartupTimeout(Duration.ofMinutes(3)))
                    .withTailChildContainers(true);


    @BeforeClass
    public static void beforeAll() throws IOException {
        /*
         * The liferay database dump can be created by initially setting up a SW360 instance
         * (including the manual configuration and the demo users)
         * and then dump the postgres database to a file.
         *
         * Dump database one-liner:
         * >  pg_dump -h {HOST} -p {PORT} -U {USER} sw360pgdb > src/integrationtest/resources/postgres/sw360pgdb.sql
         *
         */
        assumeTrue("Liferay PostgreSQL dump at 'src/integrationtest/resources/postgres/sw360pgdb.sql' does not exist", new File("src/integrationtest/resources/postgres/sw360pgdb.sql").exists());

        sw360_deployment.start();

        project = new ExampleTestProject();
        project.addAndOverwriteFile(new FileInputStream(new File("src/integrationtest/resources/pom.xml")), "pom.xml");
    }

    @AfterClass
    public static void afterAll() {
        sw360_deployment.stop();
    }

    @Before
    public void setUp() throws Exception {
        servicePort = sw360_deployment.getServicePort("test_sw360", SW360_PORT);
        accessToken = login();
    }

    @Test
    public void testSW360Uploader() throws Exception {
        File testDir = project.getProjectRoot().toFile();

        Verifier verifier = new Verifier(testDir.getAbsolutePath(), true);
        verifier.setCliOptions(Arrays.asList(
                "-Dsw360.rest.url=" + restUrl(),
                "-Dsw360.auth.url=" + authUrl(),
                "-Dsw360.username=" + SW360_USERNAME,
                "-Dsw360.password=" + SW360_PASSWORD));
        verifier.executeGoal("package");

        verifier.verifyTextInLog("[INFO] SW360 Report Generator loaded and configured");
        verifier.verifyTextInLog("BUILD SUCCESS");


        JsonArray licenses = (JsonArray) ((JsonObject) get("/licenses").get("_embedded")).get("sw360:licenses");
        assertThat(licenses).hasSize(4);
        assertThat(IntStream.range(0, licenses.size())
                .mapToObj(licenses::getString)
                .collect(Collectors.toList()))
                .contains("Creative Commons Attribution Share Alike 3.0 Unported");

        JsonArray components = (JsonArray) ((JsonObject) get("/components").get("_embedded")).get("sw360:components");
        assertThat(components).hasSize(8);
        assertThat(IntStream.range(0, components.size())
                .mapToObj(i -> (JsonObject) components.get(i))
                .map(o -> o.getString(Jsoner.mintJsonKey("name", null)))
                .collect(Collectors.toList()))
                .contains("org.apache.commons:commons-csv");

        JsonArray releases = (JsonArray) ((JsonObject) get("/releases").get("_embedded")).get("sw360:releases");
        assertThat(releases).hasSize(8);
        assertThat(IntStream.range(0, releases.size())
                .mapToObj(i -> (JsonObject) releases.get(i))
                .map(o -> o.getString(Jsoner.mintJsonKey("name", null)))
                .collect(Collectors.toList()))
                .contains("org.apache.commons:commons-csv");

        JsonArray projects = (JsonArray) ((JsonObject) get("/projects").get("_embedded")).get("sw360:projects");
        assertThat(projects).hasSize(1);
        assertThat(IntStream.range(0, projects.size())
                .mapToObj(i -> (JsonObject) projects.get(i))
                .map(o -> o.getString(Jsoner.mintJsonKey("name", null)))
                .collect(Collectors.toList()))
                .contains("example-project-using-sw360-uploader");
    }

    private String authUrl() {
        return SW360_BASE + ":" + servicePort + SW360_AUTH_ENDPOINT;
    }

    private String restUrl() {
        return SW360_BASE + ":" + servicePort + SW360_RESOURCE_ENDPOINT;
    }

    private String login() throws IOException, JsonException {
        Request request = new Request.Builder()
                .url(authUrl() + "/oauth/token?grant_type=password&username=" + SW360_USERNAME + "&password=" + SW360_PASSWORD)
                .method("POST", RequestBody.create(MediaType.get("application/json"), "{}"))
                .header("Authorization", Credentials.basic(SW360_AUTH_USER, SW360_AUTH_SECRET))
                .build();
        Response response = client.newCall(request).execute();
        if (response.body() != null) {
            return ((JsonObject) Jsoner.deserialize(response.body().string()))
                    .getString(Jsoner.mintJsonKey("access_token", null));
        }
        throw new AssertionError("Failed to log in");
    }

    private JsonObject get(String endpoint) throws IOException, JsonException {
        Request request = new Request.Builder()
                .url(restUrl() + endpoint)
                .get()
                .header("Authorization", "Bearer " + accessToken)
                .build();
        Response response = client.newCall(request).execute();
        return (JsonObject) Jsoner.deserialize(response.body().string());
    }
}