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

package org.camunda.bpm.engine.impl.dmn.result;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.BpmnBehaviorLogger;

/**
 * Maps the decision result to a list of untyped values.
 *
 * @author Philipp Ossler
 */
public class CollectValuesDecisionResultMapper implements DecisionResultMapper {

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  @Override
  public Object mapDecisionResult(DmnDecisionResult decisionResult) {
    if (decisionResult.isEmpty()) {
      return Collections.emptyList();

    } else {

      Set<String> outputNames = collectOutputNames(decisionResult);
      if (outputNames.size() > 1) {
        throw LOG.decisionResultCollectMappingException(outputNames, decisionResult);

      } else {
        String outputName = outputNames.iterator().next();
        return decisionResult.collectOutputValues(outputName);
      }
    }
  }

  protected Set<String> collectOutputNames(DmnDecisionResult decisionResult) {
    Set<String> outputNames = new HashSet<String>();

    for (Map<String, Object> valueMap : decisionResult.getOutputList()) {
      outputNames.addAll(valueMap.keySet());
    }

    return outputNames;
  }

}
