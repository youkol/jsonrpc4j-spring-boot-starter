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
package com.youkol.support.jsonrpc4j.autoconfigure;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import com.googlecode.jsonrpc4j.JsonRpcBasicServer;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = JsonRpcProperties.JSONRPC_PREFIX)
public class JsonRpcProperties {

    public static final String JSONRPC_PREFIX = "wkrj.jsonrpc";

    private boolean enabled = true;

    private final Server server = new Server();

    private final Client client = new Client();

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Server getServer() {
        return this.server;
    }

    public Client getClient() {
        return this.client;
    }

    public static class Servlet {

        private boolean enabled = true;

        /**
         * Path of the jsonrpc servlet.
         */
        private String path = "/jsonrpc";

        /**
         * Load on startup priority of the jsonrpc servlet.
         */
        private int loadOnStartup = -1;

        public boolean getEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return this.path;
        }

        public void setPath(String path) {
            Assert.notNull(path, "Path must not be null");
            Assert.isTrue(!path.contains("*"), "Path must not contain wildcards");
            this.path = path;
        }

        public int getLoadOnStartup() {
            return this.loadOnStartup;
        }

        public void setLoadOnStartup(int loadOnStartup) {
            this.loadOnStartup = loadOnStartup;
        }

    }

    public static class Welcome {

        private boolean enabled = true;

        public boolean getEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Server {

        private boolean enabled = true;

        private final Servlet servlet = new Servlet();

        private final Welcome welcome = new Welcome();

        private boolean backwardsCompatible = true;

        private boolean rethrowExceptions = false;

        private boolean allowExtraParams = false;

        private boolean allowLessParams = false;

        private boolean shouldLogInvocationErrors = true;

        private Duration parallelBatchProcessingTimeout = Duration.ofSeconds(30);

        private String contentType = JsonRpcBasicServer.JSONRPC_CONTENT_TYPE;

        public boolean getEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Servlet getServlet() {
            return this.servlet;
        }

        public Welcome getWelcome() {
            return this.welcome;
        }

        public boolean getBackwardsCompatible() {
            return this.backwardsCompatible;
        }

        public void setBackwardsCompatible(boolean backwardsCompatible) {
            this.backwardsCompatible = backwardsCompatible;
        }

        public boolean getRethrowExceptions() {
            return this.rethrowExceptions;
        }

        public void setRethrowExceptions(boolean rethrowExceptions) {
            this.rethrowExceptions = rethrowExceptions;
        }

        public boolean getAllowExtraParams() {
            return this.allowExtraParams;
        }

        public void setAllowExtraParams(boolean allowExtraParams) {
            this.allowExtraParams = allowExtraParams;
        }

        public boolean getAllowLessParams() {
            return this.allowLessParams;
        }

        public void setAllowLessParams(boolean allowLessParams) {
            this.allowLessParams = allowLessParams;
        }

        public boolean getShouldLogInvocationErrors() {
            return this.shouldLogInvocationErrors;
        }

        public void setShouldLogInvocationErrors(boolean shouldLogInvocationErrors) {
            this.shouldLogInvocationErrors = shouldLogInvocationErrors;
        }

        public Duration getParallelBatchProcessingTimeout() {
            return this.parallelBatchProcessingTimeout;
        }

        public void setParallelBatchProcessingTimeout(Duration parallelBatchProcessingTimeout) {
            this.parallelBatchProcessingTimeout = parallelBatchProcessingTimeout;
        }

        public String getContentType() {
            return this.contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

    }

    public static class Client {

        private boolean enabled = false;

        private String baseUrl;

        private String scanPackage;

        public boolean getEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return this.baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getScanPackage() {
            return this.scanPackage;
        }

        public void setScanPackage(String scanPackage) {
            this.scanPackage = scanPackage;
        }

    }
}
