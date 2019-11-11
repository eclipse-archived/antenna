# Template Bundle Development
The Antenna's own template bundle is a basic implementation without any styling or customization. It is used 
automatically when no other bundle is configured. This guide will show how to create a custom template bundle and 
integrate it in the Antenna workflow run. 

## Custom Template Bundle
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