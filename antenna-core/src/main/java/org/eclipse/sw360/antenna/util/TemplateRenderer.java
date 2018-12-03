/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class TemplateRenderer {

    public static final String CLASSPATH_WORKFLOW_XML = "workflow.xml";

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateRenderer.class);

    private VelocityContext renderContext;

    public TemplateRenderer() {
        this.renderContext = new VelocityContext();
    }

    public TemplateRenderer(Map<String, Object> initMap) {
        this();
        initMap.forEach((key, value) -> this.renderContext.put(key, value));
    }

    public String renderTemplateFile(File templateFile, Map<String, Object> contextMap) {
        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            renderContext.put(entry.getKey(), entry.getValue());
        }
        return renderTemplateFile(templateFile);
    }

    private VelocityEngine getVelocityEngine(Map<String,String> veProperties) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,"org.apache.velocity.runtime.log.NullLogSystem");

        veProperties.forEach(ve::setProperty);
        return ve;
    }

    public String renderTemplateFile(File templateFile) {
        Map<String,String> veProperties = new HashMap<>();
        veProperties.put(RuntimeConstants.RESOURCE_LOADER, "file");
        veProperties.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                templateFile.getParentFile() != null
                        ? templateFile.getParentFile().getAbsolutePath()
                        : templateFile.getAbsoluteFile().getParentFile().getAbsolutePath());

        VelocityEngine ve = getVelocityEngine(veProperties);
        Template template = ve.getTemplate(templateFile.getName());
        return renderTemplate(template);
    }

    public Optional<String> renderClassPathWorkflow() {
        Map<String,String> veProperties = new HashMap<>();
        veProperties.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
        veProperties.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        veProperties.put("classpath.resource.loader.path", "/");

        VelocityEngine ve = getVelocityEngine(veProperties);
        try {
            Template template = ve.getTemplate(CLASSPATH_WORKFLOW_XML);
            return Optional.of(renderTemplate(template));
        } catch (org.apache.velocity.exception.ResourceNotFoundException ex) {
            return Optional.empty();
        }

    }

    private String renderTemplateWithCurrentRenderContext(Template template) {
        StringWriter sw = new StringWriter();
        template.merge(renderContext, sw);
        return sw.toString();
    }

    private Map<String, String> getPropertiesMapFromRenderedResult(String result)
            throws IOException, SAXException, ParserConfigurationException {
        XmlSettingsReader xsr = new XmlSettingsReader(result);
        return xsr.getStringKeyedMapProperty("properties", Function.identity());
    }

    /*
     * This function takes a xml template like:
     * <pre>{@code
     *     <?xml version="1.0" encoding="UTF-8"?>
     *     <project>
     *         <properties>
     *             <prop1>prop1value</prop1>
     *             <prop2>$prop0</prop2>
     *         </properties>
     *         <some_tag>
     *             <some_other_tag>
     *                 $prop0
     *             </some_other_tag>
     *             <some_other_tag>
     *                 ${prop1}
     *             </some_other_tag>
     *             <some_other_tag>
     *                 $prop2
     *             </some_other_tag>
     *        </some_tag>
     *     </project>
     * }</pre>
     *
     * and renders it as "Stage 1" with the current context, e.g {@code {"prop0" => "prop0value"} }
     *
     * The result of "Stage 1" might look like:
     * <pre>{@code
     *     <?xml version="1.0" encoding="UTF-8"?>
     *     <project>
     *         <properties>
     *             <prop1>prop1value</prop1>
     *             <prop2>prop0value</prop2>
     *         </properties>
     *         <some_tag>
     *             <some_other_tag>
     *                 prop0value
     *             </some_other_tag>
     *             <some_other_tag>
     *                 ${prop1}
     *             </some_other_tag>
     *             <some_other_tag>
     *                 $prop2
     *             </some_other_tag>
     *        </some_tag>
     *     </project>
     * }</pre>
     *
     * for "Stage 2" the code parses the result of "Stage 1" and enriches the context with the content of properties.
     * The resulting context might look like:
     *     {@code {"prop0" => "prop0value", "prop1" => "prop1value", "prop2" => "prop0value"} }
     * and the final rendered ArtifactFact might look like:
     * <pre>{@code
     *     <?xml version=\"1.0\" encoding="UTF-8"?>
     *     <project>
     *         <properties>
     *             <prop1>prop1value</prop1>
     *             <prop2>prop0value</prop2>
     *         </properties>
     *         <some_tag>
     *             <some_other_tag>
     *                 prop0value
     *             </some_other_tag>
     *             <some_other_tag>
     *                 prop1value
     *             </some_other_tag>
     *             <some_other_tag>
     *                 prop0value
     *             </some_other_tag>
     *        </some_tag>
     *     </project>
     * }</pre>
     */
    private String renderTemplate(Template template) {
        // Stage 1 - Render the template using the provided Map
        String result = renderTemplateWithCurrentRenderContext(template);

        try {
            // Stage 2 - Render the template again using the provided Map and the
            // properties that might have been rendered in the first stage.
            Map<String,String> propertiesMap = getPropertiesMapFromRenderedResult(result);

            if (propertiesMap.isEmpty()) {
                // bail out early if no properties present
                return result;
            }

            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                renderContext.put(entry.getKey(), entry.getValue());
            }

            result = renderTemplateWithCurrentRenderContext(template);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            String msg = "Error in second stage while rendering template. This also means that first stage result is invalid.";
            LOGGER.error(msg, e);
            throw new AntennaExecutionException(msg,e);
        }
        return result;
    }
}
