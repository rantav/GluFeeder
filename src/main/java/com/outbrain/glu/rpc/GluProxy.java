package com.outbrain.glu.rpc;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.outbrain.glu.GluExecutionOrder;
import com.outbrain.glu.GluPlanAction;
import com.outbrain.glu.model.GluModel;
import com.outbrain.glu.model.GluModelDiff;
import com.outbrain.glu.model.GluModelDiffer;
import com.outbrain.glu.model.GluScope;
import com.outbrain.glu.model.MaxMinVersion;

/**
 * Talks to Glu and asks it to do things for it, such as deploying a new model.
 * <p>
 * The easy way to use this class is to call the
 * {@link #deploy(String, GluPlanAction, GluExecutionOrder, String)} method.
 * <p>
 * Internally deploy() invokes loadModel, then createExecutionPlan then executePlan so if you need
 * more control over the process you may call those methods in order.
 * Order is important, you need to first upload a new model, then create an execution plan and then
 * execute it.
 *
 * @author Ran Tavory
 *
 */
public class GluProxy {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(GluProxy.class);

  private final ModelLoader modelLoader;
  private final ExecutionPlanCreatorFactory executionPlanCreatorFactory;
  private final PlanExecutorFactory planExecutorFactory;
  private final ExecutionStatusReaderFactory statusReaderFactory;
  private final GetModelFactory getModelFactory;

  public GluProxy(ModelLoader modelLoader, ExecutionPlanCreatorFactory executionPlanCreatorFactory,
      PlanExecutorFactory planExecutorFactory, ExecutionStatusReaderFactory statusReaderFactory,
      GetModelFactory getModelFactory) {
    Assert.notNull(modelLoader, "modelLoader is null");
    Assert.notNull(executionPlanCreatorFactory, "executionPlanCreatorFactory is null");
    Assert.notNull(planExecutorFactory, "planExecutorFactory is null");
    Assert.notNull(statusReaderFactory, "statusReaderFactory is null");
    Assert.notNull(getModelFactory, "getModelFactory is null");

    this.modelLoader = modelLoader;
    this.executionPlanCreatorFactory = executionPlanCreatorFactory;
    this.planExecutorFactory = planExecutorFactory;
    this.statusReaderFactory = statusReaderFactory;
    this.getModelFactory = getModelFactory;
  }

  public String getUrl() {
    return modelLoader.getGluUrl();
  }


  public GluModelDiff getModelDiff(GluScope scope) throws HttpException, IOException {
    GluModelDiffer differ = new GluModelDiffer();
    GluModel desired = getModelFactory.getService().getModel(ModelType.DESIRED);
    GluModel live = getModelFactory.getService().getModel(ModelType.LIVE);
    return differ.diff(desired, live, scope);
  }

  /**
   * Loads the jsonModel string into glu.
   * <p>
   * To post an update to glu we issue a POST request to
   * http://glu/console/rest/v1/outbrain/system/model The body of the request should
   * contain the json model. See https://github.com/linkedin/glu/wiki/Console
   *
   * @param jsonModel
   * @throws IOException
   * @throws HttpException
   * @return modelId the ID of the newly created model. null if the model was
   *         already up to date
   */
  public String loadModel(String jsonModel) throws HttpException, IOException {
    return modelLoader.loadModel(jsonModel);
  }
  /**
   * Creates an execution plan.
   * <p>
   *
   * @param planAction
   * @param order
   * @param systemFilter
   * @return the ID of the plan. Use this ID to call executePlan.
   */
  public String createExecutionPlan(GluPlanAction planAction, GluExecutionOrder order,
      String systemFilter) throws HttpException, IOException {
    ExecutionPlanCreator planCreator = executionPlanCreatorFactory.getService();
    return planCreator.createExecutionPlan(planAction, order, systemFilter);
  }

  /**
   * Executes a plan on glu.
   * <p>
   * The planId should be retrieved by invoking {@see #loadModel(String)}.
   * <p>
   * The returned executionId can be used to track the execution of the plan.
   *
   * @param executionPlanId
   * @return the executionId. This ID can be used to track execution progress.
   * @throws IOException
   */
  public String executePlan(String executionPlanId) throws IOException {
    PlanExecutor planExecutor = planExecutorFactory.getService();
    return planExecutor.executePlan(executionPlanId);
  }

  public GluProgressStatus getExecutionStatus(String planId, String executionId) throws IOException {
    ExecutionStatusReader reader = statusReaderFactory.getService();
    return reader.checkExecutionStatus(planId, executionId);
  }

  /**
   * Extracts from glu's live model the list of current modules with their corresponding minimum
   * and maximum version that are currently deployed.
   * @return A map from module names to their corresponding min and max versions
   * @throws IOException
   * @throws HttpException
   */
  public List<MaxMinVersion> getLiveVersionsHisto() throws HttpException, IOException {
    GetModel modelGetter = getModelFactory.getService();
    GluModel model = modelGetter.getModel(ModelType.LIVE);
    return model.getMinMaxVersions(modelLoader.getGluUrl());
  }
}
