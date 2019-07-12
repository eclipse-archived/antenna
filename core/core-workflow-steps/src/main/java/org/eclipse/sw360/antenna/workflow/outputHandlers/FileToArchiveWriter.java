/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.workflow.outputHandlers;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractOutputHandler;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

/**
 * Writes the disclosure document to the given archive
 */
public class FileToArchiveWriter extends AbstractOutputHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileToArchiveWriter.class);
    private static final String INSTRUCTIONS_KEY = "instructions";

    private final List<FileToArchiveWriterInstruction> writeToArchiveInstructions = new ArrayList<>();

    public FileToArchiveWriter() {
        this.workflowStepOrder = 500;
    }

    @Override
    public void handle(Map<String, IAttachable> generatedOutput) {
        for (FileToArchiveWriterInstruction instruction : writeToArchiveInstructions) {
            handleWriteToArchiveInstructions(instruction, generatedOutput);
        }
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        String[] rawWriteToArchiveInstructions = getConfigValue(INSTRUCTIONS_KEY, configMap)
                .split(";");
        for(String rawInstruction : rawWriteToArchiveInstructions){
            writeToArchiveInstructions.add(new FileToArchiveWriterInstruction(rawInstruction, reporter));
        }
    }

    private void handleWriteToArchiveInstructions(FileToArchiveWriterInstruction instruction, Map<String, IAttachable> generatedOutput) {
        Path zipFile = instruction.zipFile;
        if (! zipFile.toFile().exists()) {
            String msg = "zip file=[" + zipFile + "] does not yet exists";
            reporter.add(MessageType.PROCESSING_FAILURE, msg);
            LOGGER.warn(msg);
            return;
        }

        String key = instruction.outputType;
        if (! generatedOutput.containsKey(key)) {
            String msg = "unable to attach File with key=[" + key + "], was not produced";
            reporter.add(MessageType.PROCESSING_FAILURE, msg);
            LOGGER.error(msg);
            return;
        }

        Path sourcePath = generatedOutput.get(key).getFile().toPath();
        Path pathInArchive = instruction.pathInArchive;

        LOGGER.info("Write file=[" + sourcePath + "] to artifact=[" + zipFile + "]");

        addFileToArchive(sourcePath, zipFile, pathInArchive);
    }

    public void addFileToArchive(Path sourcePath, Path zipFile, Path pathInArchive) {
        if (pathInArchive == null){
            throw new AntennaExecutionException("The argument pathInArchive was empty");
        }
        if(sourcePath == null) {
            throw new AntennaExecutionException("The argument sourcePath was empty");
        }

        if(! sourcePath.toFile().exists()){
            throw new AntennaExecutionException("Source file=["+sourcePath+"] does not exist");
        }

        try {
            addNewEntryToZipFile(zipFile, pathInArchive, sourcePath);
        } catch (IOException | URISyntaxException e) {
            throw new AntennaExecutionException("The file=[" + sourcePath + "] could not be added to the archive=[" + zipFile + "]", e);
        }
    }

    private void addNewEntryToZipFile(Path zipFile, Path pathInArchive, Path sourcePath)
            throws IOException, URISyntaxException {
        Map<String, String> properties = Collections.singletonMap("create", "false");
        URI zipFileUri = new URI("jar:" + zipFile.toUri().toString());
        try (FileSystem zipfs = FileSystems.newFileSystem(zipFileUri, properties)) {
            Path zipFilePath = zipfs.getPath(pathInArchive.toString()).toAbsolutePath();
            Path parent = Optional.ofNullable(zipFilePath.getParent())
                    .orElseThrow(() -> new AntennaExecutionException("parent should exists"));
            Files.createDirectories(parent);
            Files.copy(sourcePath, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
