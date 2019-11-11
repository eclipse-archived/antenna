package org.eclipse.sw360.antenna.workflow.generators.pdf;

import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.attribution.document.core.TemplateBundle;

import java.io.InputStream;
import java.util.Optional;

public class BasicPDFTemplateBundle implements TemplateBundle {
    private static final String TITLE_TEMPLATE = "/templates/basic_title.pdf";
    private static final String COPYRIGHT_TEMPLATE = "/templates/basic_copyright.pdf";
    private static final String CONTENT_TEMPLATE = "/templates/basic_content.pdf";
    private static final String BACKPAGE_TEMPLATE = "/templates/basic_back.pdf";

    @Override
    public String key() {
        return "basic-pdf-template";
    }

    @Override
    public InputStream loadTitleTemplate() {
        return Optional
                .ofNullable(BasicPDFTemplateBundle.class.getResourceAsStream(TITLE_TEMPLATE))
                .orElseThrow(() -> new ConfigurationException("Unable to load file=[" + TITLE_TEMPLATE + "]"));
    }

    @Override
    public InputStream loadCopyrightTemplate() {
        return Optional
                .ofNullable(BasicPDFTemplateBundle.class.getResourceAsStream(COPYRIGHT_TEMPLATE))
                .orElseThrow(() -> new ConfigurationException("Unable to load file=[" + COPYRIGHT_TEMPLATE + "]"));
    }

    @Override
    public InputStream loadContentTemplate() {
        return Optional
                .ofNullable(BasicPDFTemplateBundle.class.getResourceAsStream(CONTENT_TEMPLATE))
                .orElseThrow(() -> new ConfigurationException("Unable to load file=[" + CONTENT_TEMPLATE + "]"));
    }

    @Override
    public InputStream loadBackPageTemplate() {
        return Optional
                .ofNullable(BasicPDFTemplateBundle.class.getResourceAsStream(BACKPAGE_TEMPLATE))
                .orElseThrow(() -> new ConfigurationException("Unable to load file=[" + BACKPAGE_TEMPLATE + "]"));
    }
}
