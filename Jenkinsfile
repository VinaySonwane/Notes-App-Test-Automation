pipeline {
    agent any

    // Ensure Maven and JDK names perfectly match your Jenkins Global Tool Configuration
    tools {
        maven 'Maven'
        jdk 'JDK'
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Automatically pulls the latest code from your GitHub repository
                checkout scm
            }
        }

        stage('Execute Parallel Test Suite') {
            steps {
                // Execute the TestNG suite using Maven. The 'catchError' ensures the pipeline
                // proceeds to reporting even if tests fail.
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    // Triggers the TestNG suite using Maven
                    bat 'mvn clean test -DsuiteXmlFile=testng.xml'
                }
            }
        }
    }

    post {
        always {
            // 1. Publish the Allure Report
            allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]

            // 2. Archive Artifacts — screenshots are saved by Allure into target/allure-results
            //    as PNG attachments, and also written to target/screenshots by ScreenshotUtils.
            //    Both paths are captured here so neither is missed.
            //    allowEmptyArchive prevents the pipeline from crashing if no screenshots were taken.
            archiveArtifacts artifacts: 'target/cucumber-reports.html, target/allure-results/**/*.png, target/screenshots/**/*.png',
                             allowEmptyArchive: true
        }
        success {
            echo 'SUCCESS: Test Execution Completed! The Notes App is stable.'
        }
        failure {
            echo 'FAILED: Tests broke. Check the Allure Report and Screenshots for details.'
        }
    }
}
