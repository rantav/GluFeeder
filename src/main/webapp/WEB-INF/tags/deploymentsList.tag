<%@ tag body-content="empty" %>
<%@ attribute name="deployments" required="true" type="java.util.Collection"%>
<%@ attribute name="allowModification" required="true" type="java.lang.Boolean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setTimeZone value="Israel" scope="session"/>
  <table>
    <thead class="greenback">
      <tr>
        <th>Modules</th>
        <th>Status</th>
        <th>Progress</th>
        <th>Tags</th>
        <th>Clusters</th>
        <th>Revision</th>
        <th>Execution</th>
        <th>Start <a href="javascript:$('.times').toggle('slow');void(0)" class="showhide">[+]</a></th>
        <th>End</th>
        <th>Last Touch</th>
        <th>Error?</th>
        <th>Source</th>
        <th>Diff <a href="javascript:$('.json').toggle('slow');void(0)" class="showhide">[+]</a></th>
        <th>Phases <a href="javascript:$('.phases').toggle('slow');void(0)" class="showhide">[+]</a></th>
      </tr>
    </thead>
    <tbody class="greyback">
      <c:forEach items="${deployments}" var="deployment">
        <tr>
          <td>
            <c:forEach items="${deployment.modules}" var="module">
              <a href="modules?modules=${module.name}">${module.name}</a>
            </c:forEach>
          </td>
          <td>
            <c:if test="${allowModification}">
	             <c:choose>
	               <c:when test="${deployment.status eq 'PENDING_YUM'}">
	                 <a href="cancel?id=${deployment.id}&limit=${limit}" class="cancel">cancel</a>
	               </c:when>
	               <c:otherwise>
                   <a href="archive?id=${deployment.id}&limit=${limit}" class="cancel" title="archive - will not be displayed in the /status page">[arch]</a> 
                   <a href="delete?id=${deployment.id}&limit=${limit}" class="cancel" title="permanently delete, completely deletes this deployment">[del]</a>
	               </c:otherwise>
	             </c:choose>
             </c:if>
             ${deployment.status}
          </td>
          <td>
              <c:forEach items="${deployment.deploymentPhases}" var="phase">
               ${phase.progress}
              <br/>
              </c:forEach>
              <c:if test="${deployment.status eq 'GLU_DEPLOYING_FIRST_HOST' or deployment.status eq 'GLU_DEPLOYING_MORE_HOSTS'}">
                <img src="../loading.gif" class="image"/>
              </c:if>
           </td>
          <td>
            <c:forEach items="${deployment.tags}" var="tag">
              <a href="tags?tags=${tag.name}">${tag.name}</a>
            </c:forEach>
          </td>
          <td>
            <c:forEach items="${deployment.clusters}" var="cluster">
              <a href="clusters?clusters=${cluster.name}">${cluster.name}</a>
            </c:forEach>
          </td>
          <td>
             <a href="https://svn.il.outbrain.com:8443/viewvc/Outbrain/Outbrain/trunk/?pathrev=${deployment.modulesRevision}">
               ${deployment.modulesRevision}
             </a>
          </td>
          <td>
            <c:forEach items="${deployment.deploymentPhases}" var="phase">
              <c:choose>
                <c:when test="${empty phase.executionId}">
                  <span class="redback">null</span>
                </c:when>
                <c:otherwise>
                  <a href="${gluServer}plan/deployments/${phase.executionId}?showErrorsOnly=false">${phase.executionId}</a>
              </c:otherwise>
            </c:choose>
            <br/>
           </c:forEach>
          </td>
          <td>
            <fmt:formatDate value="${deployment.startTime}" pattern="HH:mm"/>
            <div class="times" style="display:none">
               <fmt:formatDate value="${deployment.startTime}" pattern="dd-MM" />
            </div>
          </td>
          <td>
            <c:if test="${deployment.timedout}">
              <span class="redback">TIMEOUT</span>
            </c:if>
            <fmt:formatDate value="${deployment.endTime}" pattern="HH:mm" />
            <div class="times" style="display:none">
               <fmt:formatDate value="${deployment.endTime}" pattern="dd-MM" />
            </div>
          </td>
          <td>
           <fmt:formatDate value="${deployment.lastTouched}" pattern="HH:mm" />
            <div class="times" style="display:none">
               <fmt:formatDate value="${deployment.lastTouched}" pattern="dd-MM" />
            </div>
          </td>
          <td>${deployment.error.localizedMessage}</td>
          <td>
             ${deployment.source}
             <div>
               <a href="committers?committers=${deployment.committer}">${deployment.committer}</a>
             </div>
          </td>
          <td><pre style="display:none" class="json">${deployment.jsonDiff}</pre></td>
          <td>
           <div class="phases" style="display:none">
           <c:forEach items="${deployment.deploymentPhases}" var="phase">
            <c:choose>
              <c:when test="${empty phase.executionId}">
               ${phase}
              </c:when>
              <c:otherwise>
	             <a href="${gluServer}plan/deployments/${phase.executionId}?showErrorsOnly=false">
	               ${phase}
	              </a>
              </c:otherwise>
            </c:choose>
             <hr/>
           </c:forEach>
           </div>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
