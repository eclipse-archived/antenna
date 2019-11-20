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
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FileToArchiveWriter.class);
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
    public void configure(Map<String, String> configMap) {
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

        LOGGER.debug("Write file=[{}] to artifact=[{}]", sourcePath, zipFile);

        addFileToArchive(sourcePath, zipFile, pathInArchive);
    }

    public void addFileToArchive(Path sourcePath, Path zipFile, Path pathInArchive) {
        if (pathInArchive == null){
            throw new ExecutionException("The argument pathInArchive was empty");
        }
        if(sourcePath == null) {
            throw new ExecutionException("The argument sourcePath was empty");
        }

        if(! sourcePath.toFile().exists()){
            throw new ExecutionException("Source file=["+sourcePath+"] does not exist");
        }

        try {
            addNewEntryToZipFile(zipFile, pathInArchive, sourcePath);
        } catch (IOException | URISyntaxException e) {
            throw new ExecutionException("The file=[" + sourcePath + "] could not be added to the archive=[" + zipFile + "]", e);
        }
    }

    private void addNewEntryToZipFile(Path zipFile, Path pathInArchive, Path sourcePath)
            throws IOException, URISyntaxException {
        Map<String, String> properties = Collections.singletonMap("create", "false");
        URI zipFileUri = new URI("jar:" + zipFile.toUri().toString());
        try (FileSystem zipfs = FileSystems.newFileSystem(zipFileUri, properties)) {
            Path zipFilePath = zipfs.getPath(pathInArchive.toString()).toAbsolutePath();
            Path parent = Optional.ofNullable(zipFilePath.getParent())
                    .orElseThrow(() -> new ExecutionException("parent should exists"));
            Files.createDirectories(parent);
            Files.copy(sourcePath, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
