package com.outbrain.glu.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author ran
 *
 */
public interface DeploymentsDao {

  /**
   * Gets a single deployment element by its ID.
   * @param id
   * @return the deployment; null if it doesn't exist.
   */
  Deployment element(Long id);

  /**
   * Permanently deletes the given deployment ID.
   * @param id
   */
  void delete(Long id);

  void save(Deployment d);

  List<Deployment> listDeployments(boolean includeArchived);

  long getNumActive(boolean includeArchived);

  /**
   * @return number of active deployments, not including archinved
   */
  long getNumActive();

  /**
   * Short for d.setArchived(true); save(d);
   * @param d
   */
  void archive(Deployment d);

  void archive(Long deploymentId);

  long getNumTotal(boolean includeArchived);

  /**
   * @return Total number of deployments, not including archived
   */
  long getNumTotal();

  long getNumDone(boolean includeArchived);

  /**
   * @return number of done deployments, not including archived.
   */
  long getNumDone();


  long getNumError(boolean includeArchived);

  /**
   * @return number of errornous deployments, not including archived errors.
   */
  long getNumError();

  List<Deployment> listDeploymentsReverseChronological(boolean includeArchived, long limit);

  /**
   * Cancels (stops) a deployment request.
   *
   * @param id
   */
  void cancel(Long id);

  void clear();

  /**
   * Counts the number successful and failed deployments taking place each day.
   * @param start start date
   * @param end end date.
   * @return A map from Date objects representing Days and their number of failed and successful
   * deployments
   */
  Map<Date, SuccessFailCounts> getDeploymentsHistogramByDate(Date start, Date end);

}
