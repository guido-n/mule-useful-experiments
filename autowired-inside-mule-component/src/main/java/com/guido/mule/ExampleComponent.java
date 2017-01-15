package com.guido.mule;

import com.guido.spring.ExampleTool;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Example Mule component
 */
public class ExampleComponent implements Callable {

    @Autowired
    @Qualifier("exampleTool")
    ExampleTool tool;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {

        return tool.saySomething();

    }

}
