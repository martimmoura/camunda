/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {screen, waitFor} from 'modules/testing-library';
import {flowNodeSelectionStore} from 'modules/stores/flowNodeSelection';
import {processInstanceDetailsStore} from 'modules/stores/processInstanceDetails';
import {mockFetchFlowNodeMetadata} from 'modules/mocks/api/processInstances/fetchFlowNodeMetaData';
import {mockFetchProcessDefinitionXml} from 'modules/mocks/api/v2/processDefinitions/fetchProcessDefinitionXml';
import {labels, renderPopover} from './mocks';
import {
  calledDecisionMetadata,
  calledFailedDecisionMetadata,
  calledInstanceMetadata,
  calledUnevaluatedDecisionMetadata,
  CALL_ACTIVITY_FLOW_NODE_ID,
  PROCESS_INSTANCE_ID,
  FLOW_NODE_ID,
  USER_TASK_FLOW_NODE_ID,
  BUSINESS_RULE_FLOW_NODE_ID,
} from 'modules/mocks/metadata';
import {metadataDemoProcess} from 'modules/mocks/metadataDemoProcess';
import {
  createInstance,
  createProcessInstance,
  mockCallActivityProcessXML,
} from 'modules/testUtils';
import {init} from 'modules/utils/flowNodeMetadata';
import {selectFlowNode} from 'modules/utils/flowNodeSelection';
import {mockFetchProcessInstance as mockFetchProcessInstanceV2} from 'modules/mocks/api/v2/processInstances/fetchProcessInstance';
import {mockFetchElementInstance} from 'modules/mocks/api/v2/elementInstances/fetchElementInstance';
import {mockSearchElementInstances} from 'modules/mocks/api/v2/elementInstances/searchElementInstances';
import {mockFetchFlownodeInstancesStatistics} from 'modules/mocks/api/v2/flownodeInstances/fetchFlownodeInstancesStatistics';
import {mockFetchProcessInstanceIncidents} from 'modules/mocks/api/processInstances/fetchProcessInstanceIncidents';
import {mockIncidents} from 'modules/mocks/incidents';
import {flowNodeMetaDataStore} from 'modules/stores/flowNodeMetaData';
import {incidentsStore} from 'modules/stores/incidents';
import {mockSearchIncidentsByProcessInstance} from 'modules/mocks/api/v2/incidents/searchIncidentsByProcessInstance';
import {mockSearchDecisionInstances} from 'modules/mocks/api/v2/decisionInstances/searchDecisionInstances';
import {mockSearchJobs} from 'modules/mocks/api/v2/jobs/searchJobs';

import type {
  ElementInstance,
  ProcessInstance,
} from '@camunda/camunda-api-zod-schemas/8.8';
import {mockSearchProcessInstances} from 'modules/mocks/api/v2/processInstances/searchProcessInstances';

const MOCK_EXECUTION_DATE = '21 seconds';

const mockElementInstance: ElementInstance = {
  elementInstanceKey: '2251799813699889',
  elementId: BUSINESS_RULE_FLOW_NODE_ID,
  elementName: 'Business Rule Task',
  type: 'BUSINESS_RULE_TASK',
  state: 'COMPLETED',
  startDate: '2018-06-21T10:00:00.000Z',
  endDate: '2018-06-21T10:00:00.000Z',
  processDefinitionId: 'someKey',
  processInstanceKey: PROCESS_INSTANCE_ID,
  processDefinitionKey: '2',
  hasIncident: false,
  tenantId: '<default>',
};

const mockProcessInstance: ProcessInstance = {
  processInstanceKey: '229843728748927482',
  state: 'ACTIVE',
  startDate: '2018-06-21',
  processDefinitionKey: '2',
  processDefinitionVersion: 1,
  processDefinitionId: 'someKey',
  tenantId: '<default>',
  processDefinitionName: 'Called Process',
  hasIncident: true,
};

