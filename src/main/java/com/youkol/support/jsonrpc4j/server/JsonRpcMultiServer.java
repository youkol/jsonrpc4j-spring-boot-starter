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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * A multiple service dispatcher that supports JSON-RPC "method" names
 * that use dot-notation to select a server endpoint. For example:
 *
 * <pre>
 * {
 *    "jsonrpc": "2.0",
 *    "method": "service.method",
 *    "params": {"foo", "bar"},
 *    "id": 1
 * }
 * </pre>
 *
 * An example of using this class is:
 * <code>
 * JsonRpcMultiServer rpcServer = new JsonRpcMultiServer();
 * rpcServer.addService("Foo", new FooService())
 * .addService("Bar", new BarService());
 * </code>
 * A client can then call a <i>test(String, String)</i> method on the Foo
 * service
 * like this:
 *
 * <pre>
 * {
 *    "jsonrpc": "2.0",
 *    "method": "Foo.test",
 *    "params": ["some", "thing"],
 *    "id": 1
 * }
 * </pre>
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public class JsonRpcMultiServer extends JsonRpcServer {

    public static final char DEFAULT_SEPARATOR = '.';
    private static final Logger logger = LoggerFactory.getLogger(JsonRpcMultiServer.class);

    private final Map<String, Object> handlerMap;
    private final Map<String, Class<?>> interfaceMap;
    private char separator = DEFAULT_SEPARATOR;

    public JsonRpcMultiServer() {
        this(new ObjectMapper());
        logger.debug("created empty multi server");
    }

    public JsonRpcMultiServer(ObjectMapper mapper) {
        super(mapper, mapper);
        this.handlerMap = new HashMap<>();
        this.interfaceMap = new HashMap<>();
    }

    public JsonRpcMultiServer addService(String name, Object handler) {
        return this.addService(name, handler, null);
    }

    public JsonRpcMultiServer addService(String name, Object handler, Class<?> remoteInterface) {
        logger.debug("add service interface {} with handler {}", remoteInterface, handler);
        this.handlerMap.put(name, handler);
        if (remoteInterface != null) {
            this.interfaceMap.put(name, remoteInterface);
        }
        return this;
    }

    public char getSeparator() {
        return this.separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    /**
     * Returns the handler's class or interfaces. The serviceName is used
     * to look up a registered handler.
     *
     * @param serviceName the optional name of a service
     * @return the class
     */
    @Override
    protected Class<?>[] getHandlerInterfaces(String serviceName) {
        Class<?> remoteInterface = this.interfaceMap.get(serviceName);
        if (remoteInterface != null) {
            return new Class<?>[] { remoteInterface };
        } else if (Proxy.isProxyClass(this.getHandler(serviceName).getClass())) {
            return this.getHandler(serviceName).getClass().getInterfaces();
        } else {
            return new Class<?>[] { this.getHandler(serviceName).getClass() };
        }
    }

    /**
     * Get the service name from the methodNode. JSON-RPC methods with the form
     * Service.method will result in "Service" being returned in this case.
     *
     * @param methodName method name
     * @return the name of the service, or <code>null</code>
     */
    @Override
    protected String getServiceName(final String methodName) {
        if (methodName != null) {
            int ndx = methodName.indexOf(this.separator);
            if (ndx > 0) {
                return methodName.substring(0, ndx);
            }
        }
        return methodName;
    }

    /**
     * Get the method name from the methodNode, stripping off the service name.
     *
     * @param methodName the JsonNode for the method
     * @return the name of the method that should be invoked
     */
    @Override
    protected String getMethodName(final String methodName) {
        if (methodName != null) {
            int ndx = methodName.indexOf(this.separator);
            if (ndx > 0) {
                return methodName.substring(ndx + 1);
            }
        }
        return methodName;
    }

    /**
     * Get the handler (object) that should be invoked to execute the specified
     * RPC method based on the specified service name.
     *
     * @param serviceName the service name
     * @return the handler to invoke the RPC call against
     */
    @Override
    protected Object getHandler(String serviceName) {
        Object handler = this.handlerMap.get(serviceName);
        if (handler == null) {
            logger.error("Service '{}' is not registered in this multi-server", serviceName);
            throw new RuntimeException("Service '" + serviceName + "' does not exist");
        }
        return handler;
    }
}
