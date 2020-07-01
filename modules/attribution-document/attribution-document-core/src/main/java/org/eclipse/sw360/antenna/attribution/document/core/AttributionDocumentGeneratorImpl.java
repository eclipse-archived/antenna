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
package org.eclipse.sw360.antenna.attribution.document.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.attribution.document.core.model.ArtifactAndLicense;
import org.eclipse.sw360.antenna.attribution.document.core.model.LicenseInfo;
import org.eclipse.sw360.antenna.attribution.document.utils.PDFWriterUtils;
import org.eclipse.sw360.antenna.attribution.document.utils.TemplateLoaderUtil;
import rst.pdfbox.layout.elements.ControlElement;
import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.Position;
import rst.pdfbox.layout.text.TextFlow;

/**
 * Generates a attribution document based on provided templates and values.
 */
public class AttributionDocumentGeneratorImpl {
    private static final String PARAGRAPH_TEXT = "The %s utilizes third-party software components. " +
            "This attribution document lists these software components and their licenses.";
    private static final String PARAGRAPH_MARKUP = "Components are identified by " +
            "{color:#0000EE}{link[https://github.com/package-url/purl-spec]}package URL (purl){link}{color:#000000}.";

    private final String documentName;
    private final File workingDir;
    private final String templateKey;
    private final DocumentValues values;

    /**
     * @param documentName  (non-blank) the name of the attribution document name
     * @param workingDir    (non-null) a writable directory to store intermediate and resulting files
     * @param templateKey   (non-blank) the key identifying the {@link TemplateBundle}.
     * @param values        (non-null)
     */
    public AttributionDocumentGeneratorImpl(String documentName, File workingDir, String templateKey, DocumentValues values) {
        this.documentName = documentName;
        this.workingDir = workingDir;
        this.templateKey = templateKey;
        this.values = values;
    }

    /**
     * Generates the attribution document with the given PDF template files.
     *
     * @param artifacts (non-null) list of artifacts to use in generation.
     * @param cover (non-null) template PDF file for the cover page.
     * @param copyright (non-null) template PDF file for the copyright page.
     * @param content (non-null) template PDF file for the content page with listed components.
     * @param back (non-null) template PDF file for the back page.
     * @return (non - null) the file handle of the generated attribution document.
     */
    public File generate(List<ArtifactAndLicense> artifacts, File cover, File copyright, File content, File back) {
        return generate(artifacts, cover, copyright, content, back, null, null, null, null);
    }

    public File generate(List<ArtifactAndLicense> artifacts,
                         File cover, File copyright, File content, File back,
                         File regular, File bold, File boldItalic, File italic) {
        try (Templates templates = TemplateLoaderUtil.load(cover, copyright, content, back,
                Optional.ofNullable(regular), Optional.ofNullable(bold),
                Optional.ofNullable(boldItalic), Optional.ofNullable(italic))) {
            return generate(artifacts, templates);
        }
    }

    /**
     * Generates the attribution document.
     *
     * @param artifacts (non-null) list of artifacts to use in generation.
     * @return (non - null) the file handle of the generated attribution document.
     */
    public File generate(List<ArtifactAndLicense> artifacts) {
         try (Templates templates = TemplateLoaderUtil.load(templateKey)) {
             return generate(artifacts, templates);
        }
    }

    private File generate(List<ArtifactAndLicense> artifacts, Templates templates) {
        File title = writeTitle(templates);
        File copyright = writeCopyright(templates);
        File backPage = writeBackPage(templates.getBackPage());
        File artifactPages = writeArtifacts(templates, artifacts, templates.getContent());
        File intermediateDoc = mergePages(title, copyright, artifactPages, backPage);

        return postProcess(templates, intermediateDoc, documentName);
    }

    private File postProcess(Templates templates, File intermediateDoc, String fileName) {
        try (PDDocument pdDocument = PDDocument.load(intermediateDoc)) {

            int allPages = pdDocument.getPages().getCount();
            PDFont font = templates.loadSansFont(pdDocument);

            for (int i = 1; i < allPages; i++) {
                PDPage page = pdDocument.getPage(i);
                float pageWidth = page.getMediaBox().getWidth();
                try (PDPageContentStream contents = new PDPageContentStream(pdDocument, page, AppendMode.APPEND, false)) {
                    writeCopyRightFooter(font, pageWidth, contents);
                    writePageNumber(font, pageWidth, contents, (i + 1) + "/" + allPages);
                }
            }

            File out = new File(workingDir, fileName);
            pdDocument.save(out);
            return out;
        } catch (IOException e) {
            throw new ExecutionException("Post process failed", e);
        }
    }

