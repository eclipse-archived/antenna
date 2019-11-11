# Attribution Document generator

### How to use
Add this configuration to the workflow.xml

```xml
<workflow>
    <generators>
        <step>
            <name>Attribution Document Generator</name>
            <classHint>org.eclipse.sw360.antenna.attribution.document.workflow.generators.AttributionDocumentGenerator</classHint>
            <configuration>
                <entry key="attribution.doc.templateKey" value="basic-pdf-template"/>
                <entry key="attribution.doc.name" value=""/>
                <entry key="attribution.doc.productName" value=""/>
                <entry key="attribution.doc.productVersion" value=""/>
                <entry key="attribution.doc.copyrightHolder" value=""/>
            </configuration>
        </step>
    </generators>
</workflow>
```

#### Explanation of parameters
* `attribution.doc.templateKey`: Specifies the bundle, which will be used for the generation of the attribution document.
The default bundle key is `basic-pdf-template`. If you have your custom bundle please use your unique key, specified in
your implementation.
* `attribution.doc.name`: *(optional)* Specifies the filename of your attribution document. If not specified, the 
attribution document is placed at `${project.build.directory}/antenna/attribution-document.pdf`.
* `attribution.doc.productName`: Specifies the name of the product, for which the attribution document is generated.
It will be appear in the title page of the document. 
* `attribution.doc.productVersion`: Specifies the version of the product, for which the attribution document is generated.
It will be appear in the title page of the document, below the product name. 
* `attribution.doc.copyrightHolder`: Specifies the Copyright Holder of the product, which will be appear in the footer
of the attribution document. 

#### Template development
Please have a look in the [Template Bundle Development](../template-bundle-development.html), if you want to create your
own template bundle with customized and company-related look.