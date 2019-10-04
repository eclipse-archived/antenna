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
package org.eclipse.sw360.antenna.disclosure.document.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;

import rst.pdfbox.layout.elements.ControlElement;
import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.text.Alignment;
import rst.pdfbox.layout.text.Position;
import rst.pdfbox.layout.text.TextFlow;

/**
 * Generates a disclosure document based on provided templates and values.
 */
public class DisDoc extends AbstractDisDoc {

   private final File workingDir;
   private final String templateKey;

   private final DocumentValues values;

   /**
    * @param workingDir
    *           (non-null) a writable directory to store intermediate and resulting files
    * @param templateKey
    *           (non-blank) the key identifying the {@link TemplateBundle}.
    * @param values
    *           (non-null)
    */
   public DisDoc( File workingDir, String templateKey, DocumentValues values ) {
      Validate.isTrue( workingDir != null && workingDir.isDirectory(), "Please provide a directory for working dir. Was: " + workingDir );
      Validate.isTrue( StringUtils.isNotBlank( templateKey ), "TemplateKey must not be blank" );
      Validate.isTrue( values != null, "DocumentValues must not be null" );
      this.workingDir = workingDir;
      this.templateKey = templateKey;
      this.values = values;
   }

   /**
    * Generates the disclosure document.
    *
    * @param artifacts
    *           (non-null) list of artifacts to use in generation.
    * @return (non-null) the file handle of the generated disclosure document.
    */
   public File generate( List<ArtifactAndLicense> artifacts ) {
      Validate.isTrue( artifacts != null, "Artifacts must not be null" );

      TemplateLoaderUtil.Templates templates = TemplateLoaderUtil.load( templateKey );

      File title = writeTitle( templates );
      File copyright = writeCopyright( templates );
      File backPage = writeBackPage( templates.getBackPage() );
      File artifactPages = writeArtifacts( templates, artifacts, templates.getContent() );
      File intermediateDoc = mergePages( title, copyright, artifactPages, backPage );
      return postProcess( templates, intermediateDoc, "disclosure-doc.pdf" );
   }

   private File postProcess(TemplateLoaderUtil.Templates templates, File intermediateDoc, String fileName ) {

      try ( PDDocument pdDocument = PDDocument.load( intermediateDoc ) ) {

         int allPages = pdDocument.getPages().getCount();
         PDFont font = templates.loadSansFont( pdDocument );

         for ( int i = 1; i < allPages; i++ ) {
            PDPage page = pdDocument.getPage( i );
            float pageWidth = page.getMediaBox().getWidth();
            try ( PDPageContentStream contents = new PDPageContentStream( pdDocument, page, AppendMode.APPEND, false ) ) {
               writeCopyRightFooter( font, pageWidth, contents );
               writePageNumber( font, pageWidth, contents, (i + 1) + "/" + allPages );
            }
         }

         File out = new File( workingDir, fileName );
         pdDocument.save( out );
         return out;
      } catch ( IOException e ) {
         throw new DocumentGenerationFailed( "Post process failed", e );
      }
   }

   private void writeCopyRightFooter( final PDFont font, float pageWidth, PDPageContentStream contents ) throws IOException {
      TextFlow copyright = new TextFlow();
      copyright.setMaxWidth( pageWidth - 50 );
      copyright.addText( "\u00A9 " + values.getCopyrightHolder(), 8, font );
      copyright.drawText( contents, new Position( 70, 39 ), Alignment.Left, null );
   }

   private void writePageNumber( PDFont font, float pageWidth, PDPageContentStream contents, String runningNumber ) throws IOException {
      TextFlow pageNum = new TextFlow();
      pageNum.setMaxWidth( pageWidth - 50 );
      pageNum.addText( runningNumber, 8, font );
      pageNum.drawText( contents, new Position( 500, 39 ), Alignment.Left, null );
   }

