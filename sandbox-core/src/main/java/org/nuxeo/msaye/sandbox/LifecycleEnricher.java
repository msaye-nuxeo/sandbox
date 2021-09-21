package org.nuxeo.msaye.sandbox;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;

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
        writeTransitionsField(jg, TRANSITIONS_NAME, doc);
        jg.writeEndObject();
    }

    protected void writePolicyField(JsonGenerator jg, String fieldName, DocumentModel doc)
        throws IOException {
        jg.writeStringField(fieldName, doc.getLifeCyclePolicy());
    }

    protected void writeTransitionsField(JsonGenerator jg, String fieldName, DocumentModel doc)
        throws IOException {
        jg.writeArrayFieldStart(fieldName);
        for (Object transition : doc.getAllowedStateTransitions().toArray()) {
            jg.writeString(transition.toString());
        }
        jg.writeEndArray();
    }

}
