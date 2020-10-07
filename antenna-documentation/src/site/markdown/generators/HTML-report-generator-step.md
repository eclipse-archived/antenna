# HTML attribution document generator
This generator generates a plain html attribution document listing all found component versions together with
the available license and copyright data as well as the component id in the package-url format. The complete
license texts of all found licenses are in the appendix and linked to the components using the corresponding license.

The file is plain html with an associated stylesheet. The idea is that the file can easily be adapted to the needs
of a project using the file, e.g., for the about open source page in an app. Therefore, the stylesheet is
externalized in a `styles.css` file that is stored in parallel to the generated file
`./target/antenna/3rdparty-licenses.html`.

### How to use
Add this configuration to the workflow.xml

```xml
<workflow>
    <generators>
        <step>
            <name>HTML Report Generator</name>
            <classHint>org.eclipse.sw360.antenna.workflow.generators.HTMLReportGenerator</classHint>
            <configuration>
                <entry key="license.report.template.file" value="${project.basedir}/myVelocityTemplate.vm"/>
                <entry key="license.report.file" value="${project.build.directory}/myDirectory/generatedHtmlReport.html"/>
                <entry key="license.report.style.file" value="${project.basedir}/myStyles.css"/>
            </configuration>
        </step>
    </generators>
</workflow>
```

#### Explanation of parameters
* `license.report.template.file`: Absolute path to the custom velocity template file. 
* `license.report.file`: The self-selected file name for the HTML file if it should not be 3rdparty-licenses.html.
* `license.report.style.file`: Absolute path to the custom styles.css file.

### Magic string for output handlers

When configuring output handlers, the output of the HTML report generator can be referred to by `attribution-doc`.