   private File writeArtifacts(TemplateLoaderUtil.Templates templates, List<ArtifactAndLicense> artifacts, PDDocument template ) {

      PDFont sansFont = templates.loadSansFont( template );
      PDFont italicFont = templates.loadSansItalicFont( template );
      PDFont boldFont = templates.loadSansBoldFont( template );
      PDFont boldItalicFont = templates.loadBoldItalicFont( template );

      try {

         Document document = new Document( 40, 60, 80, 80 );

         Paragraph ack = new Paragraph();
         ack.addText(
               "The " + values.getProductName()
                     + " utilizes third-party software components. This disclosure document lists these software components and their licenses.\n",
               10, sansFont );
         ack.addMarkup(
               "Components are identified by {color:#0000EE}{link[https://github.com/package-url/purl-spec]}package URL (purl){link}{color:#000000}.\n\n", 10,
               sansFont, boldFont,
               italicFont, boldItalicFont );

         //https://github.com/package-url/purl-spec
         document.add( ack );

         for ( ArtifactAndLicense artifactAndLicense : artifacts ) {
            Paragraph p = new Paragraph();

            if ( artifactAndLicense.getPurl().isPresent() ) {
               p.addMarkup( "*Package URL:* " + artifactAndLicense.getPurl().get() + "\n", 10, sansFont, boldFont, italicFont, boldItalicFont );
            }
            p.addMarkup( "*Filename:* " + artifactAndLicense.getFilename(), 10, sansFont, boldFont, italicFont, boldItalicFont );
            if ( artifactAndLicense.getCopyrightStatement().isPresent() ) {
               p.addMarkup( "\n*Copyright:* " + artifactAndLicense.getCopyrightStatement().get(), 10, sansFont, boldFont, italicFont, boldItalicFont );
            }
            p.addMarkup( "\n*Licenses:*", 10, sansFont, boldFont, italicFont, boldItalicFont );

            artifactAndLicense.getLicenses().forEach( licenseInfo -> {
               try {
                  p.addMarkup( "\n- {color:#0000EE}{link[#" + licenseInfo.getKey() + "]}" + licenseInfo.getShortName() + "{link}{color:#000000}", 10, sansFont,
                        boldFont,
                        italicFont,
                        boldItalicFont );
               } catch ( IOException e ) {
                  throw new DocumentGenerationFailed( "Failed to write license: " + licenseInfo.getShortName(), e );
               }
            } );

            p.addText( "\n\n", 8, sansFont );

            document.add( p );
         }

         Map<String, ArtifactAndLicense.LicenseInfo> licenses = extractUniqueLicenses( artifacts );
         List<ArtifactAndLicense.LicenseInfo> sortedLicenses = sortByTitle( licenses );
         for ( ArtifactAndLicense.LicenseInfo license : sortedLicenses ) {
            document.add( ControlElement.NEWPAGE );
            Paragraph p = new Paragraph();
            p.addMarkup( "{anchor:" + license.getKey() + "}*" + license.getTitle() + "*{anchor}\n\n", 15, sansFont, boldFont, italicFont,
                  boldItalicFont );
            p.addText( license.getText(), 10, sansFont );
            p.addText( "\n\n", 8, sansFont );

            document.add( p );
         }

         File file = new File( workingDir, "intermediate.artifacts.pdf" );
         doSave( document, file );
         return doOverlay( file, template, "artifacts.pdf" );

      } catch ( IOException e ) {
         throw new DocumentGenerationFailed( "Failed to write artifact and licenses", e );
      }
   }

   private static List<ArtifactAndLicense.LicenseInfo> sortByTitle(Map<String, ArtifactAndLicense.LicenseInfo> licenses ) {

      List<ArtifactAndLicense.LicenseInfo> list = new ArrayList<>( licenses.values() );
      Collections.sort( list,
            ( o1, o2 ) -> String.CASE_INSENSITIVE_ORDER.compare( o1.getTitle(), o2.getTitle() ) );
      return list;
   }

