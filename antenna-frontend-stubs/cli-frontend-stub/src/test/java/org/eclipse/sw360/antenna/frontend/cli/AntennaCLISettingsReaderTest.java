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
package org.eclipse.sw360.antenna.frontend.cli;

import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.util.XmlSettingsReader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AntennaCLISettingsReaderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntennaCLISettingsReader.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private DefaultProject project = mock(DefaultProject.class);

    @Mock
    private XmlSettingsReader reader = mock(XmlSettingsReader.class);

    private AntennaCLISettingsReader tcsr = new AntennaCLISettingsReader();

    private Map<String,Boolean> booleanAnswers = new HashMap<>();
    private Map<String,Function<ToolConfiguration,Boolean>> booleanGetters = new HashMap<>();
    private Map<String,Integer> intAnswers = new HashMap<>();
    private Map<String,Function<ToolConfiguration,Integer>> intGetters = new HashMap<>();
    private Map<String,String> stringAnswers = new HashMap<>();
    private Map<String,Function<ToolConfiguration,String>> stringGetters = new HashMap<>();
    private Map<String,List<String>> listAnswers = new HashMap<>();
    private Map<String,Function<ToolConfiguration,List<String>>> listGetters = new HashMap<>();

    private Map<String, Boolean> testLicenseValidation = new HashMap<>();

    private <T,S> void putToMaps(Map<String,T> answerMap, Map<String,Function<S,T>> getterMap,
                                String key, T value, Function<S,T> getter){
        answerMap.put(key, value);
        getterMap.put(key, getter);
    }

    private <S> void putToMaps(Map<String,String> answerMap, Map<String,Function<S,String>> getterMap,
                                String key, Function<S,String> getter){
        answerMap.put(key, key + "-value");
        getterMap.put(key, getter);
    }

    private <S> void putToMapsForPath(Map<String,String> answerMap, Map<String,Function<S,String>> getterMap,
                            String key, Function<S,Path> getter){
        answerMap.put(key, key + "-value");
        getterMap.put(key, tc -> getter.apply(tc).toString());
    }

    private <T,S> void putToMaps(Map<String,List<String>> answerMap, Map<String,Function<S,List<String>>> getterMap,
                                String key, List<String> value, Function<S,List<File>> getter){
        answerMap.put(key, value);
        getterMap.put(key, tc -> getter.apply(tc).stream().map(File::toString).collect(Collectors.toList()));
    }

    @Before
    public void setup() throws IOException {
        when(project.getBuildDirectory()).thenReturn(temporaryFolder.newFolder("buildDir").toString());

        when(reader.getBooleanProperty(anyString(), anyString(), anyBoolean())).thenAnswer(
                (Answer<Boolean>) invocation -> {
                    String name = (String) invocation.getArguments()[1];
                    return booleanAnswers.getOrDefault(name, false);
                });
        when(reader.getIntProperty(anyString(), anyString(), anyInt())).thenAnswer(
                (Answer<Integer>) invocation -> {
                    String name = (String) invocation.getArguments()[1];
                    return intAnswers.getOrDefault(name, 1234);
                });
        when(reader.getStringPropertyByXPath(anyString(), anyString())).thenAnswer(
                (Answer<String>) invocation -> {
                    String name = (String) invocation.getArguments()[1];
                    return stringAnswers.getOrDefault(name, "none for "+name);
                });
        when(reader.getStringPropertyByXPath(anyString(), anyString(), anyString())).thenAnswer(
                (Answer<String>) invocation -> {
                    String name = (String) invocation.getArguments()[1];
                    return stringAnswers.getOrDefault(name, "none for "+name);
                });
        when(reader.getStringProperty(anyString())).thenAnswer(
                (Answer<String>) invocation -> {
                    String name = (String) invocation.getArguments()[0];
                    return stringAnswers.getOrDefault(name, "none for "+name);
                });
        when(reader.getStringProperty(anyString(), anyString())).thenAnswer(
                (Answer<String>) invocation -> {
                    String name = (String) invocation.getArguments()[0];
                    return stringAnswers.getOrDefault(name, "none for "+name);
                });
        when(reader.getStringPropertyByXPath(anyString(), anyString(), anyString())).thenAnswer(
                (Answer<String>) invocation -> {
                   String name = (String) invocation.getArguments()[1];
                   return stringAnswers.getOrDefault(name, "none for " +name);
                });
        when(reader.getStringListProperty(anyString())).thenAnswer(
                (Answer<List<String>>) invocation -> {
                    String name = (String) invocation.getArguments()[0];
                    return listAnswers.getOrDefault(name, new ArrayList<>());
                });

        when(reader.getStringKeyedBooleanMapProperty(anyString()))
                .thenReturn(testLicenseValidation);
    }

    private void fillMapsForToolConfiguration() throws IOException {
        putToMaps(booleanAnswers, booleanGetters, "attachAll", true, ToolConfiguration::isAttachAll);
        putToMaps(booleanAnswers, booleanGetters, "showCopyrightStatements", true, ToolConfiguration::isShowCopyrightStatements);

        putToMapsForPath(stringAnswers, stringGetters, "antennaTargetDirectory", ToolConfiguration::getAntennaTargetDirectory);
        putToMapsForPath(stringAnswers, stringGetters, "scanDir", ToolConfiguration::getScanDir);
        putToMaps(stringAnswers, stringGetters, "productName", ToolConfiguration::getProductName);
        putToMaps(stringAnswers, stringGetters, "productFullname", ToolConfiguration::getProductFullName);
        putToMaps(stringAnswers, stringGetters, "version", "1.0", ToolConfiguration::getVersion);
        putToMaps(stringAnswers, stringGetters, "companyName", ToolConfiguration::getCompanyName);
        putToMaps(stringAnswers, stringGetters, "copyrightHoldersName", ToolConfiguration::getCopyrightHoldersName);
        putToMaps(stringAnswers, stringGetters, "copyrightNotice", ToolConfiguration::getCopyrightNotice);
        putToMaps(stringAnswers, stringGetters, "disclosureDocumentNotes", ToolConfiguration::getDisclosureDocumentNotes);
        // putToMaps(stringAnswers, stringGetters, "encodingCharSet", ToolConfiguration::getEncoding);
        putToMaps(stringAnswers, stringGetters, "proxyHost", ToolConfiguration::getProxyHost);

        putToMaps(intAnswers, intGetters, "proxyPort", 2314, ToolConfiguration::getProxyPort);

        List<String> arbitraryFileList = new ArrayList<>();
        arbitraryFileList.add(temporaryFolder.newFile("file1").toString());
        arbitraryFileList.add(temporaryFolder.newFile("file2").toString());
        arbitraryFileList.add(temporaryFolder.newFile("file3").toString());
        putToMaps(listAnswers, listGetters, "filesToAttach", arbitraryFileList, ToolConfiguration::getFilesToAttach);
        putToMaps(listAnswers, listGetters, "configFiles", arbitraryFileList, ToolConfiguration::getConfigFiles);

    }

    @Test
    public void readBasicSettingsToToolConfigurationBuilderTest() throws Exception {
        fillMapsForToolConfiguration();

        ToolConfiguration toolConfiguration = tcsr.readBasicSettingsToToolConfigurationBuilder(reader, project).buildConfiguration();

        booleanGetters.forEach((key, getter) -> {
            LOGGER.info("test for: " + key);
            assertEquals(booleanAnswers.get(key), getter.apply(toolConfiguration));
        });
        stringGetters.forEach((key, getter) -> {
            LOGGER.info("test for: " + key);
            assertEquals(stringAnswers.get(key), getter.apply(toolConfiguration));
        });
        intGetters.forEach((key, getter) -> {
            LOGGER.info("test for: " + key);
            assertEquals(intAnswers.get(key), getter.apply(toolConfiguration));
        });
        listGetters.forEach((key, getter) -> {
            LOGGER.info("test for: " + key);
            assertEquals(listAnswers.get(key), getter.apply(toolConfiguration));
        });
    }
}