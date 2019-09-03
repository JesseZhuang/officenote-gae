## Office Note

### maintenance

Every year before school starts, update the five dates in gcloud datastore "SchoolYearDates" kind accordingly.

After a few years, the out dated archived blurbs can be deleted. For example, in 2025, we can delete all blurbs in Blurb2018.

## appengine-standard-archetype

This is a generated App Engine Standard Java application from the appengine-standard-archetype archetype.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/


* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](https://maven.apache.org/download.cgi) (at least 3.5)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)

### Setup

    gcloud init
    gcloud auth application-default login

Actually

    gcloud auth login
    gcloud config set project ${PROJECT_ID}
    # when switching gcloud account
    rm ~/.appcfg_oauth2_tokens_java

https://cloud.google.com/code/docs/intellij/migrate

Note Google Application Engine java does not seem to work well with jdk 12.

```bash
brew tap AdoptOpenJDK/openjdk
brew cask install <version>
brew cask uninstall <version>
jenv enable-plugin maven
mvn -version # check mvn using java runtime version
```

### Maven
#### Running locally

    mvn appengine:devserver

#### Deploying

    mvn appengine:update
    mvn appengine:deploy

#### Testing

    mvn verify

As you add / modify the source code (`src/main/java/...`) it's very useful to add
[unit testing](https://cloud.google.com/appengine/docs/java/tools/localunittesting)
to (`src/main/test/...`).  The following resources are quite useful:

* [Junit4](http://junit.org/junit4/)
* [Mockito](http://mockito.org/)
* [Truth](http://google.github.io/truth/)

## Data Export and Import

https://cloud.google.com/datastore/docs/export-import-entities

### Updating to latest Artifacts

An easy way to keep your projects up to date is to use the maven [Versions plugin][versions-plugin].

    mvn versions:display-plugin-updates
    mvn versions:display-dependency-updates
    mvn versions:use-latest-versions

Note - Be careful when changing `javax.servlet` as App Engine Standard uses 3.1 for Java 8, and 2.5
for Java 7.

Our usual process is to test, update the versions, then test again before committing back.

[plugin]: http://www.mojohaus.org/versions-maven-plugin/
