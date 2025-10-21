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


This plugin adds the ability to perform automatic mobile app security testing for Android and iOS mobile apps through the NowSecure AUTO test engine.

## Summary:
Purpose-built for mobile app teams, NowSecure AUTO provides fully automated, mobile appsec testing coverage (static+dynamic+behavioral tests) optimized for the dev pipeline. Because NowSecure tests the mobile app binary post-build from Jenkins, it can test software developed in any language and provides complete results including newly developed code, 3rd party code, and compiler/operating system dependencies. With near zero false positives, NowSecure pinpoints real issues in minutes, with developer fix details, and routes tickets automatically into ticketing systems, such as Jira. NowSecure is frequently used to perform security testing in parallel with functional testing in the dev cycle. Requires a license for and connection to the NowSecure AUTO software.
 https://www.nowsecure.com

