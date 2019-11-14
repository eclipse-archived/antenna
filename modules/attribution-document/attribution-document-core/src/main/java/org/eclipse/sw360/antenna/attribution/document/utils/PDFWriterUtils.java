/**
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.attribution.document.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.attribution.document.core.TextFlowInteraction;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.Position;
import rst.pdfbox.layout.text.TextFlow;

import java.io.IOException;

public class PDFWriterUtils {
    public static void write(PDDocument template, TextFlowInteraction writer) {
        PDPage page = template.getPage(0);
        float pageHeight = page.getMediaBox().getHeight();
        float pageWidth = page.getMediaBox().getWidth();

        try (PDPageContentStream contents = new PDPageContentStream(template, page, PDPageContentStream.AppendMode.APPEND, false)) {
            TextFlow text = new TextFlow();
            text.setMaxWidth(pageWidth - 50);
            Position position = writer.addYourText(text, pageHeight, pageWidth);
            text.drawText(contents, position, Alignment.Left, null);
        } catch (IOException e) {
            throw new ExecutionException("Writing text failed.", e);
        }
    }
}