    private File writeArtifacts(Templates templates, List<ArtifactAndLicense> artifacts, PDDocument template) {
        PDFont sansFont = templates.loadSansFont(template);
        PDFont italicFont = templates.loadSansItalicFont(template);
        PDFont boldFont = templates.loadSansBoldFont(template);
        PDFont boldItalicFont = templates.loadBoldItalicFont(template);

        try {
            Document document = new Document(40, 60, 80, 80);
            document.add(createParagraph(String.format(PARAGRAPH_TEXT + "%n", values.getProductName()),
                    String.format(PARAGRAPH_MARKUP + "%n%n"),
                    10,
                    sansFont,
                    boldFont,
                    italicFont,
                    boldItalicFont));

            for (ArtifactAndLicense artifact : artifacts) {
                document.add(createParagraph(artifact, 10, sansFont, boldFont, italicFont, boldItalicFont));
            }
            writeLicenseText(document, artifacts, 15, sansFont, boldFont, italicFont, boldItalicFont);

            File file = new File(workingDir, "intermediate.artifacts.pdf");
            doSave(document, file);
            return doOverlay(file, template, "artifacts.pdf");
        } catch (IOException e) {
            throw new ExecutionException("Failed to write artifact and licenses", e);
        }
    }

    private Document writeLicenseText(Document document, List<ArtifactAndLicense> artifacts, int size, PDFont sansFont,
                                  PDFont boldFont,
                                  PDFont italicFont,
                                  PDFont boldItalicFont) throws IOException {
        Map<String, LicenseInfo> licenses = extractUniqueLicenses(artifacts);
        List<LicenseInfo> sortedLicenses = sortByTitle(licenses);

        for (LicenseInfo license : sortedLicenses) {
            document.add(ControlElement.NEWPAGE);

            Paragraph p = new Paragraph();
            p.addMarkup(String.format("{anchor:%s}*%s*{anchor} %n%n", license.getKey(), license.getTitle()),
                    size,
                    sansFont,
                    boldFont,
                    italicFont,
                    boldItalicFont);
            p.addText(license.getText(), 10, sansFont);

            document.add(p);
        }

        return document;
    }

    private Paragraph createParagraph(ArtifactAndLicense artifact, int size, PDFont sansFont, PDFont boldFont,
                                      PDFont italicFont, PDFont boldItalicFont) throws IOException {
        Paragraph p = new Paragraph();
        if (artifact.getPurl().isPresent()) {
            p.addMarkup(String.format("*Package URL:* %s%n", artifact.getPurl().get()),
                    size,
                    sansFont,
                    boldFont,
                    italicFont,
                    boldItalicFont);
        }
        if (artifact.getFilename() != null && ! artifact.getFilename().isEmpty()) {
            p.addMarkup(String.format("*Filename:* %s%n", artifact.getFilename()),
                    size,
                    sansFont,
                    boldFont,
                    italicFont,
                    boldItalicFont);
        }
        if (artifact.getCopyrightStatement().isPresent()) {
            p.addMarkup(String.format("*Copyright:* %s%n", artifact.getCopyrightStatement().get()),
                    size,
                    sansFont,
                    boldFont,
                    italicFont,
                    boldItalicFont);
        }
        p.addMarkup("*Licenses:*", size, sansFont, boldFont, italicFont, boldItalicFont);
        for (LicenseInfo license : artifact.getLicenses()){
            p.addMarkup(String.format("%n- {color:#0000EE}{link[#%s]}%s{link}{color:#000000}",
                                        license.getKey(), license.getShortName()),
                    size,
                    sansFont,
                    boldFont,
                    italicFont,
                    boldItalicFont);
        }
        p.addText("\n\n",size, sansFont);

        return p;
    }

    private Paragraph createParagraph(String text, String markUp, int size,
                                      PDFont sansFont, PDFont boldFont, PDFont italicFont, PDFont boldItalicFont) throws IOException {
        Paragraph p = new Paragraph();
        p.addText(text, size, sansFont);
        p.addMarkup(markUp, size, sansFont, boldFont, italicFont, boldItalicFont);
        return p;
    }

