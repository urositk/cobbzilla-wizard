package org.cobbzilla.wizard.client.script;

import lombok.NoArgsConstructor;
import org.cobbzilla.wizard.util.RestResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor
public class ApiRunnerMultiListener extends ApiRunnerListenerBase {

    private List<ApiRunnerListener> apiListeners = new ArrayList<>();

    @SuppressWarnings("unused") // called from ApiRunner copy constructor via reflection (which is called from ApiScriptMultiDriver.run)
    public ApiRunnerMultiListener (ApiRunnerMultiListener other) {
        super(other.getName());
        for (ApiRunnerListener listener : other.apiListeners) addApiListener(copy(listener));
    }

    public ApiRunnerMultiListener addApiListener(ApiRunnerListener listener) { apiListeners.add(listener); return this; }

    public static ApiRunnerMultiListener addApiListener(ApiRunnerListener multi, ApiRunnerListener add) {
        if (multi == null)  return die("addApiListener: multi was null");
        if (multi instanceof ApiRunnerMultiListener) return ((ApiRunnerMultiListener) multi).addApiListener(add);
        return die("addApiListener: expected ApiRunnerMultiListener, but was "+multi.getClass().getName());
    }

    @Override public void beforeCall(ApiScript script, Map<String, Object> ctx) {
        for (ApiRunnerListener sub : apiListeners) sub.beforeCall(script, ctx);
    }

    @Override public void afterCall(ApiScript script, Map<String, Object> ctx, RestResponse response) {
        for (ApiRunnerListener sub : apiListeners) sub.afterCall(script, ctx, response);
    }

    @Override public void beforeScript(String before, Map<String, Object> ctx) throws Exception {
        for (ApiRunnerListener sub : apiListeners) sub.beforeScript(before, ctx);
    }

    @Override public void afterScript(String after, Map<String, Object> ctx) throws Exception {
        for (ApiRunnerListener sub : apiListeners) sub.afterScript(after, ctx);
    }

}