   private File doOverlay( File file, PDDocument template, String newFileName ) throws IOException {
      PDDocument content = PDDocument.load( file );

      Overlay overlay = new Overlay();
      overlay.setInputPDF( content );
      overlay.setAllPagesOverlayPDF( template );
      overlay.setOverlayPosition( Overlay.Position.BACKGROUND );
      overlay.overlay( Collections.emptyMap() );

      File overlayed = new File( workingDir, newFileName );
      try ( FileOutputStream fos = new FileOutputStream( overlayed ) ) {
         content.save( fos );
         content.close();
         return overlayed;
      }
   }

   private void doSave( Document document, File file ) throws IOException {
      try ( FileOutputStream fos = new FileOutputStream( file ) ) {
         document.save( fos );
      }
   }

   private static Map<String, ArtifactAndLicense.LicenseInfo> extractUniqueLicenses(List<ArtifactAndLicense> artifacts ) {

      Map<String, ArtifactAndLicense.LicenseInfo> map = new TreeMap<>();

      for ( ArtifactAndLicense artifactAndLicense : artifacts ) {
         List<ArtifactAndLicense.LicenseInfo> licenses = artifactAndLicense.getLicenses();
         licenses.forEach( licenseInfo -> {
            map.put( licenseInfo.getKey(), licenseInfo );
         } );
      }
      return map;
   }

   private File mergePages( File title, File copyright, File artifactPages, File backPage ) {
      PDFMergerUtility merger = new PDFMergerUtility();

      File outFile = new File( workingDir, "merged.dis-doc.pdf" );

      try ( FileOutputStream fos = new FileOutputStream( outFile ) ) {
         merger.setDestinationStream( fos );
         merger.setDestinationFileName( outFile.getPath() );
         merger.addSource( title );
         merger.addSource( copyright );
         merger.addSource( artifactPages );
         merger.addSource( backPage );
         merger.mergeDocuments( MemoryUsageSetting.setupMainMemoryOnly() );
         return outFile;
      } catch ( IOException e ) {
         throw new DocumentGenerationFailed( "Merging single pages failed.", e );
      }
   }

   private File writeBackPage( PDDocument backPage ) {

      File file = new File( workingDir, "back.pdf" );
      try {
         backPage.save( file );
         backPage.close();
         return file;
      } catch ( IOException e ) {
         throw new DocumentGenerationFailed( "Could not write back page.", e );
      }
   }

   private File writeCopyright( TemplateLoaderUtil.Templates templates ) {
      PDDocument copyright = templates.getCopyright();
      final PDFont font = templates.loadSansFont( copyright );

      super.write( copyright, ( text, pageHeight, pageWidth ) -> {
         String string = "\u00A9 " + values.getCopyrightHolder() + ", " + values.getCopyrightYear() + ".";
         text.addText( string, 10, font );
         return new Position( 70, pageHeight - 183 );
      } );

      File title = new File( workingDir, "copyright.pdf" );
      try {
         copyright.save( title );
         copyright.close();
         return title;
      } catch ( IOException e ) {
         throw new DocumentGenerationFailed( "Unable to create copyright page.", e );
      }
   }

   private File writeTitle( TemplateLoaderUtil.Templates templates ) {

      PDDocument titleTemplate = templates.getTitle();

      final PDFont font = templates.loadSansFont( titleTemplate );

      super.write( titleTemplate, ( text, pageHeight, pageWidth ) -> {
         text.setMaxWidth( pageWidth - 150 );
         text.addText( values.getProductName(), 22, font );
         text.addText( "\n\nVersion " + values.getVersion(), 12, font );
         return new Position( 80, pageHeight - 295 );
      } );

      File title = new File( workingDir, "title.pdf" );
      try {
         titleTemplate.save( title );
         titleTemplate.close();
         return title;
      } catch ( IOException e ) {
         throw new DocumentGenerationFailed( "Unable to create title page.", e );
      }
   }

   private static class DocumentGenerationFailed extends RuntimeException {
      private static final long serialVersionUID = 1L;

      public DocumentGenerationFailed( String message, Throwable cause ) {
         super( message, cause );
      }
   }

} // end class
