# Contributing to Antenna

First off, thanks for contributing!

This is a set of guidelines for contributing to [Antenna](https://github.com/eclipse/antenna).

#### Table Of Contents

[Code of Conduct](#code-of-conduct)

[What should I know before I get started?](#what-should-i-know-before-i-get-started)
  * [Eclipse Contributor Agreement](#eclipse-contributor-agreement)
  * [Contact](#contact)

[How Can I Contribute?](#how-can-i-contribute)
  * [Reporting Bugs](#reporting-bugs)
  * [Suggesting Enhancements](#suggesting-enhancements)
  * [Your First Code Contribution](#your-first-code-contribution)
  * [Pull Requests](#pull-requests)

[Styleguides](#styleguides)
  * [Git Commit Messages](#git-commit-messages)
  * [Java Styleguide](#java-styleguide)
  * [Documentation Styleguide](#documentation-styleguide)

## Code of Conduct

This project is hosted by the [Eclipse Foundation](https://github.com/eclipse) on GitHub and hence adheres to the
[Eclipse Code of Conduct](https://wiki.eclipse.org/Eclipse_Code_of_Conduct) that is based on the
[Contributor Covenant](https://www.contributor-covenant.org/).

## What should I know before I get started?

### Eclipse Contributor Agreement

Before your contribution can be accepted by the project team contributors must
electronically sign the [Eclipse Contributor Agreement (ECA)](http://www.eclipse.org/legal/ECA.php).

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project.
To achieve this, sign your commits by adding `-s` to your `git commit` command.
The non-committer must additionally have an Eclipse Foundation account and
must have a signed Eclipse Contributor Agreement (ECA) on file.
Your Git signature must match your Eclipse Foundation account information to be accepted.
If your signature was accepted is visible by checking the status check on your pull request.

For more information, please see the [Eclipse Committer Handbook](https://www.eclipse.org/projects/handbook/#resources-commit).

> **Note**: You can submit issues without signing the ECA.

### Contact

Contact the project developers via the [project's "dev" list](https://dev.eclipse.org/mailman/listinfo/antenna-dev).

Or contact us over our [Slack channel](https://join.slack.com/t/sw360chat/shared_invite/enQtNzg5NDQxMTQyNjA5LThiMjBlNTRmOWI0ZjJhYjc0OTk3ODM4MjBmOGRhMWRmN2QzOGVmMzQwYzAzN2JkMmVkZTI1ZjRhNmJlNTY4ZGI)

## How Can I Contribute?

### Before Submitting An Issue

* **Check the [troubleshooting documentation](https://github.com/eclipse/antenna/blob/master/antenna-documentation/src/site/markdown/troubleshooting.md.vm).**
You might be able to find the cause of the problem and fix things yourself. 
Most importantly, check if you can reproduce the problem [in the current version of Antenna](https://github.com/eclipse/antenna/blob/master).
It is possible that the bug has already been fixed.
* **Check the [documentation](https://github.com/eclipse/antenna/tree/master/antenna-documentation/src/site/markdown)**.
Your issue might already be mentioned there.
* **Perform a [cursory search](https://github.com/eclipse/antenna/issues?utf8=%E2%9C%93&q=is%3Aissue)** to see if the problem has already been reported.
If it has *and the issue is still open*, add a comment to the existing issue instead of opening a new one.

> **Note:** If you find a **Closed** issue that seems like it is the same thing that you want to report, open a new issue and include a link to the original issue in the body of your new one.

### Reporting Bugs

This section guides you through submitting a bug report.
Before creating bug reports, please check [the "before-submitting-an-issue" list](#before-submitting-an-issue) as you might find out that you don't need to create one.
Fill out [the required template](https://github.com/bsinno/osm-antenna/issues/new?labels=bug&template=bug_template.md).

#### How Do I Submit A (Good) Bug Report?

We have a template for a [bug report](https://github.com/eclipse/antenna/issues/new?labels=bug&template=bug_template.md).
Please use that by clicking on **new issue** and then choose the option **bug report**.
Explain the problem and include additional details to help maintainers reproduce the problem:

* Give a **clear and descriptive title** that identifies the problem.
* Give a **summary of the bug**.
* Describe the **steps to reproduce** the bug.
    * Don't hesitate to go into detail.
    * Everything that helps to reproduce the bug on a local machine is helpful.
        * Logs with stack traces
        * Antenna configuration files
        * System properties
        * Code snippets
        * Links to projects that experienced the problem.
    * Describe the behavior you experienced in detail and point out what exactly is the problem with that behavior.
    * Explain what you expected to experience instead.
* Give your **acceptance criteria**
    * What behavior do you want to see?
    * What are your minimum requirements to consider the bug gone?
* The **Definition of Done** is already predefined by us, but you can add points when you feel like something is missing.  

Provide more context by answering these questions:

* **Did the problem start happening recently** (e.g. after updating to a new version of Antenna) or was this always a problem?
* If the problem started happening recently, **can you reproduce the problem in an older version of Antenna?**
What's the most recent version in which the problem doesn't occur?
* **Can you reliably reproduce the issue?**
If not, provide details about how often the problem happens and under which conditions it normally happens.
If not, check if a server you are trying to contact might be at fault (e.g. your SW360 instance).
* If the problem is related to working with files, **does the problem happen for all files and projects or only some?**

Include details about your configuration and environment:

* Which Antenna version are you using?
* What's the name and version of the OS you're using?
* Do you have Apache Maven installed? If so, which version?
* What kind of dependencies does your project have?
* How does your tool configuration file, workflow file and antenna configuration file look?

### Suggesting Enhancements

This section guides you through submitting an enhancement or feature suggestion.
Fill in [the template](https://github.com/bsinno/osm-antenna/issues/new?template=feature_template.md), including the steps that you imagine you would take if the feature you're requesting existed.

#### How Do I Submit A (Good) Enhancement Suggestion?

* **Use a clear and descriptive title** for the issue to identify the enhancement.
* **Summary of the Feature**
    * **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
    * **Explain why this enhancement would be useful**.
* Give your **acceptance criteria**
    * What behavior do you want to see
    * What are your minimum requirements
* The **Definition of Done** is already predefined by us, but you can add points when you feel like something is missing.

### Your First Code Contribution

When you first contribute to Antenna, it can be that it takes a while for us to start reviewing your code.
Please be patient, and if necessary, write one of the contributors and ask what the state is on your PR.

Be sure you fulfill all legal requirements (e.g. [Eclipse Contributor Agreement](#eclipse-contributor-agreement)), otherwise we can't accept your pull request.

### Pull Requests

The process described here has several goals:

- Maintain Antenna's quality
- Enable a sustainable system for maintainers to review contributions

Please follow these steps, detailed in the [pull request template](.github/pull_request_template.md), to have your contribution considered by the maintainers:

1. Fill out the [pull request template](.github/pull_request_template.md).
Be sure to tick all boxes that apply to your pull request.  
2. Follow the [style guides](#style-guides).
3. Describe in your PR what you have done, what this is fixing or introducing and why this isn't regressive.
If there is an issue to your PR, please link it.
If you have adhered to the style guide of the [commit messages](#git-commit-messages), you will have that content already.
4. After you submit your pull request, verify that all [status checks](https://help.github.com/articles/about-status-checks/) are passing.
Should a status check fail and you believe it is unrelated to your contribution, please write so in a comment in the PR.

While the prerequisites above may be satisfied, the reviewer(s) may still ask you to do additional work or ask for changes before your PR is finally accepted.
Since the reviewer knows the code very well, they might have ideas how to include features more elegantly or see inconsistencies compared to the code base.

#### Reviews
Once your pull request is being reviewed, there are some guidelines.
1. Threads started during a review should only be marked as resolved by the starter of the thread.
2. Comments and discussions should be ended cleanly. This can be done by using emoticons on the comments,
e. g. a "thumbs-up" to mark a comment as done.
3. Once you have integrated changes that were requested, you can dismiss the review of the reviewer and re-request a review.

When integrating a reviewers comments into a pull request, please fix the issue in the respective commit where it appears,
instead of in a new commit. 

An approval of the PR means that the PR can be merged as is, without any changes still necessary.
If the owner of a PR is an Eclipse committer of the project, the owner can merge their own PR once approved.
Otherwise, the PR needs to be merged by an Eclipse committer of the project.

##### Request Reviewer
 You can add desired reviewers here with an @mention.
> For a full list of reviewers check out the *Committers* slot on [the project site](https://projects.eclipse.org/projects/technology.sw360.antenna/who).  
> A list of their GitHub names can be found in the [Eclipse Antenna Wiki](https://github.com/eclipse/antenna/wiki/Current-Committers).

#### Squashing
Depending the size or content of a pull request it might be sensible to separate your pull request into
[logically separate commits](https://github.com/git/git/blob/master/Documentation/SubmittingPatches#L43).
However, several commits are not always necessary. Only separate commits if their contents belong separately.
If a commit alone does not build, then it probably belongs with additional other changes.
When changes are required fixup your commits or squash new commits based on requested changes.
[Here you can find a guide on fixups and autosquashing](https://fle.github.io/git-tip-keep-your-branch-clean-with-fixup-and-autosquash.html).
Do not forget to amend your commit messages along with the changes.

> Note: Formatting changes should be in independent commits, as they are not necessary for functional changes.

#### Testing Changes:
Changes that include changes to the code need to be tested. 
* If you have added changes that require a new test, you should write it. 
* If you make changes that impact an existing test, it should still pass.
 
Here is a list of possible tests you can perform to see if your changes are working:
- Specific Unit Test
- Executing the Example Projects
- `mvn test` of whole project
- `mvn test` of submodule
- `gradle test` (for the gradle plugin)
- Integration tests with a running SW360

## Style Guides

### Git Commit Messages

When writing the commit message headline, keep in mind that the prefixes in the commit messages are meant to help when searching for changes.

Additionally, you should add descriptions to your commit messages of
- changes you have made to the code
    - why those changes are necessary 
    - why those changes are implemented in that way specifically
    
Don't just describe what is "obvious" in your code. 
A rational explanation why the changes are necessary help the reviewer understand the reasoning *behind* your code.

If there is an issue to your commit, link it using keywords as described [here](https://help.github.com/en/github/managing-your-work-on-github/closing-issues-using-keywords).

When there are changes in a commit that are the result of discussions which arose during the review of a pull request,
those discussions and their result should be mentioned in the commit message. 
This helps that the understanding of changes made is not lost in time.

> Note: please be sure not to exceed the standard length of 72 characters per line.

##### Sign your commits
It is very important that you sign your commits, so the
[Eclipse Contributor Agreement Validation](https://github.com/apps/eclipse-eca-validation)
will not be rejected. You can either use the `-s` flag the Git provides to sign a commit:
```
$ git commit -s -m 'doc: add variable x and y to the description of the sw360 data model'
```
or you can insert it when righting your commit message. Be sure to follow this example:
```
    my commit message

    Signed-off-by: John Doe <john.doe@antenna.org>
```

### Java Styleguide

All Java should adhere to the [Java Language Specification](https://docs.oracle.com/javase/specs/).

### Documentation Styleguide

Our documentation mainly focuses on the usage of Antenna.
So should you introduce any new properties, configurations, workflow steps or anything else the user will have to configure, please add a documentation about this.
Also, when you introduce path sensitive features or changes (like reading in a document), make sure it works with most standard OSs Antenna supports.
 Any drawbacks, additional information and small notes should also be documented.

 For our documentation we use [Markdown](https://daringfireball.net/projects/markdown) and the [Doxia Markup Languages References](https://maven.apache.org/doxia/references/index.html).
 With the Doxia Markup Language References we reference the name of the product and some other values that can be found in the site modules POM file.

Our documentation is written in American English.
