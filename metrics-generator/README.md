# Metrics Generator Lambda

## Setup

1. Install [Node.js](https://nodejs.org/), [Gradle](https://gradle.org/), and [Java](https://adoptopenjdk.net/)

2. Install and update all packages.

       npm install
       npx ncu -u

       gradle wrapper
       ./gradlew dependencyUpdates

## Build

    ./gradlew build

## Deploy

A standalone deployment of Serverless to AWS requires a unique stage name:

    enableCustomDomain=false npx sls deploy --stage my-deploy --region us-east-2

    npx sls deploy --stage my-stack --region us-east-2