const mockIncident = {
  incidentKey: '4503599627375678',
  errorType: 'DECISION_EVALUATION_ERROR' as const,
  errorMessage:
    "Failed to evaluate expression 'paid = false': no variable found for name 'paid'",
  state: 'ACTIVE' as const,
  creationTime: '2022-02-03T16:44:06.981+0000',
  processDefinitionId: 'someKey',
  processDefinitionKey: '2',
  processInstanceKey: PROCESS_INSTANCE_ID,
  elementId: BUSINESS_RULE_FLOW_NODE_ID,
  elementInstanceKey: '2251799813699889',
  jobKey: '',
  tenantId: '<default>',
};

const mockDecisionInstance = {
  decisionEvaluationInstanceKey: '750893257230984',
  decisionEvaluationKey: '750893257230984',
  decisionDefinitionName: 'Take decision',
  decisionDefinitionId: 'decision-1',
  decisionDefinitionKey: '123',
  decisionDefinitionVersion: 1,
  decisionDefinitionType: 'DECISION_TABLE' as const,
  processDefinitionKey: '2',
  processInstanceKey: PROCESS_INSTANCE_ID,
  elementInstanceKey: '2251799813699889',
  state: 'EVALUATED' as const,
  evaluationDate: '2018-12-12T22:00:00.000+0000',
  evaluationFailure: '',
  tenantId: '<default>',
  result: '',
};

vi.mock('date-fns', async () => {
  const actual = await vi.importActual('date-fns');
  return {
    ...actual,
    formatDistanceToNowStrict: () => MOCK_EXECUTION_DATE,
  };
});

