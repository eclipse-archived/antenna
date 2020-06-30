/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generators.pdf;

import org.apache.commons.io.IOUtils;
import org.eclipse.sw360.antenna.attribution.document.core.TemplateBundle;

import java.io.*;
import java.util.Objects;
import java.util.Optional;

public class BasicPDFTemplateBundle implements TemplateBundle {
    private static final String TITLE_TEMPLATE = "/templates/basic_title.pdf";
    private static final String COPYRIGHT_TEMPLATE = "/templates/basic_copyright.pdf";
    private static final String CONTENT_TEMPLATE = "/templates/basic_content.pdf";
    private static final String BACKPAGE_TEMPLATE = "/templates/basic_back.pdf";
    private static final String SANS_FONT = "/templates/NotoSans-Regular.ttf";
    private static final String SANS_BOLD_FONT = "/templates/NotoSans-Bold.ttf";
    private static final String SANS_BOLD_ITALIC_FONT = "/templates/NotoSans-BoldItalic.ttf";
    private static final String SANS_ITALIC_FONT = "/templates/NotoSans-Italic.ttf";



    @Override
    public String key() {
        return "basic-pdf-template";
    }

    @Override
    public InputStream loadTitleTemplate() {
        return loadFromClassPath(TITLE_TEMPLATE);
    }

    @Override
    public InputStream loadCopyrightTemplate() {
        return loadFromClassPath(COPYRIGHT_TEMPLATE);
    }

    @Override
    public InputStream loadContentTemplate() {
        return loadFromClassPath(CONTENT_TEMPLATE);
    }

    @Override
    public InputStream loadBackPageTemplate() {
        return loadFromClassPath(BACKPAGE_TEMPLATE);
    }

    @Override
    public Optional<InputStream> loadSansFont() {
        return Optional.of(loadFromClassPath(SANS_FONT));
    }

    @Override
    public Optional<InputStream> loadSansBoldFont() {
        return Optional.of(loadFromClassPath(SANS_BOLD_FONT));
    }

    @Override
    public Optional<InputStream> loadSansBoldItalicFont() {
        return Optional.of(loadFromClassPath(SANS_BOLD_ITALIC_FONT));
    }

    @Override
    public Optional<InputStream> loadSansItalicFont() {
        return Optional.of(loadFromClassPath(SANS_ITALIC_FONT));
    }

    private InputStream loadFromClassPath(String resource) {
        InputStream resourceAsStream = BasicPDFTemplateBundle.class.getResourceAsStream(resource);
        Objects.requireNonNull(resourceAsStream, "Unable to load resource for '" + resource +
                "'. Maybe the name is wrong or the packaging?");
        return resourceAsStream;
    }
}
