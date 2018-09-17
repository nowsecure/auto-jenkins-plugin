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

## Resources
See https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins
- https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin


