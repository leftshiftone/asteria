# asteria-deploy

_asteria-deploy_ is used to deploy an application or a ZIP file containing 
all the necessary build information (like Docker image metadata) to a cloud provider
such as Amazon Web Services.

Currently the following cloud providers are supported:
* ***Amazon Web Services - ElasticBeanstalk (Multicontainer Docker Environment)***

## Configuration for AWS EB
_asteria-docker_ uses a configuration file called ```deployment.config``` by default.
The location ```deployment.config``` location **must** be the project root directory!


The configuration file must be present for _asteria-deploy_ to work. The configuration
parameters may be split into root tags where each defines where the application should be deployed (deployment modes). 

During execution via `deployElasticbeanstalk` you must specify the which deployment mode(s) is/are the target for the deployment.
This can be done using the Gradle project property `-Pdeployment.mode=FIRST,SECOND`. Multiple modes may be selected by
passing the modes as comma separated values.

Examples:
> given that all modes are correctly specified in `deployment.config`
1. **snapshot** - via: ```-Pdeployment.mode=snapshot```
2. **release**  - via: ```-Pdeployment.mode=release```
3. **somethingelse** - via: ```-Pdeployment.mode=somethingelse```
4. **snapshot and somethingelse** - via ```-Pdeployment.mode=snapshot,somethingelse```

> If the ```deployment.mode``` property is omitted during execution an ```MissingDeploymentModeException``` is thrown!

The following confiuration parameters may be used:
* **bucketName** _[REQUIRED]_ - name of the S3 Bucket the application bundle (application version) will be uploaded to
* **applicationName** _[REQUIRED]_ - Name of the Elasticbeantalk Application
* **environmentName** _[REQUIRED]_ - Name of the Elasticbeanstalk Environment
* **dockerrunLocation** _[REQUIRED]_ - Location of the Dockerrun definition file relative to the location of
the configuration file
* **awsRegion** _[OPTIONAL]_ - specifies the target region for the deployment unit. If not set the default region as specified in the default provider chain is used.

**Example Configuration:**
```hocon
snapshot {
    elasticbeanstalk {
        bucketName = "elasticbeanstalk-eu-west-1-007098893018",
        applicationName = "gaia-beta",
        environmentName = "gaia-saas-aws-beta",
        awsRegion = "eu-west-1"
        dockerrunLocation = "Dockerrun.aws.json"
 }
}

release {
    elasticbeanstalk {
        bucketName = "elasticbeanstalk-eu-central-1-007098893018",
        applicationName = "gaia-live",
        environmentName = "gaia-saas-aws-live",
        awsRegion = "eu-central-1"
        dockerrunLocation = "Dockerrun.live.aws.json"
 }
}
```

## Tasks

Currently _asteria-deploy_ offers the following tasks:

* **prepareDockerrun**
  
  Replaces tokens (e.g ```${version}```) in the deployment description file (file specified under ```dockerrunLocation```) file and copies the resolved file to the ```project.buildDir/Dockerrun.aws.json```
  
  The following tokens are currently supported: 
  * ```${version}``` - Extracted from either asteria-version or from project.version set in ```build.gradle```
  

* **deployElasticbeanstalk**
  
  Creates and deploys a ZIP containing the deploymentDescription file to AWS Elasticbeanstalk using the parameters specified
  in the ```deployment.config``` file
