![Cognifide logo](docs/cognifide-logo.png)

[![Gradle Status](https://gradleupdate.appspot.com/Cognifide/gradle-aem-plugin/status.svg)](https://gradleupdate.appspot.com/Cognifide/gradle-aem-plugin/status)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/Cognifide/gradle-aem-plugin.svg?label=License)](http://www.apache.org/licenses/)

# Gradle AEM Plugin

<p align="center">
  <img src="docs/logo.png" alt="Gradle AEM Plugin Logo"/>
</p>

Currently there is no popular way to build applications for AEM using Gradle build system. This project contains brand new Gradle plugin to assemble CRX package and deploy it on instance(s).

Incremental build which takes seconds, not minutes. Developer who does not loose focus between build time gaps. Extend freely your build system directly in project. 

AEM developer - it's time to meet Gradle! You liked or used plugin? Don't forget to **star this project** on GitHub :)

<br>
<p align="center">
  <img src="docs/example-build.gif" alt="Example Project Build"/>
</p>
<br>

## Features

* Fully automated, tied to project, local AEM instance(s) setup allowing to start development within few minutes.
* Composing CRX package from multiple JCR content roots, bundles.
* Automated all-in-one CRX packages generation (assemblies).
* Easy multi-deployment with instance groups.
* Automated dependent packages installation from local and remote sources (SMB, SSH, HTTP(s)).
* Smart Vault files generation (combining defaults with overiddables).
* Embedded Vault tool for checking out and cleaning JCR content from running AEM instance.
* OSGi Manifest customization by official [osgi](https://docs.gradle.org/current/userguide/osgi_plugin.html) plugin or feature rich [org.dm.bundle](https://github.com/TomDmitriev/gradle-bundle-plugin) plugin.
* OSGi Declarative Services annotations support (instead of SCR, [see docs](http://blogs.adobe.com/experiencedelivers/experience-management/osgi/using-osgi-annotations-aem6-2/)).

## Requirements

* Java >= 8, but target software can be compiled to older Java.

## Configuration

* Recommended way to start using Gradle AEM Plugin is to clone and customize [example project](https://github.com/Cognifide/gradle-aem-example).
* General AEM configuration options are listed [here](src/main/kotlin/com/cognifide/gradle/aem/AemConfig.kt).
* As a build command, it is recommended to use Gradle Wrapper (`gradlew`) instead of locally installed Gradle (`gradle`) to easily have same version of build tool installed on all environments. At first time, wrapper will be automatically downloaded and installed.

### Plugin setup

Released versions of plugin are available on [Bintray](https://bintray.com/cognifide/maven-public/gradle-aem-plugin), 
so that this repository need to be included in *buildscript* section.

#### Minimal:

```groovy
buildscript {
    repositories {
        jcenter()
        maven { url  "http://dl.bintray.com/cognifide/maven-public" }
    }
    
    dependencies {
        classpath 'com.cognifide.gradle:aem-plugin:2.0.16'
    }
}

apply plugin: 'com.cognifide.aem.package'
```

Building and deploying to AEM via command: `gradlew aemBuild`.

#### Full (defaults):

```groovy
apply plugin: 'com.cognifide.aem.instance'
apply plugin: 'kotlin' // 'java' or whatever you like to compile bundle

defaultTasks = [':aemSatisfy', ':aemBuild', ':aemAwait']

aem {
    config {
        localInstance "http://localhost:4502"
        localInstance "http://localhost:4503"
        
        deployConnectionTimeout = 5000
        deployParallel = true
        deploySnapshots = []
        uploadForce = true
        recursiveInstall = true
        acHandling = "merge_preserve"
        contentPath = project.file("src/main/content")
        if (project == project.rootProject) {
            bundlePath = "/apps/${project.name}/install"
        } else {
            bundlePath = "/apps/${project.rootProject.name}/${project.name}/install"
        }
        localPackagePath = ""
        remotePackagePath = ""
        filesExcluded = [
          "**/.gradle",
          "**/.git",
          "**/.git/**",
          "**/.gitattributes",
          "**/.gitignore",
          "**/.gitmodules",
          "**/.vlt",
          "**/node_modules/**",
          "jcr_root/.vlt-sync-config.properties"
        ]
        filesExpanded = [
          "**/META-INF/vault/*.xml"
        ]
        fileProperties = []
        vaultCopyMissingFiles = true
        vaultFilesPath = project.rootProject.file("src/main/resources/META-INF/vault")
        vaultSkipProperties = [
          "jcr:uuid!**/home/users/*,**/home/groups/*",
          "jcr:lastModified",
          "jcr:created",
          "cq:lastModified*",
          "cq:lastReplicat*",
          "*_x0040_Delete",
          "*_x0040_TypeHint"
        ]
        vaultGlobalOptions = "--credentials {{instance.credentials}}"
        vaultLineSeparator = System.lineSeparator()
        dependBundlesTaskNames = ["assemble", "check"]
        dependContentTaskNames = ["aemCompose.dependencies"]
        buildDate = Date()
        instancesPath = "${System.getProperty("user.home")}/.aem/${project.rootProject.name}"
        instanceFilesPath = project.rootProject.file("src/main/resources/${AemInstancePlugin.FILES_PATH}")
        instanceFilesExpanded = [
          "**/*.properties", 
          "**/*.sh", 
          "**/*.bat", 
          "**/*.xml",
          "**/start",
          "**/stop"
        ]
        awaitDelay = 3000
        awaitInterval = 1000
        awaitTimeout = 900
        awaitTimes = 300
        satisfyRefreshing = false
    }
}

aemSatisfy {
    local("pkg/vanityurls-components-1.0.2.zip")
    url("https://github.com/Cognifide/APM/releases/download/cqsm-3.0.0/apm-3.0.0.zip")
    url("smb://company-share/aem/packages/my-lib.zip")
    url("sftp://company-share/aem/packages/other-lib.zip")
    url("file:///C:/Libraries/aem/package/extra-lib.zip")
}

```

Building and deploying to AEM via command: `gradlew` (default tasks will be used).

Instances configuration can be omitted, then *http://localhost:4502* and *http://localhost:4503* will be used by default.
Content path can also be skipped, because value above is also default. This is only an example how to customize particular [values](src/main/kotlin/com/cognifide/gradle/aem/AemConfig.kt).

For multi project build configuration, see [example project](https://github.com/Cognifide/gradle-aem-example).

### Workflow

Initially, to create fully configured local AEM instances simply run command `gradlew aemSetup`.

Later during development process, building and deploying to AEM should be done using most simple command: `gradle`.
Above configuration uses default tasks, so that alternatively it is possible to build same using explicitly specified command `gradlew aemSatisfy aemBuild aemAwait`.

* Firstly dependent packages (like AEM hotfixes, Vanity URL Components etc) will be installed lazily (only when they are not installed yet).
* In next step application is being built and deployed to all configured AEM instances.
* Finally build awaits till all AEM instances be stable.

### Tasks

* `aemSetup` - Perform initial setup of local AEM instance(s). Automated version of `aemCreate aemUp aemSatisfy aemBuild`.
* `aemCreate` - Create local AEM instance(s). To use it specify required properties in ignored file *gradle.properties* at project root (protocols supported: SMB, SSH, HTTP(s) or local path, SMB as example):
    * `aem.instance.local.jarUrl=smb://[host]/[path]/cq-quickstart.jar`
    * `aem.instance.local.licenseUrl=smb://[host]/[path]/license.properties`
    * `aem.smb.domain=MYDOMAIN`
    * `aem.smb.username=MYUSER`
    * `aem.smb.password=MYPASSWORD`
* `aemUp` - Turn on local AEM instance(s).
* `aemDown` - Turn off local AEM instance(s).
* `aemDestroy` - Destroy local AEM instance(s).
* `aemAwait` - Wait until all local AEM instance(s) be stable.
* `aemCompose` - Compose CRX package from JCR content and bundles. Available methods:
    * `includeProject(projectPath: String)`, includes both bundles and JCR content from another project, example: `includeProject ':core'`.
    * `includeContent(projectPath: String)`, includes only JCR content, example: `includeContent ':design'`.
    * `includeBundles(projectPath: String)`, includes only bundles, example: `includeBundles ':common'`.
    * `includeBundlesAtPath(projectPath: String, installPath: String)`, includes only bundles at custom install path, example: `includeBundles(':common', '/apps/my-app/install')`.
    * `includeBundles(projectPath: String, runMode: String)`, as above, useful when bundles need to be installed only on specific type of instance.
    * `mergeBundles(projectPath: String)`, includes only bundles at same install path.
    * `mergeBundles(projectPath: String, runMode: String)`, as above, useful when bundles need to be installed only on specific type of instance.
    * `includeProjects(pathPrefix: String)`, includes both bundles and JCR content from all AEM projects (excluding itself) in which project path is matching specified filter. Vault filter roots will be automatically merged and available in property `${filterRoots}` in *filter.xml* file. Useful for building assemblies (all-in-one packages).
    * `includeSubprojects()`, alias for method above: `includeProjects("${project.path}:*")`.
    * all inherited from [ZIP task](https://docs.gradle.org/3.5/dsl/org.gradle.api.tasks.bundling.Zip.html).
* `aemUpload` - Upload composed CRX package into AEM instance(s).
* `aemDelete` - Delete uploaded CRX package from AEM instance(s).
* `aemInstall` - Install uploaded CRX package on AEM instance(s).
* `aemUninstall` - Uninstall uploaded CRX package on AEM instance(s).
* `aemPurge` - Fail-safe combination of `aemUninstall` and `aemDelete`.
* `aemActivate` - Replicate installed CRX package to other AEM instance(s).
* `aemDeploy` - Upload & install CRX package into AEM instance(s). Primary, recommended form of deployment. Optimized version of `aemUpload aemInstall`.
* `aemDistribute` - Upload, install & activate CRX package into AEM instances(s). Secondary form of deployment. Optimized version of `aemUpload aemInstall aemActivate -Paem.deploy.instance.name=*-author`.
* `aemSatisfy` - Upload & install dependent CRX package(s) before deployment. Available methods:
    * `local(path: String)`, use CRX package from local file system.
    * `local(file: File)`, same as above, but file can be even located outside the project.
    * `url(url: String)`, use CRX package that will be downloaded from specified URL to local temporary directory.
    * `downloadHttp(url: String)`, download package using HTTP with no auth.
    * `downloadHttpAuth(url: String, username: String, password: String)`, download package using HTTP with Basic Auth support.
    * `downloadHttpAuth(url: String)`, as above, but credentials must be specified in variables: `aem.http.username`, `aem.http.password`. Optionally enable SSL errors checking by setting property `aem.http.ignoreSSL` to `false`.
    * `downloadSmbAuth(url: String, domain: String, username: String, password: String)`, download package using SMB protocol.
    * `downloadSmbAuth(url: String)`, as above, but credentials must be specified in variables: `aem.smb.domain`, `aem.smb.username`, `aem.smb.password`.
    * `downloadSftpAuth(url: String, username: String, password: String)`, download package using SFTP protocol.
    * `downloadSftpAuth(url: String)`, as above, but credentials must be specified in variables: `aem.sftp.username`, `aem.sftp.password`. Optionally enable strict host checking by setting property `aem.sftp.hostChecking` to `true`.
    * `group(name: String, configurer: Closure)`, useful for declaring group of packages (or just naming single package) to be installed only on demand. For instance: `group 'tools', { url('http://example.com/package.zip'); url('smb://internal-nt/package2.zip')  }`. Then to install only packages in group `tools`, use command: `gradlew aemSatisfy -Paem.satisfy.group=tools`.
* `aemCollect` - Composes ZIP package from all CRX packages being satisfied and built. Available methods:
    * all inherited from [ZIP task](https://docs.gradle.org/3.5/dsl/org.gradle.api.tasks.bundling.Zip.html).
* `aemCheckout` - Check out JCR content from running AEM author instance to local content path.
* `aemClean` - Clean checked out JCR content.
* `aemSync` - Check out then clean JCR content.
* `aemVlt` - Execute any Vault command. See parameters section for more details.
* `aemDebug` - Dumps effective AEM build configuration of project to JSON file.

### Task rules

* `aem<ProjectPath>Build` - Build CRX package and deploy it to AEM instance(s). It is recommended to include appropriate deploy task name in [default tasks](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html#sec:default_tasks) of project. For instance, to deploy project at path `:app:design` use task named `aemAppDesignBuild`.

### Expandable properties

By default, plugin is configured that in all XML files located under path *META-INF/vault* properties can be injected using syntax: `${property}`.

Related configuration:

```groovy
aem {
    config {
        fileProperties = [
            "organization": "My Company"
        ]
        filesExpanded = [
            "**/META-INF/vault/*.xml"
        ]
    }
}
```

This feature is specially useful to generate valid *META-INF/properties.xml* file.

Predefined properties:
* `rootProject` - project with directory in which *settings.gradle* is located.
* `project` - current project.
* `config` - [AEM configuration](src/main/kotlin/com/cognifide/gradle/aem/AemConfig.kt).
* `buildDate` - date when CRX package composing started.
* `buildCount` - number to be used as CRX package build count (`buildDate` in format `yDDmmssSSS`).
* `created` - current date in *ISO8601* format.
* `name` - text used as CRX package name. Considers multi-project structure (names of subpackages are prefixed with root project name).

Maven fallback properties (useful when migrating project):

* `project.groupId` - alias for `project.group`.
* `project.artifactId` - alias for `project.name`.
* `project.build.finalName` - alias for `${project.name}-${project.version}`.

Task specific:
* `aemCompose` - properties which are being dynamically calculated basing on content actually included into package.
   * `filterRoots` - after using method `includeContent` of `aemCompose` task, all Vault filter roots are being gathered. This property contains all these XML tags concatenated especially useful for building assemblies. If no projects will be included, then this variable will contain a single filter root with bundle path to be able to deploy auto-generated package with JAR file only.
* `aemVlt` - properties are being injected to command specified in `aem.vlt.command` property. Following properties are being used internally also by `aemCheckout`.
   * `instance` - instance used to communicate with while performing Vault commands. Determined by (order take precedence): properties `aem.vlt.instance`, `aem.deploy.instance.list`, `aem.deploy.instance.name` and as fallback first instance which name matches filter `*-author`.
   * `filter` - file name or path to Vault workspace filter file  *META-INF/vault/filter.xml*. Determined by (order take precedence): property: `aem.vlt.filter`, configuration `contentPath` property suffixed with `META-INF/vault/filter.xml`. 

### How to

#### Set AEM configuration properly for all / concrete project(s)

Global configuration like AEM instances should be defined in root *build.gradle* file:

```groovy
allprojects { subproject ->
  plugins.withId 'com.cognifide.aem.base', {
    aem {
        config {
          localInstance("http://localhost:6502")
          contentPath = subproject.file("src/main/aem")
        }
    }
  }
}
```

Project `:app` specific configuration like CRX package options should be defined in `app/build.gradle`:

```groovy
aemCompose {
    archiveName = 'company-example'
    duplicatesStrategy = "EXCLUDE"
    includeProject ':app:core'
}
```

Warning! Very often plugin users mistake is to configure `aemSatisfy` task in `allprojects` closure. 
As an effect there will be same dependent CRX package defined multiple times.

#### Work with local and / or remote AEM instances

In AEM configuration section, there is possibility to use `localInstance` or `remoteInstance` methods to define AEM instances to be used to:
 
* install CRX packages being built via commands: `aemBuild`, `aemDeploy` and more detailed `aemUpload`, `aemInstall` etc,
* communicate with while using Vault tool in commands `aemSync`, `aemCheckout`, `aemVlt`,
* install dependent packages while using `aemSatisfy` command.

```groovy
aem {
    config {
      localInstance("http://localhost:4502") // local-author
      localInstance("http://localhost:4502", "admin", "admin", "author", 14502) // local-author
      
      localInstance("http://localhost:4503") // local-publish
      localInstance("http://localhost:4503", "admin", "admin", "publish", 14502) // local-publish
      
      remoteInstance("http://192.168.10.1:4502", "user1", "password2", "integration") // integration-author
      remoteInstance("http://192.168.10.2:4503", "user2", "password2", "integration") // integration-publish
    }
}
```

Rules:

* Instance name is a combination of `${environment}-${type}` e.g *local-author*, *integration-publish* etc.
* Only instances being defined as *local* are being considered in command `aemSetup`, `aemCreate`, `aemUp` etc (that comes from `com.cognifide.aem.instance` plugin).
* All instances being defined as *local* or *remote* are being considered in commands CRX package deployment related like `aemBuild`, `aemDeploy` etc.

#### Deploy CRX package(s) only to filtered group of instances:

When there are defined named AEM instances: `local-author`, `local-publish`, `integration-author` and `integration-publish`,
then it is available to deploy packages with taking into account: 

 * type of environment (local, integration)
 * type of AEM instance (author / publish)

```bash
gradlew aemDeploy -Paem.deploy.instance.name=integration-*
gradlew aemDeploy -Paem.deploy.instance.name=*-author
```

Default value of that instance name filter is `local-*`.
   
#### Deploy CRX package(s) only to instances specified explicitly

List delimited: instances by semicolon, instance properties by comma.

```bash
gradlew aemDeploy -Paem.deploy.instance.list=http://localhost:4502,admin,admin;http://localhost:4503,admin,admin
```

#### Deploy only filtered dependent CRX package(s)

Filters with wildcards, comma delimited.

```bash
gradlew aemSatisfy -Paem.satisfy.group=hotfix-*,groovy-console
```

#### Check out and clean JCR content using filter at custom path
   
E.g for subproject `:content`:
   
```bash
gradlew :content:aemSync -Paem.vlt.filter=custom-filter.xml
gradlew :content:aemSync -Paem.vlt.filter=src/main/content/META-INF/vault/custom-filter.xml
gradlew :content:aemSync -Paem.vlt.filter=C:/aem/custom-filter.xml
```

#### Check out and clean JCR content using filter roots specified explicitly
   
```bash
gradlew :content:aemSync -Paem.vlt.filterRoots=[/etc/tags/example,/content/dam/example]
```

#### Execute any Vault command 

E.g copy nodes from one remote AEM instance to another.

```bash
gradlew :content:aemVlt -Paem.vlt.command='rcp -b 100 -r -u -n http://admin:admin@localhost:4502/crx/-/jcr:root/content/dam/example http://admin:admin@localhost:4503/crx/-/jcr:root/content/dam/example' 
```

See [VLT Tool documentation](https://docs.adobe.com/docs/en/aem/6-2/develop/dev-tools/ht-vlttool.html).
Gradle requires to have working directory with file *build.gradle* in it, but Vault tool can work at any directory under *jcr_root*. To change working directory for Vault, use property `aem.vlt.path` which is relative path to be appended to *jcr_root* for project task being currently executed.

#### Assemble all-in-one CRX package(s)

Let's assume following project structure (with build file contents):

* build.gradle (project `:`)

```groovy
apply plugin: 'com.cognifide.aem.package'

aemCompose {
    includeProjects(':app:*')
    includeProjects(':content:*')
    includeProject(':migration')
}

```

When building via command `gradlew :build`, then the effect will be a CRX package with assembled JCR content and OSGi bundles from projects: `:app:core`, `:app:common`, `:content:init`, `:content:demo` and `:migration`.

* app/build.gradle  (project `:app`)

```groovy
apply plugin: 'com.cognifide.aem.package'

aemCompose {
    includeSubprojects()
}

```

When building via command `gradlew :app:build`, then the effect will be a CRX package with assembled JCR content and OSGi bundles from projects: `:app:core`, `:app:common` only.

* *app/core/build.gradle* (project `:app:core`, JCR content and OSGi bundle)
* *app/common/build.gradle* (project `:app:common`, JCR content and OSGi bundle)
* *content/init/build.gradle* (project `:content:init`, JCR content only)
* *content/demo/build.gradle* (project `:content:demo`, JCR content only)
* *migration/build.gradle* (project `:content:demo`, JCR content only)
* *test/integration/build.gradle* (project `:test:integration`, any source code)
* *test/functional/build.gradle* (project `:test:functional`, any source code)

Gradle AEM Plugin is configured in that way that project can have:
 
* JCR content and source code to compile OSGi bundle
* source code to compile OSGi bundle
* both

By distinguishing `includeProject`, `includeBundle` or `includeContent` there is ability to create any assembly CRX package with content of any type without restructuring the project.
Only one must have rule to be kept while developing a multi-module project is that **all Vault filter roots of all projects must to be exclusive**.
In general, they are most often exclusive, to avoid strange JCR installer behaviors, but sometimes exceptional [workspace filter](http://jackrabbit.apache.org/filevault/filter.html) rules are being applied like `mode="merge"` etc.

#### Skip installed package resolution by download name. 

```bash
gradlew aemInstall -Paem.deploy.skipDownloadName=true
```

Only matters when Vault properties file is customized then that property could be used to eliminate conflicts.

## Known issues

### Caching task `aemCompose`

Expandable properties with dynamically calculated value (unique per build) like `created` and `buildCount` are not used by default generated properties file intentionally, 
because such usages will effectively forbid caching `aemCompose` task and it will be never `UP-TO-DATE`.

### Vault tasks parallelism

Vault tool current working directory cannot be easily configured, because of its API. AEM plugin is temporarily changing current working directory for Vault, then returning it back to original value.
In case of that workaround, Vault tasks should not be run in parallel (by separated daemon processed / JVM synchronization bypassed), because of potential unpredictable behavior.

### Files from SSH for `aemCreate` and `aemSatisfy`

Local instance JAR file can be provided using SSH, but SSHJ client used in implementation has an [integration issue](https://github.com/hierynomus/sshj/issues/347) related with JDK and Crypto Policy.
As a workaround, just run build without daemon (`--no-daemon`).

## License

**Gradle AEM Plugin** is licensed under the [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)
