package org.cobbzilla.wizard.client.script;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.util.RestResponse;

import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class ApiRunnerListenerBase implements ApiRunnerListener {

    public ApiRunnerListenerBase (String name) { setName(name); }

    @Getter @Setter private String name;

    @Override public void beforeCall(ApiScript script, Map<String, Object> ctx) {}
    @Override public void afterCall(ApiScript script, Map<String, Object> ctx, RestResponse response) {}

    @Override public void statusCheckFailed(ApiScript script, RestResponse restResponse) {
        if (script.isTimedOut()) {
            die("statusCheckFailed("+getName()+"): request "+script.getRequestLine()+" expected "+script.getResponse().getStatus()+" but was "+restResponse.status
                    + (restResponse.status == 422 ? ", validation errors: "+restResponse.json : ""));
        }
    }

    @Override public void conditionCheckFailed(ApiScript script, RestResponse restResponse, ApiScriptResponseCheck check, Map<String, Object> ctx) {
        if (script.isTimedOut()) {
            die("conditionCheckFailed("+getName()+"): "+script.getRequestLine()+":\nfailed condition="+check+"\nserver response="+restResponse+"\nctx="+ctx);
        }
    }

    @Override public void sessionIdNotFound(ApiScript script, RestResponse restResponse) {
        if (script.isTimedOut()) die("sessionIdNotFound("+getName()+"): expected "+script.getResponse().getSession()+", server response="+restResponse);
    }

    @Override public void scriptCompleted(ApiScript script) {}

    @Override public void scriptTimedOut(ApiScript script) { die("scriptTimedOut: script="+script+", timed out"); }

    @Override public void unexpectedResponse(ApiScript script, RestResponse restResponse) {
        if (script.isTimedOut()) die("unexpectedResponse("+getName()+"): script="+script+", server response="+restResponse);
    }

    @Override public void beforeScript(String before, Map<String, Object> ctx) throws Exception {}
    @Override public void afterScript(String after, Map<String, Object> ctx) throws Exception {}

    @Override public boolean skipCheck(ApiScript script, ApiScriptResponseCheck check) { return false; }

}
