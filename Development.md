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

## Testing Master/Slave
- Create Slave node in Jenkins
- Download agent.jar from master
```
url http://host:port/jnlpJars/agent.jar
```
- Start slave from another machine
```
java -jar agent.jar -jnlpUrl http://host:port/jenkins/computer/slave1/slave-agent.jnlp -secret xxxx -workDir ""
```
Note: in above example, slave is called slave1. You can go to slave node config in Jenkins to see the secret.

## Jenkins Upload permissions
- https://github.com/jenkins-infra/repository-permissions-updater/permissions/plugin-nowsecure-auto-security-test.yml

## Wiki
- https://wiki.jenkins.io/display/JENKINS/NowSecure+AUTO+Jenkins+Plugin


## Resources
- https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins
- https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin
- https://github.com/jenkins-infra/repository-permissions-updater
- https://wiki.jenkins.io/display/JENKINS/Plugins
- https://repo.jenkins-ci.org/releases/io/jenkins/plugins/nowsecure-auto-security-test/0.1/
