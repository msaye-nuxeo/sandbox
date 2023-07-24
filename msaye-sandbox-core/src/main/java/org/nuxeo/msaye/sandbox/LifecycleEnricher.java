package org.nuxeo.msaye.sandbox;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerator;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.enrichers.AbstractJsonEnricher;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;
import org.nuxeo.ecm.core.lifecycle.LifeCycleService;
import org.nuxeo.runtime.api.Framework;

/**
 * Enrich {@link nuxeo.ecm.core.api.DocumentModel} Json.
 * <p>
 * Format is:
 * </p>
 * <pre>
 * {@code
 * {
 *   ...
 *   "contextParameters": {
 *     "lifecycle": { ... }
 *   }
 * }}
 * </pre>
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class LifecycleEnricher extends AbstractJsonEnricher<DocumentModel> {

    private LifeCycleService lifeCycleService;

    public static final String NAME = "lifecycle";
    public static final String POLICY_NAME = "policy";
    public static final String STATE_NAME = "state";
    public static final String TRANSITIONS_NAME = "transitions";

    public LifecycleEnricher() {
        super(NAME);
    }

    @Override
    public void write(JsonGenerator jg, DocumentModel doc) throws IOException {
        LifeCycleService lcs = Framework.getService(LifeCycleService.class);

        jg.writeFieldName(NAME);
        jg.writeStartObject();
        writePolicyField(jg, POLICY_NAME, doc);
        writeStateField(jg, STATE_NAME, doc);
        writeTransitionsField(jg, TRANSITIONS_NAME, doc);
        jg.writeEndObject();
    }

    protected void writePolicyField(JsonGenerator jg, String fieldName, DocumentModel doc)
        throws IOException {
        String policy = doc.getLifeCyclePolicy();
//      System.out.println("policy: " + policy);
//      System.out.println("  class: " + policy.getClass());
        if (policy == null) {
            jg.writeNullField(fieldName);
        } else {
            jg.writeStringField(fieldName, policy);
        }
    }

    protected void writeStateField(JsonGenerator jg, String fieldName, DocumentModel doc)
        throws IOException {
        String state = doc.getCurrentLifeCycleState();
//      System.out.println("state: " + state);
//      System.out.println("  class: " + state.getClass());
        if (state == null) {
            jg.writeNullField(fieldName);
        } else {
            jg.writeStringField(fieldName, state);
        }
    }

    protected void writeTransitionsField(JsonGenerator jg, String fieldName, DocumentModel doc)
        throws IOException {
        jg.writeArrayFieldStart(fieldName);
        Collection<String> transitions = doc.getAllowedStateTransitions();
//      System.out.println("transitions: " + transitions);
//      System.out.println("  class: " + transitions.getClass());
        for (Object transition : transitions.toArray()) {
            jg.writeString(transition.toString());
        }
        jg.writeEndArray();
    }

}
