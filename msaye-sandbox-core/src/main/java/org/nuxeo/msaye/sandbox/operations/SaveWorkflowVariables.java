package org.nuxeo.msaye.sandbox.operations;

import static org.nuxeo.msaye.sandbox.constants.Constants.CAT_SANDBOX;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PropertyException;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.routing.core.impl.GraphRoute;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Allows a workflow actor to save workflow variables.
 * Note: this does not work for saving Node/Task scoped variables, only Workflow scoped variables.
 */
@Operation(id=SaveWorkflowVariables.ID, category=CAT_SANDBOX, label="Sandbox.SaveWorkflowVariables", description="Allows a workflow actor to save workflow variables.")
public class SaveWorkflowVariables {

    public static final String ID = "Sandbox.SaveWorkflowVariables";

    protected static final Logger logger = LogManager.getLogger(SaveWorkflowVariables.class);

    protected static final ObjectMapper om = new ObjectMapper();

    @Context
    protected AutomationService automationService;

    @Context
    protected CoreSession restrictedSession;

    @Context
    protected OperationContext ctx;

    @Context
    protected TaskService taskService;

    @Param(name = "variables", required = false, description = "A JSON object containing variables as key, values")
    protected JsonNode variables;

    @Param(name = "variablesJson", required = false, description = "A JSON object containing variables as key, values, as in: {\"var1\": 1,\"var2\": 2}")
    protected String variablesJson;

    @Param(name = "taskId", description = "UUID of the current task editing the variables")
    protected String taskId;

    @OperationMethod
    public void run() throws OperationException {
        // getTask throws DocumentSecurityException if the calling user is not an Actor on this task.
        Task task = taskService.getTask(restrictedSession, taskId);
        // Ensure current user can write properties on the task document.
        if (!restrictedSession.hasPermission(task.getDocument().getRef(), SecurityConstants.WRITE_PROPERTIES)) {
            throw new DocumentSecurityException();
        }
//      Map<String, Object> variables = parseVariablesJson();
        Map<String, String> variables = jsonNodeToMap();

        // Replaces the restrictedSession with an unrestricted session bound to the system user.
        UnrestrictedSessionRunner runner = new UnrestrictedSessionRunner(restrictedSession) {
            @Override
            public void run() {
                setWorkflowVariables(session, task, variables);
            }
        };
        // run the above code block as the system user
        runner.runUnrestricted();
    }

    private Map<String, String> jsonNodeToMap() {
        Map<String, String> results = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = variables.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            results.put(field.getKey(), field.getValue().asText());
        }
        return results;
    }

    private Map<String, Object> parseVariablesJson() throws OperationException {
        try {
            return om.readValue(variablesJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            String msg = String.format("Error parsing '%s' as a JSON object", variablesJson);
            throw new OperationException(msg, e);
        }
    }

    private void setWorkflowVariables(CoreSession session, Task task, Map<String, String> variables) {
        String workflowInstanceId = task.getProcessId();
        DocumentModel workflowInstance = session.getDocument(new IdRef(workflowInstanceId));
        GraphRoute graph = workflowInstance.getAdapter(GraphRoute.class);
        Map<String, Serializable> vars = graph.getVariables();
        for (var entry : variables.entrySet()) {
            vars.replace(entry.getKey(), (Serializable) entry.getValue());
        }
        try {
            graph.setVariables(vars);
        } catch (PropertyException e) {
            e.addInfo("Cannot set properties on workflow instance with the id: " + workflowInstanceId);
            throw e;
        }

    }

}
