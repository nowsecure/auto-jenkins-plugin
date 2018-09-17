# NowSecure AUTO Jenkins Plugin

### Generate API Key
See https://docs.nowsecure.com/auto/integration-services/jenkins-integration for adding API token.

### Store API Key in Jenkins Credentials
Select Credentials from sideline:
![Credentials](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins3.png)

Store API Key as Secret text:
![StoreCredentials](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins4.png)

### Define Jenkins Job
![New Build](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins1.png)

### Bind Credentials with apiKey variable
![Bind](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins5.png)

### Adding Plugin to your mobile build
Select Configure option from Jenkins console, then select dropdown of build step and choose NS Auto Jenkins Plugin, e.g.
![Build Step](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins6.png)

### Specify configuration parameters
![Configure Step](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins7.png)

## Advanced configuration options
![Advanced](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins8.png)

## Kick off build
Kick off your mobile builds and you will see the raw JSON reports and score under artifacts folder.
![Console](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins9.png)

## Pipeline
Adding plugin to pipeline:
![Pipeline Setup](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins10.png)

![Pipeline Config](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins11.png)

![Pipeline Script](https://github.com/jenkinsci/nowsecure-auto-security-test-plugin/blob/master/images/jenkins12.png)

Sample pipeline script
```
pipeline {
    agent any
    stages {
        stage('security-test') {
            environment {
                apiKey = credentials('AutoApiKey')
            }
            steps {
                step([$class: 'NSAutoPlugin', apiKey: env.apiKey, binaryName: 'myapk.apk', breakBuildOnScore: true, description: 'my description', group: 'mygroup', waitForResults: true])
            }
        }
    }
}
```

## Resources
See https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins
- https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin


