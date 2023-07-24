package org.nuxeo.msaye.sandbox;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.enrichers.AbstractJsonEnricher;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

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
*     "transitions": { ... }
 *   }
 * }}
 * </pre>
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class TransitionsEnricher extends AbstractJsonEnricher<DocumentModel> {

    public static final String NAME = "transitions";

    public TransitionsEnricher() {
        super(NAME);
    }

    @Override
    public void write(JsonGenerator jg, DocumentModel doc) throws IOException {
        // How to instanciate a Session if `obj` is a DocumentModel
        //try (SessionWrapper wrapper = ctx.getSession(obj)) {
        //    CoreSession session = wrapper.getSession();
        //    ...
        //}

//      ObjectNode transitionsJsonObject = getTransitionsAsJson(doc);
//      jg.writeFieldName(NAME);
//      jg.writeObject(Collections.EMPTY_MAP);
//      jg.writeObject(transitionsJsonObject);
        jg.writeArrayFieldStart(NAME);
        for (Object transition : doc.getAllowedStateTransitions().toArray()) {
            jg.writeString(transition.toString());
        }
        jg.writeEndArray();
    }
/*
    private ObjectNode getTransitionsAsJson(DocumentModel doc) {
        ObjectMapper o = new ObjectMapper();

        ObjectNode transitionsObject = o.createObjectNode();
        transitionsObject.put("transitions", doc.getAllowedStateTransitions().toString());

        return transitionsObject;
    }
*/
}
