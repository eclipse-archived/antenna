# Template Bundle Development
The Antenna's own template bundle is a basic implementation without any styling or customization. It is used 
automatically when no other bundle is configured. This guide will show how to create a custom template bundle and 
integrate it in the Antenna workflow run. 

## Custom Template Bundle

### How to create the template PDF files

First, the template has to be developed with a tool like Microsoft Word / Open Office / Libre Office. The corresponding 
document will consist of the four pages cover, copyright, content and back page. The page size is A4 at 72 PPI with 
595x842 pixels. The first page is the cover which can be designed with logos, corporate information etc. but it should 
be free of any text at the pixel 80x547 for the project's name and version. The second page is the copyright page to 
which a header and/or footer can be added, but it should be free of any text at the pixel 70x659 for the project's 
copyright notes. The third page is the content page to which a header and/or footer can be added, but it should be free 
of any text from the pixel 40x80 until the page bottom margin 40x762 for the list of the dependencies with PURL, file 
name, license and copyright notes. The last page is the back page which can be designed according to your own needs 
because this page will simply be attached to the attribution document without any additions.

After the document is finished, each individual page is exported as the respective template PDF file. The first page of 
the document has to be exported to a PDF file named cover.pdf, the second page to a PDF file named copyright.pdf, the
third page to a PDF file named content.pdf and the last page to a PDF file named back.pdf. 

### Basic folder structure

```
custom-template-bumdle-project
├── attribution-document-basic-bundle.iml
├── pom.xml
└── src
    └── main
        ├── java
        │   └── org
        │       └── example
        │           └── company
        │               └── antenna
        │                   └── workflow
        │                       └── generators
        │                           └── CustomTemplateBundle.java
        └── resources
            ├── META-INF
            │   └── services
            │       └── org.eclipse.sw360.antenna.attribution.document.core.TemplateBundle
            ├── fonts
            └── templates
                ├── custom_back.pdf
                ├── custom_content.pdf
                ├── custom_copyright.pdf
                └── custom_title.pdf
```

### Project setup 
1. Create a new Maven project. 
2. Add a new java class which implements the interface TemplateBundle. The interface has the following methods:  
Mandatory: key(), loadTitleTemplate(), loadCopyrightTemplate(), loadContentTemplate(), loadBackPageTemplate()  
Optional: loadSansFont(), loadSansItalicFont(), loadSansBoldFont(), loadSansBoldItalicFont()

    - `key`: Returns the unique key of the custom bundle.
    - `loadTitleTemplate`: Provides an `InputStream` of the cover page.
    - `loadCopyrightTemplate`: Provides an `InputStream` of the copyright page.
    - `loadContentTemplate`: Provides an `InputStream` of the content page. 
    - `loadBackPageTemplate`: Provides an `InputStream` of the back page. 
    - `loadSansFont, loadSansItalicFont, loadSansBoldFont, loadSansBoldItalicFont:` Provides an `InputStream` of your 
    Font. 
3. Add the provider configuration file of the service (TemplateBundle) in the `META-INF/services` 
directory, which contains the fully-qualified binary name of your concrete provider e.g. 
`org.example.company.antenna.workflow.generators.CustomTemplateBundle`.
4. Add for each cover, copyright, content and back page a one page pdf file with your customization.

### Usage in Antenna
After setting up your custom template bundle project, you have to package it to a JAR file and put it on the classpath 
of your Antenna run (e.g. in Maven: add your project as a dependency to the antenna-maven-plugin build). Additionally 
you have to configure the Attribution Document generator in your workflow.xml file with your specified unique key. 