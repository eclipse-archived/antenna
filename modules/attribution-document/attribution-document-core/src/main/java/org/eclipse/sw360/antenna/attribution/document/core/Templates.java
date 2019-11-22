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
package org.eclipse.sw360.antenna.attribution.document.core;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Templates {
    private PDDocument title;
    private PDDocument copyright;
    private PDDocument backPage;
    private PDDocument content;
    private byte[] fontDataSansBold;
    private byte[] fontDataSans;
    private byte[] fontDataBoldItalic;
    private byte[] fontDataSansItalic;

    public PDDocument getTitle() {
        return title;
    }

    public void setTitle(PDDocument title) {
        this.title = title;
    }

    public PDDocument getCopyright() {
        return copyright;
    }

    public void setCopyright(PDDocument copyright) {
        this.copyright = copyright;
    }

    public PDDocument getBackPage() {
        return backPage;
    }

    public void setBackPage(PDDocument backPage) {
        this.backPage = backPage;
    }

    public PDDocument getContent() {
        return content;
    }

    public void setContent(PDDocument content) {
        this.content = content;
    }

    public void loadFontDataForTemplate(TemplateBundle bundle) {
        fontDataSansBold = loadFontData(bundle.loadSansBoldFont());
        fontDataBoldItalic = loadFontData(bundle.loadSansBoldItalicFont());
        fontDataSans = loadFontData(bundle.loadSansFont());
        fontDataSansItalic = loadFontData(bundle.loadSansItalicFont());
    }

    public PDFont loadSansFont(PDDocument doc) {
        return fontDataSans.length > 0 ? loadFont(doc, fontDataSans) : PDType1Font.TIMES_ROMAN;
    }

    public PDFont loadSansBoldFont(PDDocument doc) {
        return fontDataSansBold.length > 0 ? loadFont(doc, fontDataSansBold) : PDType1Font.TIMES_BOLD;
    }

    public PDFont loadSansItalicFont(PDDocument doc) {
        return fontDataSansItalic.length > 0 ? loadFont(doc, fontDataSansItalic) : PDType1Font.TIMES_ITALIC;
    }

    public PDFont loadBoldItalicFont(PDDocument doc) {
        return fontDataBoldItalic.length > 0 ? loadFont(doc, fontDataBoldItalic) : PDType1Font.TIMES_BOLD_ITALIC;
    }

    private static byte[] loadFontData(Optional<InputStream> fontData) {
        if (fontData.isPresent()) {
            try {
                return IOUtils.toByteArray(fontData.get());
            } catch (IOException e) {
                throw new ExecutionException("Unable to load font.", e);
            }
        }
        return new byte[0];
    }

    private static PDFont loadFont(PDDocument doc, byte[] data) {
        try (InputStream is = new ByteArrayInputStream(data)) {
            return PDType0Font.load(doc, is);
        } catch (IOException e) {
            throw new ExecutionException("Could not load font.", e);
        }
    }
}
