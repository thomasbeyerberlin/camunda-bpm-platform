/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.dmn.businessruletask;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Tests the mapping of the decision result.
 *
 * @author Philipp Ossler
 */
public class DmnDecisionResultMappingTest extends PluggableProcessEngineTestCase {

  protected static final String TEST_DECISION = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultTest.dmn11.xml";

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultCustomOutputMapping.bpmn20.xml", TEST_DECISION })
  public void testCustomOutputMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries");

    assertEquals("foo", runtimeService.getVariable(processInstance.getId(), "result1"));
    assertEquals(Variables.stringValue("foo"), runtimeService.getVariableTyped(processInstance.getId(), "result1"));

    assertEquals("bar", runtimeService.getVariable(processInstance.getId(), "result2"));
    assertEquals(Variables.stringValue("bar"), runtimeService.getVariableTyped(processInstance.getId(), "result2"));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultSingleValueTest.bpmn20.xml", TEST_DECISION})
  public void testSingleValueMapping() {
    ProcessInstance processInstance = startTestProcess("single entry");

    assertEquals("foo", runtimeService.getVariable(processInstance.getId(), "result"));
    assertEquals(Variables.stringValue("foo"), runtimeService.getVariableTyped(processInstance.getId(), "result"));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultSingleOutputTest.bpmn20.xml", TEST_DECISION })
  public void testSingleOutputMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries");

    @SuppressWarnings("unchecked")
    Map<String, Object> output = (Map<String, Object>) runtimeService.getVariable(processInstance.getId(), "result");

    assertEquals(2, output.size());
    assertEquals("foo", output.get("result1"));
    assertEquals("bar", output.get("result2"));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultCollectValuesTest.bpmn20.xml", TEST_DECISION })
  public void testCollectValuesMapping() {
    ProcessInstance processInstance = startTestProcess("single entry list");

    @SuppressWarnings("unchecked")
    List<String> output = (List<String>) runtimeService.getVariable(processInstance.getId(), "result");

    assertEquals(2, output.size());
    assertEquals("foo", output.get(0));
    assertEquals("foo", output.get(1));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultOutputListTest.bpmn20.xml", TEST_DECISION })
  public void testOutputListMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries list");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> outputList = (List<Map<String, Object>>) runtimeService.getVariable(processInstance.getId(), "result");
    assertEquals(2, outputList.size());

    for (Map<String, Object> valueMap : outputList) {
      assertEquals(2, valueMap.size());
      assertEquals("foo", valueMap.get("result1"));
      assertEquals("bar", valueMap.get("result2"));
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultDefaultMappingTest.bpmn20.xml", TEST_DECISION })
  public void testDefaultResultMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries list");

    // default mapping is 'outputList'
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> outputList = (List<Map<String, Object>>) runtimeService.getVariable(processInstance.getId(), "result");
    assertEquals(2, outputList.size());

    for (Map<String, Object> valueMap : outputList) {
      assertEquals(2, valueMap.size());
      assertEquals("foo", valueMap.get("result1"));
      assertEquals("bar", valueMap.get("result2"));
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultSingleValueTest.bpmn20.xml", TEST_DECISION })
  public void testSingleValueMappingFailureMultipleOutputs() {
    try {
      startTestProcess("single entry list");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("The decision result mapper failed to process", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultSingleValueTest.bpmn20.xml", TEST_DECISION })
  public void testSingleValueMappingFailureMultipleValues() {
    try {
      startTestProcess("multiple entries");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("The decision result mapper failed to process", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultSingleOutputTest.bpmn20.xml", TEST_DECISION })
  public void testSingleOutputMappingFailure() {
    try {
      startTestProcess("single entry list");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("The decision result mapper failed to process", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultCollectValuesTest.bpmn20.xml", TEST_DECISION })
  public void testCollectValuesMappingFailure() {
    try {
      startTestProcess("multiple entries");

      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("The decision result mapper failed to process", e.getMessage());
    }
  }

  public void testInvalidMapping() {
    try {
      deploymentId = repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultInvalidMappingTest.bpmn20.xml")
          .deploy()
          .getId();

      fail("expect parse exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("No decision result mapper found for name 'invalid'", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultTest.bpmn20.xml", TEST_DECISION })
  public void testTransientDecisionResult() {
    // when a decision is evaluated and the result is stored in a transient variable "decisionResult"
    ProcessInstance processInstance = startTestProcess("single entry");

    // then the variable should not be available outside the business rule task
    assertNull(runtimeService.getVariable(processInstance.getId(), "decisionResult"));
    // and should not create an entry in history since it is not persistent
    assertNull(historyService.createHistoricVariableInstanceQuery().variableName("decisionResult").singleResult());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultOverrideVariableTest.bpmn20.xml", TEST_DECISION })
  public void testFailedToOverrideDecisionResultVariable() {
    try {
      // the transient variable "decisionResult" should not be overridden by the task result variable
      startTestProcess("single entry");
      fail("expect exception");

    } catch (ProcessEngineException e) {
      assertTextPresent("variable with name 'decisionResult' can not be updated", e.getMessage());
    }
  }

  protected ProcessInstance startTestProcess(String input) {
    return runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("input", input));
  }

}
