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

package org.camunda.bpm.engine.impl.history.producer;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableInput;
import org.camunda.bpm.dmn.engine.DmnDecisionTableOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class DefaultDmnHistoryEventProducer implements DmnHistoryEventProducer {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  @Override
  public HistoryEvent createDecisionEvaluatedEvt(DelegateExecution execution, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricDecisionInstanceEntity event = newDecisionInstanceEventEntity(executionEntity, decisionTable, decisionTableResult);
    // initialize event
    initDecisionInstanceEvent(event, decisionTable, decisionTableResult, HistoryEventTypes.DMN_DECISION_EVALUATE);
    setReferenceToProcessInstance(event, executionEntity);
    // set current time as evaluation time
    event.setEvaluationTime(ClockUtil.getCurrentTime());

    return event;
  }

  @Override
  public HistoryEvent createDecisionEvaluatedEvt(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    // create event instance
    HistoricDecisionInstanceEntity event = newDecisionInstanceEventEntity(decisionTable, decisionTableResult);
    // initialize event
    initDecisionInstanceEvent(event, decisionTable, decisionTableResult, HistoryEventTypes.DMN_DECISION_EVALUATE);
    // set current time as evaluation time
    event.setEvaluationTime(ClockUtil.getCurrentTime());

    return event;
  }

  protected HistoricDecisionInstanceEntity newDecisionInstanceEventEntity(ExecutionEntity executionEntity, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    return new HistoricDecisionInstanceEntity();
  }

  protected HistoricDecisionInstanceEntity newDecisionInstanceEventEntity(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    return new HistoricDecisionInstanceEntity();
  }

  protected void initDecisionInstanceEvent(HistoricDecisionInstanceEntity event, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult, HistoryEventTypes eventType) {
    event.setEventType(eventType.getEventName());

    event.setDecisionDefinitionId(((DecisionDefinition) decisionTable).getId());
    event.setDecisionDefinitionKey(decisionTable.getKey());
    event.setDecisionDefinitionName(decisionTable.getName());

    if(decisionTableResult.getCollectResultValue() != null) {
      Double collectResultValue = getCollectResultValue(decisionTableResult.getCollectResultValue());
      event.setCollectResultValue(collectResultValue);
    }

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = createHistoricDecisionInputInstances(decisionTableResult);
    event.setInputs(historicDecisionInputInstances);

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = createHistoricDecisionOutputInstances(decisionTableResult);
    event.setOutputs(historicDecisionOutputInstances);
  }

  protected Double getCollectResultValue(TypedValue collectResultValue) {
    // the built-in collect aggregators return only numbers

    if(collectResultValue instanceof IntegerValue) {
      return ((IntegerValue) collectResultValue).getValue().doubleValue();

    } else if(collectResultValue instanceof LongValue) {
      return ((LongValue) collectResultValue).getValue().doubleValue();

    } else if(collectResultValue instanceof DoubleValue) {
      return ((DoubleValue) collectResultValue).getValue().doubleValue();

    } else {
      throw LOG.collectResultValueOfUnsupportedTypeException(collectResultValue);
    }
  }

  protected List<HistoricDecisionInputInstance> createHistoricDecisionInputInstances(DmnDecisionTableResult decisionTableResult) {
    List<HistoricDecisionInputInstance> inputInstances = new ArrayList<HistoricDecisionInputInstance>();

    for(DmnDecisionTableInput inputClause : decisionTableResult.getInputs().values()) {

      HistoricDecisionInputInstanceEntity inputInstance = new HistoricDecisionInputInstanceEntity();
      inputInstance.setClauseId(inputClause.getKey());
      inputInstance.setClauseName(inputClause.getName());

      TypedValue typedValue = Variables.untypedValue(inputClause.getValue());
      inputInstance.setValue(typedValue);

      inputInstances.add(inputInstance);
    }

    return inputInstances;
  }

  protected List<HistoricDecisionOutputInstance> createHistoricDecisionOutputInstances(DmnDecisionTableResult decisionTableResult) {
    List<HistoricDecisionOutputInstance> outputInstances = new ArrayList<HistoricDecisionOutputInstance>();

    List<DmnDecisionTableRule> matchingRules = decisionTableResult.getMatchingRules();
    for(int index = 0; index < matchingRules.size(); index++) {
      DmnDecisionTableRule rule = matchingRules.get(index);

      String ruleId = rule.getKey();
      Integer ruleOrder = index + 1;

      for(DmnDecisionTableOutput outputClause : rule.getOutputs().values()) {

        HistoricDecisionOutputInstanceEntity outputInstance = new HistoricDecisionOutputInstanceEntity();
        outputInstance.setClauseId(outputClause.getKey());
        outputInstance.setClauseName(outputClause.getName());

        outputInstance.setRuleId(ruleId);
        outputInstance.setRuleOrder(ruleOrder);

        outputInstance.setVariableName(outputClause.getOutputName());

        TypedValue typedValue = Variables.untypedValue(outputClause.getValue());
        outputInstance.setValue(typedValue);

        outputInstances.add(outputInstance);
      }
    }

    return outputInstances;
  }

  protected void setReferenceToProcessInstance(HistoricDecisionInstanceEntity event, ExecutionEntity execution) {
    event.setProcessDefinitionKey(getProcessDefinitionKey(execution));
    event.setProcessDefinitionId(execution.getProcessDefinitionId());

    event.setProcessInstanceId(execution.getProcessInstanceId());

    event.setActivityId(execution.getActivityId());
    event.setActivityInstanceId(execution.getActivityInstanceId());
  }

  protected String getProcessDefinitionKey(ExecutionEntity execution) {
    ProcessDefinitionEntity definition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    if (definition != null) {
      return definition.getKey();
    } else {
      return null;
    }
  }

}
