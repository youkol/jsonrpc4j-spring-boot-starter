/*
 * Copyright (C) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.youkol.support.jsonrpc4j.server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.InvocationListener;

/**
 * Delegate implementation for {@link InvocationListener}
 *
 * @author jackiea
 * @since 1.0.0
 */
public class DelegatingInvocationListener implements InvocationListener {

    private List<InvocationListener> invocationListeners = new ArrayList<>();

    public DelegatingInvocationListener(List<InvocationListener> invocationListeners) {
        if (invocationListeners != null) {
            this.invocationListeners = invocationListeners;
        }
    }

    @Override
    public void willInvoke(Method method, List<JsonNode> arguments) {
        for (InvocationListener invocationListener : this.invocationListeners) {
            invocationListener.willInvoke(method, arguments);
        }
    }

    @Override
    public void didInvoke(Method method, List<JsonNode> arguments, Object result, Throwable t, long duration) {
        for (InvocationListener invocationListener : this.invocationListeners) {
            invocationListener.didInvoke(method, arguments, result, t, duration);
        }
    }

}
