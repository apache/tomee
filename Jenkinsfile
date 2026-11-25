/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Used by the "Pipeline script from SCM" job https://ci-builds.apache.org/job/TomEE/job/main-pipeline/
// Every stage with its own agent checks out the repository itself (Declarative's implicit "checkout scm").
pipeline {
    agent none

    // agent none skips installing these at the top level; every stage with its own agent gets them
    tools {
        maven 'maven_3_latest'
        jdk 'jdk_17_latest'
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        disableConcurrentBuilds()
        timeout(time: 12, unit: 'HOURS')
    }

    // every mvn call uses a local repository inside its stage's workspace: the agents' shared ~/.m2 gets
    // modified by other stages and jobs while a build reads it (missing classes in plugin and dependency jars)
    environment {
        MAVEN_ARGS = '-B -U --show-version --fail-at-end -Dmaven.test.failure.ignore=true'
    }

    stages {
        stage('Sanity checks') {
            agent { label 'ubuntu' }
            steps {
                sh 'mvn -Dmaven.repo.local="$WORKSPACE/.repository" -T 2C clean install -Pstyle,rat -DskipTests'
            }
        }

        stage('Build and test') {
            parallel {
                // the job only builds main, so no "when { branch 'main' }": that condition needs a multibranch job
                stage('Deploy snapshots') {
                    agent { label 'ubuntu' }
                    steps {
                        sh 'mvn -Dmaven.repo.local="$WORKSPACE/.repository" clean deploy -DskipTests -Pno-examples'
                    }
                }

                stage('Tests') {
                    agent { label 'ubuntu' }
                    stages {
                        stage('Build') {
                            steps {
                                sh 'mvn -Dmaven.repo.local="$WORKSPACE/.repository" -T 2C clean install -DskipTests'
                            }
                        }
                        stage('Core and Arquillian') {
                            steps {
                                // quick = every module except examples, itests and tck
                                // clean: the -T 2C build above can leave runtime files (conf/conf.d) in the distribution work dirs
                                sh 'mvn -Dmaven.repo.local="$WORKSPACE/.repository" clean install -Pquick,all-adapters'
                            }
                        }
                        stage('Integration tests') {
                            steps {
                                sh 'mvn -Dmaven.repo.local="$WORKSPACE/.repository" install -f itests/pom.xml'
                            }
                        }
                        stage('Examples') {
                            steps {
                                sh 'mvn -Dmaven.repo.local="$WORKSPACE/.repository" install -f examples/pom.xml'
                            }
                        }
                    }
                    post { always { junit allowEmptyResults: true, testResults: '**/target/*-reports/TEST-*.xml' } }
                }

                stage('TCK') {
                    agent { label 'ubuntu' }
                    steps {
                        sh 'mvn -Dmaven.repo.local="$WORKSPACE/.repository" -T 2C clean install -DskipTests'
                        sh 'mvn -Dmaven.repo.local="$WORKSPACE/.repository" install -pl tck -amd -Pall-adapters,tck-cdi,tck-jsonp,tck-jsonb,tck-jax-rs,mp-tck-all,tck-concurrency,tck-security,tck-data'
                    }
                    post { always { junit allowEmptyResults: true, testResults: '**/target/*-reports/TEST-*.xml' } }
                }
            }
        }
    }

    post {
        failure { mail to: 'commits@tomee.apache.org', subject: "${env.JOB_NAME} #${env.BUILD_NUMBER} failed", body: env.BUILD_URL }
        unstable { mail to: 'commits@tomee.apache.org', subject: "${env.JOB_NAME} #${env.BUILD_NUMBER} is unstable", body: env.BUILD_URL }
        fixed { mail to: 'commits@tomee.apache.org', subject: "${env.JOB_NAME} #${env.BUILD_NUMBER} is back to normal", body: env.BUILD_URL }
    }
}
