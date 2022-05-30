package com.dem0.shell;

import java.beans.Expression;

public class Expressionexp {
    public static void main(String[] args) throws Exception {
        String payload = "calc";
        Expression expression = new Expression(Runtime.getRuntime(), "\u0065" + "\u0078" + "\u0065" + "\u0063", new Object[]{payload});
        expression.getValue();
    }
}
