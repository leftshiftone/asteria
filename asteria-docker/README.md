# asteria-docker

First and foremost _asteria-docker_ builds and pushes Docker images to a specified (remote) repository. 

## Configuration
_asteria-docker_ provides the following gradle extension parameters which must be specified in the project that applies
the plugin.

* **dockerfile**: The location of the _Dockerfile_ that should be used to build the image
* **name**: The name of the image
* **repositoryURI**: The URI to the remote repository to which the image should be published 
* **additionalFiles** _[optional]_: Additional files besides the built artifact that should be considered for the Docker build process (files that are copied to buildDir/docker/)
* **copyArtifacts** _[optional]_: Overrides the default behaviour of copying the built JAR artifact to `$buildDir/docker/app.jar` _[Default: true]_
* **withLatestTag** _[optional]_: Pushes the image again with "latest" tag _[Default: false]_

**Example Configuration:**

```groovy
asteriaDocker {
    dockerfile = file("$project.projectDir/Dockerfile")
    name = "gaia-web"
    repositoryURI = "007098893018.dkr.ecr.eu-central-1.amazonaws.com"
    additionalFiles = files("${project.buildDir}/spacy_server.py")
}
```

_asteria-docker_ provides the following options to change behaviour of tasks at runtime:
* **explicitTag** _[optional]_: Can modify _dockerBuild_ and _dockerPush_ command to override the inferred tag 
and specify the tag that should be used explicitly. If the provided string is a branch name, the ticket ID is inferred 
and used as tag.
 
 Example:
 ```shell script
 gradlew dockerPush --explicitTag  bug/TICKET-123 # Tag the image as TICKET-123 because ticket id is inferred.
 ```

## Defaults
_asteria-docker_ will assemble the Docker image id (_repositoryURI/name:TAG_) for you.
The repositoryURI and name is taken straight from the configuration while the value of the tag depends
on if the _asteria-version_ plugin is applied or not.

If _asteria-version_ is applied the TAG will be assembled from the _build.properties_ file created 
by _asteria-version_ otherwise if __asteria-version_ is **not** applied the project gradle version is used.

### With applied asteria-version

```properties
#Fri Jun 08 20:42:25 UTC 2018
version=0.8.0-SNAPSHOT
revision=28dde56abe7475c6d77c25ac9f3a8789878
timestamp=2018.06.19-23.17.55-CEST
```

If the version in _build.properties_ contains "-SNAPSHOT" it is assumed a snapshot is build. 
Otherwise if any other semantic versioning string is set as version this string will be used for the tag.

For the example the tag would be: **0.8.0-dev.28dde56.2018.06.19-23.17.55-CEST**

Non-snapshot versions are assumed to be releases (if build.properties is present) and
everything but the version is omitted.

The tag would be: **0.8.0**

If the image should be pushed with the version tag as mentioned above and with the **latest** tag
the extension parameter `withLatestTag = true` may be used.
## Tasks
_asteria-docker_ currently offers the following tasks

* **dockerCopy**
  
  Copies the Dockerfile to project.buildDir/docker 

* **dockerCopyArtifacts**

  Copies the artifact JAR to project.buildDir/docker
  
* **dockerCopyAdditionalFiles**

  Copies all additionally specified files to project.buildDir/docker

* **dockerBuild**

  Triggers a docker build

* **dockerPush**

  Triggers a docker push

Each of the tasks depends on its predecessor in the list above building the following 
dependency graph:
```dockerPush - (dependsOn)-> dockerBuild - (dependsOn) -> dockerCopyAdditionalFiles```
et cetera.
