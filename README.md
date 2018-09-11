# NS Auto Jenkins Plugin

## [Docs](https://docs.nowsecure.com/auto/integration-services/jenkins-integration/)

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
cp target/nowsecure-auto-securitytest.hpi ~/.jenkins/plugins/
```
Then redeploy Jenkins.

## Create Mobile build
![New Build](https://github.com/viaforensics/auto-jenkins-plugin/blob/master/images/jenkins1.png)


## Adding Plugin to your mobile build
Select Configure option from Jenkins console, then select dropdown of build step and choose NS Auto Jenkins Plugin, e.g.

![Build Step](https://github.com/viaforensics/auto-jenkins-plugin/blob/master/images/jenkins2.png)

## Specify configuration parameters
![Configure Step](https://github.com/viaforensics/auto-jenkins-plugin/blob/master/images/jenkins3.png)


## Kick off your build and then see results
![Build Results](https://github.com/viaforensics/auto-jenkins-plugin/blob/master/images/jenkins4.png)

