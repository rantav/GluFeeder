package com.outbrain.glu.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.outbrain.glu.GluExecutionOrder;
import com.outbrain.glu.model.GluScope;
import com.outbrain.glu.rpc.GluProgressStatus;

/**
 * Reports the deployment status and holds the context of a single deployment request.
 *
 * @author Ran Tavory
 *
 */
@Entity
@Table(name="Deployments")
public class Deployment implements Comparable<Deployment> {

  private Long id;
  private DeploymentStatus status = DeploymentStatus.NOT_STARTED;
  private Set<Module> modules;
  private Set<Tag> tags;
  private long modulesRevision;
  private String gluSystemId;
  private long jsonScmRevision;
  private Date startTime;
  private Date endTime;
  private Date lastTouched;
  private Exception error;
  private List<DeploymentPhase> phases;
  private Set<String> yumRepos;
  /** The model updates with this deployment */
  private String jsonModel;
  /** Only the diff of the model bw this and the former state */
  private String jsonDiff;
  /** descriptive text of the source of the deployment, e.g. UI, teamc etc */
  private String source;
  private Committer committer;
  private long commitRevision;
  private String commitLogMessage;
  private boolean prepareOnly;
  private GluScope scope;
  /** archived items are hidden from the usual UI */
  private boolean archived;
  private Set<Cluster> clusters;

  public Deployment(){
    scope = new GluScope();
  };

  public Deployment(Set<Module> modules,
                    long modulesRevision,
                    Set<Tag> tags,
                    Committer committer,
                    long commitRevision,
                    String commitLogMessage) {
    setModules(modules);
    this.modulesRevision = modulesRevision;
    setTags(tags);
    setCommitter(committer);
    this.commitLogMessage = commitLogMessage;
    this.commitRevision = commitRevision;
    startTime = new Date();
    phases = new ArrayList<DeploymentPhase>();
    scope = new GluScope(modules, commitRevision, tags);
  }

  public String getGluSystemId() {
    return gluSystemId;
  }
  public void setGluSystemId(String systemId) {
    this.gluSystemId = systemId;
  }

  public long getJsonScmRevision() {
    return jsonScmRevision;
  }
  public void setJsonScmRevision(long scmRevision) {
    this.jsonScmRevision = scmRevision;
  }

  public Date getStartTime() {
    return startTime;
  }
  public Date getEndTime() {
    return endTime;
  }

