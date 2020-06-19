/**
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.attribution.document.utils;

import java.io.*;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.attribution.document.core.TemplateBundle;
import org.eclipse.sw360.antenna.attribution.document.core.Templates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to load {@link TemplateBundle} instances via {@link ServiceLoader}
 */
public final class TemplateLoaderUtil {
    private static final Logger LOG = LoggerFactory.getLogger(TemplateLoaderUtil.class);

    private TemplateLoaderUtil() {
        // only utils
    }

    public static Templates load(String key) {
        return load(getLoader(key));
    }

    public static Templates load(File cover, File copyright, File content, File back) {
        return load(new TemplateBundle() {
            @Override
            public String key() {
                return "";
            }

            @Override
            public InputStream loadTitleTemplate() {
                try {
                    return new FileInputStream(cover);
                } catch (IOException e) {
                    throw new ExecutionException("Could not load cover template PDF", e);
                }
            }

            @Override
            public InputStream loadCopyrightTemplate() {
                try {
                    return new FileInputStream(copyright);
                } catch (IOException e) {
                    throw new ExecutionException("Could not load copyright template PDF", e);
                }
            }

            @Override
            public InputStream loadContentTemplate() {
                try {
                    return new FileInputStream(content);
                } catch (IOException e) {
                    throw new ExecutionException("Could not load copyright template PDF", e);
                }
            }

            @Override
            public InputStream loadBackPageTemplate() {
                try {
                    return new FileInputStream(back);
                } catch (IOException e) {
                    throw new ExecutionException("Could not load back template PDF", e);
                }
            }
        });
    }

    public static Templates load(TemplateBundle bundle) {
        Templates templates = new Templates();
        templates.setTitle(loadTemplate(bundle::loadTitleTemplate));
        templates.setCopyright(loadTemplate(bundle::loadCopyrightTemplate));
        templates.setBackPage(loadTemplate(bundle::loadBackPageTemplate));
        templates.setContent(loadTemplate(bundle::loadContentTemplate));
        templates.loadFontDataForTemplate(bundle);

        return templates;
    }

    private static TemplateBundle getLoader(String templateKey) {
        ServiceLoader<TemplateBundle> load = ServiceLoader.load(TemplateBundle.class);
        for (TemplateBundle templateBundle : load) {
            if (templateKey.equalsIgnoreCase(templateBundle.key())) {
                LOG.debug("Loaded implementation {} for key {}", templateBundle.getClass().getName(), templateKey);
                return templateBundle;
            }
        }
        throw new ExecutionException("Unable to locate a template bundle for key '" + templateKey + "'. " +
                "Please check your classpath or configuration.");
    }

    private static PDDocument loadTemplate(Supplier<InputStream> fileLoader) {
        try (InputStream is = fileLoader.get()) {
            return PDDocument.load(is);
        } catch (Exception e) {
            throw new ExecutionException("Unable to load template", e);
        }
    }
}
