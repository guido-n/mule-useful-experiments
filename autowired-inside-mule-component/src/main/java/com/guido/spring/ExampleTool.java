package com.guido.spring;

/**
 * ExampleTool
 */
public class ExampleTool {

    private String param;

    public ExampleTool(String param) {
        this.param = param;
    }

    public String saySomething() {
        return param;
    }

}