    private static List<LicenseInfo> sortByTitle(Map<String, LicenseInfo> licenses) {
        List<LicenseInfo> list = new ArrayList<>(licenses.values());
        Collections.sort(list,
                (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getTitle(), o2.getTitle()));
        return list;
    }

    private static Map<String, LicenseInfo> extractUniqueLicenses(List<ArtifactAndLicense> artifacts) {
        Map<String, LicenseInfo> map = new TreeMap<>();

        for (ArtifactAndLicense artifactAndLicense : artifacts) {
            List<LicenseInfo> licenses = artifactAndLicense.getLicenses();
            licenses.forEach(licenseInfo -> {
                map.put(licenseInfo.getKey(), licenseInfo);
            });
        }
        return map;
    }

    private File mergePages(File... mergeFiles) {
        PDFMergerUtility merger = new PDFMergerUtility();

        File outFile = new File(workingDir, "merged.attribute-doc.pdf");

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            merger.setDestinationStream(fos);
            merger.setDestinationFileName(outFile.getPath());

            for (File f : mergeFiles) {
                merger.addSource(f);
            }

            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            return outFile;
        } catch (IOException e) {
            throw new ExecutionException("Merging single pages failed.", e);
        }
    }

    private void writeCopyRightFooter(final PDFont font, float pageWidth, PDPageContentStream contents) throws IOException {
        TextFlow copyright = new TextFlow();
        copyright.setMaxWidth(pageWidth - 50);
        copyright.addText("\u00A9 " + values.getCopyrightHolder(), 8, font);
        copyright.drawText(contents, new Position(70, 39), Alignment.Left, null);
    }

    private void writePageNumber(PDFont font, float pageWidth, PDPageContentStream contents, String runningNumber) throws IOException {
        TextFlow pageNum = new TextFlow();
        pageNum.setMaxWidth(pageWidth - 50);
        pageNum.addText(runningNumber, 8, font);
        pageNum.drawText(contents, new Position(500, 39), Alignment.Left, null);
    }

    private File writeBackPage(PDDocument backPage) {
        return writePage(backPage, Paths.get(workingDir.getAbsolutePath(), "back.pdf"));
    }

    private File writeCopyright(Templates templates) {
        PDDocument copyright = templates.getCopyright();
        final PDFont font = templates.loadSansFont(copyright);

        PDFWriterUtils.write(copyright, (text, pageHeight, pageWidth) -> {
            String string = "\u00A9 " + values.getCopyrightHolder() + ", " + values.getCopyrightYear() + ".";
            text.addText(string, 10, font);
            return new Position(70, pageHeight - 183);
        });

        return writePage(copyright, Paths.get(workingDir.getAbsolutePath(), "copyright.pdf"));
    }

    private File writeTitle(Templates templates) {
        PDDocument titleTemplate = templates.getTitle();

        final PDFont font = templates.loadSansFont(titleTemplate);

        PDFWriterUtils.write(titleTemplate, (text, pageHeight, pageWidth) -> {
            text.setMaxWidth(pageWidth - 150);
            text.addText(values.getProductName(), 22, font);
            text.addText("\n\nVersion " + values.getVersion(), 12, font);
            return new Position(80, pageHeight - 295);
        });

        return writePage(titleTemplate, Paths.get(workingDir.getAbsolutePath(), "title.pdf"));
    }

    private File writePage(PDDocument document, Path path) {
        try {
            document.save(path.toFile());
            document.close();
            return path.toFile();
        } catch (IOException e) {
            throw new ExecutionException("Could not write page.", e);
        }
    }

    private File doOverlay(File file, PDDocument template, String newFileName) {
        File overlayed = new File(workingDir, newFileName);
        try (PDDocument content = PDDocument.load(file);
             Overlay overlay = new Overlay();
             FileOutputStream fos = new FileOutputStream(overlayed))  {
            overlay.setInputPDF(content);
            overlay.setAllPagesOverlayPDF(template);
            overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
            overlay.overlay(Collections.emptyMap());

            content.save(fos);
            return overlayed;
        } catch (IOException e) {
            throw new ExecutionException("PDF overlay failed", e);
        }
    }

    private void doSave(Document document, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.save(fos);
        }
    }
}
