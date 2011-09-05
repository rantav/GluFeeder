package com.outbrain.glu.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.outbrain.glu.data.Cluster;
import com.outbrain.glu.data.Deployment;
import com.outbrain.glu.data.Module;
import com.outbrain.glu.data.Tag;

/**
 * Data used to draw the graph of the deployments annotated timeline
 *
 * @author Ran Tavory
 *
 */
public class AnnotatedTimelineData {
/*
 In javascript we need this data (example):

      data.addColumn('number', 'VotesOnlineDB');
      data.addColumn('string', 'titleVotesOnlineDB');
      data.addColumn('string', 'textVotesOnlineDB');
      data.addColumn('number', 'www');
      data.addColumn('string', 'titlewww');
      data.addColumn('string', 'textwww');
      data.addColumn('number', 'ImageServer');
      data.addColumn('string', 'titleImageServer');
      data.addColumn('string', 'textImageServer');
      data.addColumn('number', 'Widgets');
      data.addColumn('string', 'titleWidgets');
      data.addColumn('string', 'textWidgets');
      data.addColumn('number', 'DocumentServices');
      data.addColumn('string', 'titleDocumentServices');
      data.addColumn('string', 'textDocumentServices');
      data.addColumn('number', 'CrawlerSpawn');
      data.addColumn('string', 'titleCrawlerSpawn');
      data.addColumn('string', 'textCrawlerSpawn');
      data.addColumn('number', 'PCEngine');
      data.addColumn('string', 'titlePCEngine');
      data.addColumn('string', 'textPCEngine');
      data.addColumn('number', 'BloggerUtils');
      data.addColumn('string', 'titleBloggerUtils');
      data.addColumn('string', 'textBloggerUtils');
      data.addColumn('number', 'CacheWarmer');
      data.addColumn('string', 'titleCacheWarmer');
      data.addColumn('string', 'textCacheWarmer');
      data.addColumn('number', 'Categorization');
      data.addColumn('string', 'titleCategorization');
      data.addColumn('string', 'textCategorization');
      data.addColumn('number', 'SourceCrawlingScheduler');
      data.addColumn('string', 'titleSourceCrawlingScheduler');
      data.addColumn('string', 'textSourceCrawlingScheduler');
      data.addColumn('number', 'OBSimulator');
      data.addColumn('string', 'titleOBSimulator');
      data.addColumn('string', 'textOBSimulator');
      data.addRows([
        [new Date(2011, 7 ,5),
         39774, "FAILED VotesOnlineDB", "@ran #ny #la", // VotesOnlineDB
         undefined, undefined, undefined, // www
         undefined, undefined, undefined, // ImageServer
         undefined, undefined, undefined, // Widgets
         39774, undefined, undefined, // DocumentServices
         undefined, undefined, undefined, // CrawlerSpawn
         undefined, undefined, undefined, // PCEngine
         undefined, undefined, undefined, // BloggerUtils
         undefined, undefined, undefined, // CacheWarmer
         undefined, undefined, undefined, // Categorization
         undefined, undefined, undefined, // SourceCrawlingScheduler
         undefined, undefined, undefined],// OBSimulator
        [new Date(2011, 7 ,5),
         undefined, undefined, undefined,// VotesOnlineDB
         undefined, undefined, undefined, // www
         undefined, undefined, undefined, // ImageServer
         undefined, undefined, undefined, // Widgets
         undefined, undefined, undefined, // DocumentServices
         39759, undefined, undefined, // CrawlerSpawn
         undefined, undefined, undefined, // PCEngine
         undefined, undefined, undefined, // BloggerUtils
         undefined, undefined, undefined, // CacheWarmer
         undefined, undefined, undefined, // Categorization
         undefined, undefined, undefined, // SourceCrawlingScheduler
         undefined, undefined, undefined],// OBSimulator
      ]);

 */

  /** An ordered list of the modules */
  private List<String> modules;

  /** The index of a module in the modules list */
  private Map<String, Integer> modulePosition;

  /** The rows in the data table */
  private List<Row> rows;

