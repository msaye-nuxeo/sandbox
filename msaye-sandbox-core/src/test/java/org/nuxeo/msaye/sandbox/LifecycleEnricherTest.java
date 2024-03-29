package org.nuxeo.msaye.sandbox;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.marshallers.json.document.DocumentModelJsonWriter;
import org.nuxeo.ecm.core.io.marshallers.json.AbstractJsonWriterTest;
import org.nuxeo.ecm.core.io.marshallers.json.JsonAssert;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext.CtxBuilder;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class})
@Deploy({"org.nuxeo.msaye.sandbox.sandbox-core"})
public class LifecycleEnricherTest extends AbstractJsonWriterTest.Local<DocumentModelJsonWriter, DocumentModel> {

    public LifecycleEnricherTest() {
        super(DocumentModelJsonWriter.class, DocumentModel.class);
    }

    @Inject
    private CoreSession session;

    @Test
    public void test() throws Exception {
//      DocumentModel doc = session.getDocument(new PathRef("/"));
        DocumentModel obj = session.createDocumentModel("/", "foo", "Document");
        DocumentModel doc = session.createDocument(obj);
        JsonAssert json = jsonAssert(doc, CtxBuilder.enrich("document", LifecycleEnricher.NAME).get());
        System.out.println("doc: " + doc);
        System.out.println("policy: " + doc.getLifeCyclePolicy());
        System.out.println("json: " + json);
        json = json.has("contextParameters").isObject();
        json.properties(1);
        json.has(LifecycleEnricher.NAME).isObject();
    }

}
