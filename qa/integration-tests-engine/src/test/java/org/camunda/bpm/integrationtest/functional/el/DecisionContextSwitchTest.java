/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.integrationtest.functional.el;

import java.util.List;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.integrationtest.functional.el.beans.GreeterBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class DecisionContextSwitchTest extends AbstractFoxPlatformIntegrationTest {

  protected static final String DMN_RESOURCE_NAME = "org/camunda/bpm/integrationtest/functional/el/BeanResolvingDecision.dmn11.xml";

  @Deployment(name="bpmnDeployment")
  public static WebArchive createBpmnDeplyoment() {
    return initWebArchiveDeployment("bpmn-deployment.war")
      .addAsResource("org/camunda/bpm/integrationtest/functional/el/BusinessRuleProcess.bpmn20.xml");
  }

  @Deployment(name="dmnDeployment")
  public static WebArchive createDmnDeplyoment() {
    return initWebArchiveDeployment("dmn-deployment.war")
      .addClass(GreeterBean.class)
      .addAsResource(DMN_RESOURCE_NAME);
  }


  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "client.war")
            .addClass(AbstractFoxPlatformIntegrationTest.class);

    TestContainer.addContainerSpecificResources(webArchive);

    return webArchive;
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void shouldSwitchContextWhenUsingDecisionService() {
    decisionService.evaluateDecisionByKey("decision", Variables.createVariables());
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void shouldSwitchContextWhenCallingFromBpmn() {
    runtimeService.startProcessInstanceByKey("testProcess");
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void shouldSwitchContextWhenUsingDecisionServiceAfterRedeployment() {

    // given
    List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery()
        .list();

    // find dmn deployment
    org.camunda.bpm.engine.repository.Deployment dmnDeployment = null;
    for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
      List<String> resourceNames = repositoryService.getDeploymentResourceNames(deployment.getId());
      if(resourceNames.contains(DMN_RESOURCE_NAME)) {
        dmnDeployment = deployment;
      }
    }

    if(dmnDeployment == null) {
      Assert.fail("Expected to find DMN deployment");
    }

    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService
      .createDeployment()
      .nameFromDeployment(dmnDeployment.getId())
      .addDeploymentResources(dmnDeployment.getId())
      .deploy();

    try {
      // when then
      decisionService.evaluateDecisionByKey("decision", Variables.createVariables());
    }
    finally {
      repositoryService.deleteDeployment(deployment2.getId(), true);
    }

  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void shouldSwitchContextWhenCallingFromBpmnAfterRedeployment() {
    // given
    List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery()
        .list();

    // find dmn deployment
    org.camunda.bpm.engine.repository.Deployment dmnDeployment = null;
    for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
      List<String> resourceNames = repositoryService.getDeploymentResourceNames(deployment.getId());
      if(resourceNames.contains(DMN_RESOURCE_NAME)) {
        dmnDeployment = deployment;
      }
    }

    if(dmnDeployment == null) {
      Assert.fail("Expected to find DMN deployment");
    }

    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService
      .createDeployment()
      .nameFromDeployment(dmnDeployment.getId())
      .addDeploymentResources(dmnDeployment.getId())
      .deploy();

    try {
      // when then
      runtimeService.startProcessInstanceByKey("testProcess");
    }
    finally {
      repositoryService.deleteDeployment(deployment2.getId(), true);
    }
  }

}