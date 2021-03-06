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
package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricJobLog;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogDto {

  protected String id;
  protected Date timestamp;

  protected String jobId;
  protected Date jobDueDate;
  protected int jobRetries;
  protected long jobPriority;
  protected String jobExceptionMessage;

  protected String jobDefinitionId;
  protected String jobDefinitionType;
  protected String jobDefinitionConfiguration;

  protected String activityId;
  protected String executionId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String deploymentId;

  protected boolean creationLog;
  protected boolean failureLog;
  protected boolean successLog;
  protected boolean deletionLog;

  public String getId() {
    return id;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getJobId() {
    return jobId;
  }

  public Date getJobDueDate() {
    return jobDueDate;
  }

  public int getJobRetries() {
    return jobRetries;
  }

  public long getJobPriority() {
    return jobPriority;
  }

  public String getJobExceptionMessage() {
    return jobExceptionMessage;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public String getJobDefinitionType() {
    return jobDefinitionType;
  }

  public String getJobDefinitionConfiguration() {
    return jobDefinitionConfiguration;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public boolean isCreationLog() {
    return creationLog;
  }

  public boolean isFailureLog() {
    return failureLog;
  }

  public boolean isSuccessLog() {
    return successLog;
  }

  public boolean isDeletionLog() {
    return deletionLog;
  }

  public static HistoricJobLogDto fromHistoricJobLog(HistoricJobLog historicJobLog) {
    HistoricJobLogDto result = new HistoricJobLogDto();

    result.id = historicJobLog.getId();
    result.timestamp = historicJobLog.getTimestamp();

    result.jobId = historicJobLog.getJobId();
    result.jobDueDate = historicJobLog.getJobDueDate();
    result.jobRetries = historicJobLog.getJobRetries();
    result.jobPriority = historicJobLog.getJobPriority();
    result.jobExceptionMessage = historicJobLog.getJobExceptionMessage();

    result.jobDefinitionId = historicJobLog.getJobDefinitionId();
    result.jobDefinitionType = historicJobLog.getJobDefinitionType();
    result.jobDefinitionConfiguration = historicJobLog.getJobDefinitionConfiguration();

    result.activityId = historicJobLog.getActivityId();
    result.executionId = historicJobLog.getExecutionId();
    result.processInstanceId = historicJobLog.getProcessInstanceId();
    result.processDefinitionId = historicJobLog.getProcessDefinitionId();
    result.processDefinitionKey = historicJobLog.getProcessDefinitionKey();
    result.deploymentId = historicJobLog.getDeploymentId();

    result.creationLog = historicJobLog.isCreationLog();
    result.failureLog = historicJobLog.isFailureLog();
    result.successLog = historicJobLog.isSuccessLog();
    result.deletionLog = historicJobLog.isDeletionLog();

    return result;
  }

}