  public void setError(Exception e) {
    if (e != null) {
      error = e;
      endWithStatus(DeploymentStatus.ERROR);
    }
  }
  public Exception getError() {
    return error;
  }


  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "DeploymentModules",
    joinColumns = {@JoinColumn(name="deploymentId")},
    inverseJoinColumns = {@JoinColumn(name="moduleId")})
  public Set<Module> getModules() {
    return modules;
  }
  public void setModules(Set<Module> modules) {
    modules = modules != null ? modules : new HashSet<Module>();
    if (this.modules == modules) {
      return;
    }
    this.modules = modules;
    for (Module module : modules) {
      module.addDeployment(this);
    }
    scope = new GluScope(modules, commitRevision, tags);
  }
  @Transient
  public String getModulesAsString() {
    return StringUtils.join(getModules(), ',');
  }

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "DeploymentClusters",
    joinColumns = {@JoinColumn(name="deploymentId")},
    inverseJoinColumns = {@JoinColumn(name="clusterId")})
  public Set<Cluster> getClusters() {
    return clusters;
  }
  public void setClusters(Set<Cluster> clusters) {
    clusters = clusters != null ? clusters : new HashSet<Cluster>();
    if (this.clusters == clusters) {
      return;
    }
    this.clusters = clusters;
    for (Cluster cluster : clusters) {
      cluster.addDeployment(this);
    }
  }


  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "DeploymentTags",
    joinColumns = {@JoinColumn(name="deploymentId")},
    inverseJoinColumns = {@JoinColumn(name="tagId")})
  public Set<Tag> getTags() {
    return tags;
  }
  public void setTags(Set<Tag> tags) {
    tags = tags != null ? tags : new HashSet<Tag>();
    if (this.tags == tags) {
      return;
    }
    this.tags = tags;
    for (Tag tag : tags) {
      tag.addDeployment(this);
    }
    scope = new GluScope(modules, commitRevision, tags);
  }

  public long getModulesRevision() {
    return modulesRevision;
  }

  @OneToMany(cascade = CascadeType.ALL, mappedBy="deployment", fetch = FetchType.EAGER)
  public List<DeploymentPhase> getDeploymentPhases() {
    return phases;
  }

  public void setDeploymentPhases(List<DeploymentPhase> ps) {
    phases = ps;
  }

  public void addDeploymentPhase(String gluSystemFilter, GluExecutionOrder order) {
    DeploymentPhase phase = new DeploymentPhase(gluSystemFilter, order);
    phase.setDeployment(this);
    phases.add(phase);
  }

  @Transient
  public Set<String> getYumRepos() {
    return yumRepos;
  }
  public void setYumRepos(Set<String> yumRepos) {
    this.yumRepos = yumRepos;
  }
  public String getYumReposString() {
    return StringUtils.join(yumRepos, ',');
  }
  public void setYumReposString(String yumReposString) {
    yumRepos = yumReposString == null ? new HashSet<String>() :
      new HashSet<String>(Arrays.asList(yumReposString.split(",")));
  }

  @Transient
  public boolean isPendingYum() {
    return status == DeploymentStatus.PENDING_YUM;
  }

  public Date getLastTouched() {
    return lastTouched;
  }

  /**
   * Update the last touch date to now.
   */
  public void touch() {
    lastTouched = new Date();
  }

  public String getJsonModel() {
    return jsonModel;
  }
  public void setJsonModel(String jsonModel) {
    this.jsonModel = jsonModel;
  }

  public void timeout() {
    endWithStatus(DeploymentStatus.ERROR_TIMEOUT);
  }

  @Transient
  public boolean isTimedout() {
    return status == DeploymentStatus.ERROR_TIMEOUT;
  }

  @Enumerated(EnumType.STRING)
  public DeploymentStatus getStatus() {
    return status;
  }

  public void setStatus(DeploymentStatus status) {
    this.status = status;
    touch();
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  @Id
  @GeneratedValue
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Nukes it. Cancels.
   */
  public void nuke() {
    endWithStatus(DeploymentStatus.CANCELED);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).append(gluSystemId).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Deployment)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    Deployment other = (Deployment) obj;
    return new EqualsBuilder().append(id, other.id).
        append(gluSystemId, other.gluSystemId).
        isEquals();
  }

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "committer_fk")
  public Committer getCommitter() {
    return committer;
  }
  public void setCommitter(Committer committer) {
    if (this.committer == committer) {
      return;
    }
    if (this.committer != null) {
      this.committer.removeDeployment(this);
    }
    this.committer = committer;
    if (committer != null) {
      committer.addDeployment(this);
    }
  }

  public long getCommitRevision() {
    return commitRevision;
  }

  public String getCommitLogMessage() {
    return commitLogMessage;
  }

  public String getJsonDiff() {
    return jsonDiff;
  }

  public void setJsonDiff(String jsonDiff) {
    this.jsonDiff = jsonDiff;
  }

  public boolean isPrepareOnly() {
    return prepareOnly;
  }

  public void setPrepareOnly(boolean prepareOnly) {
    this.prepareOnly = prepareOnly;
  }

  @Transient
  public GluScope getScope() {
    return scope;
  }

  public void setCurrentPhaseProgress(GluProgressStatus progress) {
    getCurrentPhase().setProgress(progress);
  }

  public void setModulesRevision(long modulesRevision) {
    this.modulesRevision = modulesRevision;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public void setLastTouched(Date lastTouched) {
    this.lastTouched = lastTouched;
  }

  public void setCommitRevision(long commitRevision) {
    this.commitRevision = commitRevision;
    scope = new GluScope(modules, commitRevision, tags);
  }

  public void setCommitLogMessage(String commitLogMessage) {
    this.commitLogMessage = commitLogMessage;
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  @Transient
  public boolean isActive() {
    return endTime == null;
  }

  @Transient
  public boolean hasErrors() {
    return error != null;
  }

  private void end() {
    endTime = new Date();
  }

  public void endWithStatus(DeploymentStatus s) {
    end();
    status = s;
  }

  public void setCurrentPhaseGluExecutionId(String executionId) {
    getCurrentPhase().setExecutionId(executionId);
  }

  public void setCurrentPhaseGluPlanId(String planId) {
    getCurrentPhase().setPlanId(planId);
  }

  @Transient
  private DeploymentPhase getCurrentPhase() {
    return phases.isEmpty() ? null : phases.get(phases.size() - 1);
  }

  @Transient
  public String getCurrentPhaseGluPlanId() {
    return phases.isEmpty() ? null : getCurrentPhase().getPlanId();
  }
  public void setCurrentPhasePlanId(String planId) {
    getCurrentPhase().setPlanId(planId);
  }

  @Transient
  public String getCurrentPhaseGluExecutionId() {
    return phases.isEmpty() ? null : getCurrentPhase().getExecutionId();
  }
  @Transient
  public String getCurrentPhaseGluSystemFilter() {
    return phases.isEmpty() ? null : getCurrentPhase().getSystemFilter();
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(id).
    append(status).
    append(modules).
    append(tags).
    append(modulesRevision).
    append(gluSystemId).
    append(jsonScmRevision).
    append(startTime).
    append(endTime).
    append(lastTouched).
    append(error).
    append(phases).
    append(jsonDiff).
    append(source).
    append(committer).
    append(commitRevision).
    append(commitLogMessage).
    append(prepareOnly).
    append(scope).
    append(clusters).
    toString();
  }

  @Transient
  public boolean isCanceled() {
    return getStatus().equals(DeploymentStatus.CANCELED);
  }

  @Transient
  public boolean isTestOnly() {
    Set<Cluster> clusters = getClusters();
    if (clusters != null) {
      for (Cluster cluster : clusters) {
        if (cluster.isProduction()) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int compareTo(Deployment d) {
    if (d.getId() == null && getId() == null) {
      return 0;
    }
    if (d.getId() == null) {
      return -1;
    }
    if (getId() == null) {
      return 1;
    }
    return (int) (d.getId() - getId());
  }
}
