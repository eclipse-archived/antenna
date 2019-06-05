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
package org.eclipse.sw360.antenna;

import okhttp3.*;
import org.apache.maven.it.Verifier;
import org.eclipse.sw360.antenna.frontend.testing.testProjects.ExampleTestProject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class AntennaSW360IntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntennaSW360IntegrationTest.class);
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
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
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
         * >  pg_dump -h {HOST} -p {PORT} -U {USER} sw360pgdb > src/test/resources/postgres/sw360pgdb.sql
         *
         */
        assumeTrue("Liferay PostgreSQL dump at 'src/test/resources/postgres/sw360pgdb.sql' does not exist", new File("src/test/resources/postgres/sw360pgdb.sql").exists());

        sw360_deployment.start();

        project = new ExampleTestProject();
        project.addAndOverwriteFile(new FileInputStream(new File("src/test/resources/pom.xml")), "pom.xml");
    }

    @AfterClass
    public static void afterAll() {
        sw360_deployment.stop();
    }

    @Before
    public void setUp() throws IOException {
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


        JsonArray licenses = get("/licenses").getJsonObject("_embedded").getJsonArray("sw360:licenses");
        assertThat(licenses).hasSize(4);
        assertThat(licenses.getValuesAs((JsonObject k) -> k.getString("fullName"))).contains("Creative Commons Attribution Share Alike 3.0 Unported");

        JsonArray components = get("/components").getJsonObject("_embedded").getJsonArray("sw360:components");
        assertThat(components).hasSize(8);
        assertThat(components.getValuesAs((JsonObject k) -> k.getString("name"))).contains("org.apache.commons:commons-csv");

        JsonArray releases = get("/releases").getJsonObject("_embedded").getJsonArray("sw360:releases");
        assertThat(releases).hasSize(8);
        assertThat(releases.getValuesAs((JsonObject k) -> k.getString("name"))).contains("org.apache.commons:commons-csv");

        JsonArray projects = get("/projects").getJsonObject("_embedded").getJsonArray("sw360:projects");
        assertThat(projects).hasSize(1);
        assertThat(projects.getValuesAs((JsonObject k) -> k.getString("name"))).contains("example-project-using-sw360-uploader");
    }

    private String authUrl() {
        return SW360_BASE + ":" + servicePort + SW360_AUTH_ENDPOINT;
    }

    private String restUrl() {
        return SW360_BASE + ":" + servicePort + SW360_RESOURCE_ENDPOINT;
    }

    private String login() throws IOException {
        Request request = new Request.Builder()
                .url(authUrl() + "/oauth/token?grant_type=password&username=" + SW360_USERNAME + "&password=" + SW360_PASSWORD)
                .method("POST", RequestBody.create(MediaType.get("application/json"), "{}"))
                .header("Authorization", Credentials.basic(SW360_AUTH_USER, SW360_AUTH_SECRET))
                .build();
        Response response = client.newCall(request).execute();
        if (response.body() != null) {
            return Json.createReader(new StringReader(response.body().string())).readObject().getString("access_token");
        }
        throw new AssertionError("Failed to log in");
    }

    private JsonObject get(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(restUrl() + endpoint)
                .get()
                .header("Authorization", "Bearer " + accessToken)
                .build();
        Response response = client.newCall(request).execute();
        return Json.createReader(new StringReader(response.body().string())).readObject();
    }
}