  /**
   * Represents the data for all modules in a single deployment
   * @author Ran Tavory
   *
   */
  public static class Row {
    private Date date;
    private final List<RowModuleData> modulesData = new ArrayList<AnnotatedTimelineData.RowModuleData>();
    public Date getDate() {
      return date;
    }
    public List<RowModuleData> getModulesData() {
      return modulesData;
    }
  }

  /**
   * Represents the data for a single module in a single specific deployment
   * @author Ran Tavory
   *
   */
  public static class RowModuleData {
    private String revision = "undefined";
    private String title = "undefined";
    private String text = "undefined";
    private final String name;

    public RowModuleData(String name) {
      this.name = name;
    }

    public String getRevision() {
      return revision;
    }
    public String getTitle() {
      return title;
    }
    public String getText() {
      return text;
    }
    public String getName() {
      return name;
    }
  }

  public AnnotatedTimelineData(List<Deployment> deployments) {
    Assert.notNull(deployments, "Deployments are null");
    deployments = filterNonProd(deployments);
    extractModuleNamesAndPosition(deployments);
    buildRows(deployments);
  }

  /**
   * Filters out non-production deployments
   * @param deployments
   * @return a new list with only the production deployments
   */
  private List<Deployment> filterNonProd(List<Deployment> deployments) {
    List<Deployment> ret = new ArrayList<Deployment>();
    for (Deployment deployment : deployments) {
      for (Cluster cluster : deployment.getClusters()) {
        if (cluster.isProduction()) {
          ret.add(deployment);
          break;
        }
      }
    }
    return ret;
  }

  /**
   * Builds the rows table.
   * It assumes that modules and modulePosition are already initialized
   * @param deployments
   */
  private void buildRows(List<Deployment> deployments) {
    Assert.notNull(modules, "modules isn't initialized");
    Assert.notNull(modulePosition, "modulePosition isn't initialized");
    Assert.notNull(deployments, "deployments is null");
    rows = new ArrayList<AnnotatedTimelineData.Row>(deployments.size());
    int size = modules.size();
    for (Deployment deployment : deployments) {
      Row row = new Row();
      row.date = deployment.getStartTime();
      // First fill all rowModules with undefined, "undefined", "undefined"
      // They stand for the version, title and text accordingly
      for (int i = 0; i < size; ++i) {
        row.modulesData.add(new RowModuleData(modules.get(i)));
      }

      // And then replace only the modules deployed in this deployment with their read data.
      for (Module module : deployment.getModules()) {
        String name = module.getName();
        int moduleIndex = modulePosition.get(name);
        RowModuleData rowModuleData = row.modulesData.get(moduleIndex);
        rowModuleData.revision = String.valueOf(deployment.getModulesRevision());
        StringBuffer title = new StringBuffer();
        title.append("\"");
        if (deployment.hasErrors()) {
          title.append(deployment.getStatus());
          title.append(" ");
        } else if (deployment.isCanceled()) {
          title.append("CANCLED ");
        }
        title.append(String.format("<a href='modules?modules=%s'>%s</a>\"", name, name));
        rowModuleData.title = title.toString();
        StringBuilder text = new StringBuilder();
        String committer = deployment.getCommitter().getName();
        text.append(String.format("\"<a href='committers?committers=%s'>@%s</a>", committer, committer));
        for (Tag tag : deployment.getTags()) {
          String tagName = tag.getName();
          text.append(String.format(" <a href='tags?tags=%s'>#%s</a>", tagName, tagName));
        }
        text.append(String.format(" r%d", deployment.getModulesRevision()));
        text.append("\"");
        rowModuleData.text = text.toString();
      }
      rows.add(row);
    }
  }

  /**
   * Extracts the module names from the deployments and fills the modules and modulePosition members.
   * @param deployments
   */
  private void extractModuleNamesAndPosition(List<Deployment> deployments) {
    modules = new ArrayList<String>();
    modulePosition = new HashMap<String, Integer>();
    for (Deployment deployment : deployments) {
      for (Module module : deployment.getModules()) {
        String name = module.getName();
        if (!modules.contains(name)) {
          modules.add(name);
          modulePosition.put(name, modules.size() - 1);
        }
      }
    }
  }

  public List<String> getModules() {
    return modules;
  }

  public Map<String, Integer> getModulePosition() {
    return modulePosition;
  }

  public List<Row> getRows() {
    return rows;
  }
}