describe('MetadataPopover', () => {
  beforeEach(() => {
    init('process-instance', []);
    flowNodeSelectionStore.init();
    mockFetchProcessDefinitionXml().withSuccess(metadataDemoProcess);
    mockSearchIncidentsByProcessInstance(PROCESS_INSTANCE_ID).withSuccess({
      items: [],
      page: {totalItems: 0},
    });
    mockFetchElementInstance('2251799813699889').withSuccess(
      mockElementInstance,
    );
    mockSearchElementInstances().withSuccess({
      items: [mockElementInstance],
      page: {totalItems: 1},
    });
    mockFetchFlownodeInstancesStatistics().withSuccess({
      items: [
        {
          elementId: FLOW_NODE_ID,
          active: 1,
          completed: 0,
          canceled: 0,
          incidents: 0,
        },
        {
          elementId: CALL_ACTIVITY_FLOW_NODE_ID,
          active: 1,
          completed: 0,
          canceled: 0,
          incidents: 0,
        },
        {
          elementId: USER_TASK_FLOW_NODE_ID,
          active: 1,
          completed: 0,
          canceled: 0,
          incidents: 0,
        },
      ],
    });
    mockFetchProcessInstanceIncidents().withSuccess(mockIncidents);

    mockSearchJobs().withSuccess({
      items: [],
      page: {
        totalItems: 0,
      },
    });

    mockSearchProcessInstances().withSuccess({
      items: [],
      page: {totalItems: 0},
    });

    mockSearchIncidentsByProcessInstance(PROCESS_INSTANCE_ID).withSuccess({
      items: [],
      page: {totalItems: 0},
    });

    mockSearchDecisionInstances().withSuccess({
      items: [],
      page: {totalItems: 0},
    });
  });

  afterEach(() => {
    processInstanceDetailsStore.reset();
    flowNodeSelectionStore.reset();
    flowNodeMetaDataStore.reset();
    incidentsStore.reset();
  });

  it('should render meta data for completed flow node', async () => {
    vi.useFakeTimers({shouldAdvanceTime: true});

    mockFetchProcessDefinitionXml().withSuccess(mockCallActivityProcessXML);
    mockFetchFlowNodeMetadata().withSuccess(calledInstanceMetadata);
    mockFetchProcessInstanceV2().withSuccess(
      createProcessInstance({
        processInstanceKey: PROCESS_INSTANCE_ID,
      }),
    );

    const mockCallActivityElementInstance: ElementInstance = {
      ...mockElementInstance,
      elementId: CALL_ACTIVITY_FLOW_NODE_ID,
      elementName: 'Call Activity',
      type: 'CALL_ACTIVITY',
    };

    mockFetchElementInstance('2251799813699889').withSuccess(
      mockCallActivityElementInstance,
    );

    mockSearchElementInstances().withSuccess({
      items: [mockCallActivityElementInstance],
      page: {totalItems: 1},
    });

    mockSearchProcessInstances().withSuccess({
      items: [mockProcessInstance],
      page: {totalItems: 1},
    });

    processInstanceDetailsStore.setProcessInstance(
      createInstance({
        id: PROCESS_INSTANCE_ID,
        state: 'ACTIVE',
      }),
    );
    selectFlowNode({}, {flowNodeId: CALL_ACTIVITY_FLOW_NODE_ID});

    renderPopover();

    expect(
      await screen.findByText(labels.elementInstanceKey),
    ).toBeInTheDocument();
    expect(screen.getByText(labels.executionDuration)).toBeInTheDocument();
    expect(
      await screen.findByText(labels.calledProcessInstance),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('button', {name: labels.showMoreMetadata}),
    ).toBeInTheDocument();

    expect(
      screen.getByText(
        calledInstanceMetadata.instanceMetadata!.flowNodeInstanceId,
      ),
    ).toBeInTheDocument();
    expect(screen.getByText('Less than 1 second')).toBeInTheDocument();
    expect(screen.getByTestId('called-process-instance')).toHaveTextContent(
      `Called Process - ${
        calledInstanceMetadata.instanceMetadata!.calledProcessInstanceId
      }`,
    );

    vi.clearAllTimers();
    vi.useFakeTimers();
  });

  it('should render completed decision', async () => {
    vi.useFakeTimers({shouldAdvanceTime: true});

    mockFetchProcessDefinitionXml().withSuccess(metadataDemoProcess);
    mockFetchFlowNodeMetadata().withSuccess(calledDecisionMetadata);
    mockFetchProcessInstanceV2().withSuccess(
      createProcessInstance({
        processInstanceKey: PROCESS_INSTANCE_ID,
      }),
    );

    mockSearchDecisionInstances().withSuccess({
      items: [mockDecisionInstance],
      page: {totalItems: 1},
    });

    processInstanceDetailsStore.setProcessInstance(
      createInstance({
        id: PROCESS_INSTANCE_ID,
        state: 'COMPLETED',
      }),
    );

    selectFlowNode(
      {},
      {
        flowNodeId: BUSINESS_RULE_FLOW_NODE_ID,
        flowNodeInstanceId: '2251799813699889',
      },
    );

    const {user} = renderPopover();

    console.log('mockDecisionInstance', mockDecisionInstance);

    expect(
      await screen.findByText(labels.calledDecisionInstance),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole('heading', {name: labels.incident}),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText(labels.rootCauseDecisionInstance),
    ).not.toBeInTheDocument();

    await user.click(
      screen.getByText(
        `${mockDecisionInstance!.decisionDefinitionName} - ${
          mockDecisionInstance!.decisionEvaluationKey
        }`,
      ),
    );

    await waitFor(() =>
      expect(screen.getByTestId('pathname')).toHaveTextContent(
        `/decisions/${mockDecisionInstance!.decisionEvaluationKey}`,
      ),
    );

    vi.clearAllTimers();
    vi.useFakeTimers();
  });

  it('should render failed decision', async () => {
    vi.useFakeTimers({shouldAdvanceTime: true});

    const mockBusinessRuleElementInstance: ElementInstance = {
      ...mockElementInstance,
      elementId: BUSINESS_RULE_FLOW_NODE_ID,
      type: 'BUSINESS_RULE_TASK',
      hasIncident: true,
    };

    const mockFailedDecisionInstance = {
      ...mockDecisionInstance,
      state: 'FAILED' as const,
    };

    mockFetchProcessDefinitionXml().withSuccess(metadataDemoProcess);
    mockFetchFlowNodeMetadata().withSuccess(calledFailedDecisionMetadata);
    mockFetchProcessInstanceV2().withSuccess(
      createProcessInstance({processInstanceKey: PROCESS_INSTANCE_ID}),
    );
    mockFetchElementInstance('2251799813699889').withSuccess(
      mockBusinessRuleElementInstance,
    );

    mockSearchElementInstances().withSuccess({
      items: [mockBusinessRuleElementInstance],
      page: {totalItems: 1},
    });

    mockSearchIncidentsByProcessInstance(PROCESS_INSTANCE_ID).withSuccess({
      items: [mockIncident],
      page: {totalItems: 1},
    });

    mockSearchDecisionInstances().withSuccess({
      items: [mockFailedDecisionInstance],
      page: {totalItems: 1},
    });

    processInstanceDetailsStore.setProcessInstance(
      createInstance({
        id: PROCESS_INSTANCE_ID,
        state: 'INCIDENT',
      }),
    );

    selectFlowNode(
      {},
      {
        flowNodeId: BUSINESS_RULE_FLOW_NODE_ID,
        flowNodeInstanceId: '2251799813699889',
      },
    );

    const {user} = renderPopover();

    expect(
      await screen.findByText(labels.calledDecisionInstance),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('heading', {name: labels.incident}),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('link', {
        name: `View ${mockFailedDecisionInstance.decisionDefinitionName} instance ${mockFailedDecisionInstance!.decisionEvaluationKey}`,
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByText(labels.rootCauseDecisionInstance),
    ).toBeInTheDocument();
    expect(
      screen.queryByText(labels.rootCauseProcessInstance),
    ).not.toBeInTheDocument();
    expect(
      screen.getByRole('link', {
        name: `View root cause decision ${mockFailedDecisionInstance!.decisionDefinitionName!} - ${
          mockFailedDecisionInstance!.decisionEvaluationKey
        }`,
      }),
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole('link', {
        name: `View root cause decision ${mockFailedDecisionInstance!.decisionDefinitionName!} - ${
          mockFailedDecisionInstance!.decisionEvaluationKey
        }`,
      }),
    );
    await waitFor(() =>
      expect(screen.getByTestId('pathname')).toHaveTextContent(
        `/decisions/${mockFailedDecisionInstance!.decisionEvaluationKey}`,
      ),
    );

    vi.clearAllTimers();
    vi.useRealTimers();
  });

  it('should render unevaluated decision', async () => {
    mockFetchProcessDefinitionXml().withSuccess(metadataDemoProcess);
    mockFetchFlowNodeMetadata().withSuccess(calledUnevaluatedDecisionMetadata);
    mockFetchProcessInstanceV2().withSuccess(
      createProcessInstance({
        processInstanceKey: PROCESS_INSTANCE_ID,
      }),
    );

    const mockUnevaluatedDecisionInstance = {
      ...mockDecisionInstance,
      decisionEvaluationInstanceKey: '',
      decisionEvaluationKey: '',
      state: 'UNSPECIFIED' as const,
    };

    mockSearchDecisionInstances().withSuccess({
      items: [mockUnevaluatedDecisionInstance],
      page: {totalItems: 1},
    });

    processInstanceDetailsStore.setProcessInstance(
      createInstance({
        id: PROCESS_INSTANCE_ID,
        state: 'ACTIVE',
      }),
    );

    selectFlowNode(
      {},
      {
        flowNodeId: BUSINESS_RULE_FLOW_NODE_ID,
        flowNodeInstanceId: '2251799813699889',
      },
    );

    renderPopover();

    expect(
      await screen.findByText(labels.calledDecisionInstance),
    ).toBeInTheDocument();
    expect(
      screen.getByText(mockUnevaluatedDecisionInstance.decisionDefinitionName),
    ).toBeInTheDocument();
    expect(screen.queryByText(labels.incident)).not.toBeInTheDocument();
    expect(
      screen.queryByText(labels.rootCauseDecisionInstance),
    ).not.toBeInTheDocument();
  });
});
