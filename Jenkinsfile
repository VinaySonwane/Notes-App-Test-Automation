pipeline {
    agent any


    tools {
        maven 'Maven'
        jdk 'JDK'
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Execute Parallel Test Suite') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    bat 'mvn clean test -DsuiteXmlFile=testng.xml'
                }
            }
        }
    }
    post {
        always {
            allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]

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
