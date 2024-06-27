import org.folio.eureka.EurekaImage
import org.jenkinsci.plugins.workflow.libs.Library

@Library('pipelines-shared-library') _

node('jenkins-agent-java17-bigmem') {
  stage('Build Docker Image') {
    dir('mod-login-keycloak') {
      EurekaImage image = new EurekaImage(this)
      image.setModuleName('mod-login-keycloak')
      image.makeImage()3
    }
  }
}

buildMvn {
  publishModDescriptor = false
  mvnDeploy = true
  buildNode = 'jenkins-agent-java17-bigmem'
}
