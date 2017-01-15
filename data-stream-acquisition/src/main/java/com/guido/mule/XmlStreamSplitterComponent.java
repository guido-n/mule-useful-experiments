package com.guido.mule;

import org.milyn.Smooks;
import org.milyn.FilterSettings;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.routing.basic.FragmentSerializer;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * XmlStreamSplitterComponent
 */
public class XmlStreamSplitterComponent implements Callable {

    private static final Logger log = LoggerFactory.getLogger(XmlStreamSplitterComponent.class);

    private static final String FRAGMENT_BEAN_ID = "fragmentBeanId";
    private static final String MULE_CONTEXT_BEAN_ID = "muleContextBeanId";
    private static final String EMPTY_STRING = "";

    private Smooks smooks;

    /**
     *
     * @param xpathSplitExpression
     * @param sendToUri
     */
    public XmlStreamSplitterComponent(String xpathSplitExpression, String sendToUri) {

        smooks = new Smooks();
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.addVisitor(new FragmentSerializer().setBindTo(FRAGMENT_BEAN_ID), xpathSplitExpression);

        // Cannot use lambdas here cause addVisitor()'s param is a Visitor (marker interface), not a SAXVisitAfter
        smooks.addVisitor(new XmlStreamSplitterSAXVisitAfter(sendToUri), xpathSplitExpression);
    }

    /**
     *
     * @param eventContext
     * @return
     * @throws Exception
     */
    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {

        InputStream is = eventContext.getMessage().getPayload(InputStream.class);

        ExecutionContext smooksExecutionContext = smooks.createExecutionContext();
        smooksExecutionContext.getBeanContext().addBean(MULE_CONTEXT_BEAN_ID, eventContext.getMuleContext());

        smooks.filterSource(smooksExecutionContext, new StreamSource(is));

        return EMPTY_STRING;

    }

    /**
     * XmlStreamSplitterSAXVisitAfter class
     */
    private class XmlStreamSplitterSAXVisitAfter implements SAXVisitAfter {

        private String sendToUri;

        public XmlStreamSplitterSAXVisitAfter(String sendToUri) {
            this.sendToUri = sendToUri;
        }

        @Override
        public void visitAfter(SAXElement saxElement, ExecutionContext executionContext) throws SmooksException, IOException {
            try {
                Object xmlElement = executionContext.getBeanContext().getBean(FRAGMENT_BEAN_ID);
                MuleContext muleContext = (MuleContext) executionContext.getBeanContext().getBean(MULE_CONTEXT_BEAN_ID);
                muleContext.getClient().send(sendToUri, new DefaultMuleMessage(xmlElement, muleContext));
            } catch (Exception e) {
                String message = String.format("XmlStreamSplitterComponent visitAfter() exception caught: %s", e.getMessage());
                log.error(message, e);
                throw new RuntimeException(message, e);
            }
        }
    }
}
