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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.ServiceLoader;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to load {@link TemplateBundle} instances via {@link ServiceLoader}
 */
public final class TemplateLoaderUtil {

   private static final Logger LOG = LoggerFactory.getLogger( TemplateLoaderUtil.class );

   private TemplateLoaderUtil() {

   }

   public static Templates load( String key ) {
      TemplateBundle bundle = getLoader( key );
      Templates templates = new Templates();
      templates.setTitle( loadTemplate( bundle::loadTitleTemplate ) );
      templates.setCopyright( loadTemplate( bundle::loadCopyrightTemplate ) );
      templates.setBackPage( loadTemplate( bundle::loadBackPageTemplate ) );
      templates.setContent( loadTemplate( bundle::loadContentTemplate ) );
      templates.loadFontDataForTemplate( bundle );

      return templates;
   }

   private static TemplateBundle getLoader( String templateKey ) {

      ServiceLoader<TemplateBundle> load = ServiceLoader.load( TemplateBundle.class );
      for ( TemplateBundle templateBundle : load ) {
         if ( templateKey.equalsIgnoreCase( templateBundle.key() ) ) {
            LOG.info( "Loaded implementation {} for key {}", templateBundle.getClass().getName(), templateKey );
            return templateBundle;
         }
      }
      throw new IllegalStateException( "Unable to locate a template bundle for key '" + templateKey + "'. Please check your classpath or configuration." );
   }

   private static PDDocument loadTemplate( Loader load ) {

      try ( InputStream is = load.load() ) {
         return PDDocument.load( is );
      } catch ( Exception e ) {
         throw new UnableToLoadTemplate( e );
      }
   }

   private interface Loader {

      InputStream load();

   }

   private static class UnableToLoadTemplate extends RuntimeException {
      private static final long serialVersionUID = 1L;

      public UnableToLoadTemplate( Throwable cause ) {
         super( cause );
      }
   } // end class

   public static class Templates {
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

      protected void loadFontDataForTemplate( TemplateBundle bundle ) {
         fontDataSansBold = loadFontData( bundle.loadSansBoldFont() );
         fontDataBoldItalic = loadFontData( bundle.loadSansBoldItalicFont() );
         fontDataSans = loadFontData( bundle.loadSansFont() );
         fontDataSansItalic = loadFontData( bundle.loadSansItalicFont() );
      }

      private static byte[] loadFontData( Optional<InputStream> fontData ) {
         if ( fontData.isPresent() ) {
            try {
               return IOUtils.toByteArray( fontData.get() );
            } catch ( IOException e ) {
               throw new UnableToLoadFont( e );
            }
         }
         return new byte[0];
      }

      protected void setTitle( PDDocument title ) {
         this.title = title;
      }

      public PDDocument getCopyright() {
         return copyright;
      }

      protected void setCopyright( PDDocument copyright ) {
         this.copyright = copyright;
      }

      public PDDocument getBackPage() {
         return backPage;
      }

      protected void setBackPage( PDDocument backPage ) {
         this.backPage = backPage;
      }

      public PDDocument getContent() {
         return content;
      }

      protected void setContent( PDDocument content ) {
         this.content = content;
      }

      public PDFont loadSansFont( PDDocument doc ) {
         return fontDataSans.length > 0 ? loadFont( doc, fontDataSans ) : PDType1Font.TIMES_ROMAN;
      }

      public PDFont loadSansBoldFont( PDDocument doc ) {
         return fontDataSansBold.length > 0 ? loadFont( doc, fontDataSansBold ) : PDType1Font.TIMES_BOLD;
      }

      public PDFont loadSansItalicFont( PDDocument doc ) {
         return fontDataSansItalic.length > 0 ? loadFont( doc, fontDataSansItalic ) : PDType1Font.TIMES_ITALIC;
      }

      public PDFont loadBoldItalicFont( PDDocument doc ) {
         return fontDataBoldItalic.length > 0 ? loadFont( doc, fontDataBoldItalic ) : PDType1Font.TIMES_BOLD_ITALIC;
      }

      /*
       * @see org.apache.pdfbox.pdmodel.font.PDType0Font#load(org.apache.pdfbox.pdmodel.PDDocument, InputStream)
       */
      private static PDFont loadFont( PDDocument doc, byte[] data ) {
         try ( InputStream is = new ByteArrayInputStream( data ) ) {
            return PDType0Font.load( doc, is );
         } catch ( IOException e ) {
            throw new RuntimeException( "Could not load font.", e );
         }
      }

      private static class UnableToLoadFont extends RuntimeException {
         private static final long serialVersionUID = 1L;

         public UnableToLoadFont( Throwable cause ) {
            super( cause );
         }
      } // end class
   }

}
