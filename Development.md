# NowSecure AUTO Jenkins Plugin

## Building
```
mvn hpi:run
```

## Findbugs
```
mvn findbugs:gui
```

## Local Installing
```
mvn clean install
cp target/nowsecure-auto-security-test.hpi ~/.jenkins/plugins/
```
Then redeploy Jenkins.

## Jenkins Host Deployment
```
mvn release:prepare release:perform -Dusername=... -Dpassword=...
```

## Jenkins Upload permissions
- https://github.com/jenkins-infra/repository-permissions-updater/permissions/plugin-nowsecure-auto-security-test.yml

## Wiki
- https://wiki.jenkins.io/display/JENKINS/NowSecure+AUTO+Jenkins+Plugin


## Resources
- https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins
- https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin
- https://github.com/jenkins-infra/repository-permissions-updater
- https://wiki.jenkins.io/display/JENKINS/Plugins
