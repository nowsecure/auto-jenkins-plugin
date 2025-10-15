# NowSecure Auto Jenkins Plugin

### Deprecation Notice

This Extension has been deprecated.  NowSecure strongly recommends migrating to the updated NowSecure Jenkins Plugin which can be found in the [JenkinsCI Organization's GitHub](https://github.com/jenkinsci/nowsecure-ci-assessments-plugin).

Migration to the new extension is straightforward:

1. Install the new `NowSecure CI Assessments` plugin from the Jenkins Plugin Manager
2. Review the parameters for the new extension taking care to evaluate the updated default values of parameters to ensure the meet your requirements.

Take note of `analysis_type` which is a new parameter. This parameter allows you to run static only or full assessments.

## Overview

This plugin provides the ability to send Android or iOS applications to NowSecure Auto for Mobile Application Security Testing (aka MAST)

Documentation for 


## Building
```
mvn hpi:run
```

## Findbugs
```
mvn findbugs:gui
```

## Installing
```
mvn clean install
cp target/nowsecure-auto-security-test.hpi ~/.jenkins/plugins/
```
Then redeploy Jenkins.

## Deploying

### Generate API Key
See https://docs.nowsecure.com/auto/integration-services/jenkins-integration for adding API token.

### Store API Key in Jenkins Credentials
Select Credentials from sideline:
![Credentials](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins3.png)

Store API Key as Secret text:
![StoreCredentials](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins4.png)

### Define Jenkins Job
![New Build](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins1.png)

### Bind Credentials with apiKey variable
![Bind](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins5.png)

### Adding Plugin to your mobile build
Select Configure option from Jenkins console, then select dropdown of build step and choose NS Auto Jenkins Plugin, e.g.
![Build Step](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins6.png)

### Specify configuration parameters
![Configure Step](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins7.png)

## Advanced configuration options
![Advanced](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins8.png)

## Kick off build
Kick off your mobile builds and you will see the raw JSON reports and score under artifacts folder.
![Console](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins9.png)

## Pipeline
Adding plugin to pipeline:
![Pipeline](https://github.com/nowsecure/auto-jenkins-plugin/blob/master/images/jenkins10.png)

## Resources
- https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin


