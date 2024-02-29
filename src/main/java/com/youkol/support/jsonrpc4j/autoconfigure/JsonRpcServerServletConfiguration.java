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

import javax.servlet.ServletRegistration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.youkol.support.jsonrpc4j.servlet.JsonRpcServlet;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ JsonRpcServlet.class, JsonRpcServer.class })
@ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "server.servlet.enabled", matchIfMissing = true)
public class JsonRpcServerServletConfiguration {

    public static final String DEFAULT_JSONRPC_SERVLET_BEAN_NAME = "jsonrpcServlet";

    public static final String DEFAULT_JSONRPC_SERVLET_REGISTRATION_BEAN_NAME = "jsonrpcServletRegistration";

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ServletRegistration.class)
    @EnableConfigurationProperties(JsonRpcProperties.class)
    static class JsonRpcServletConfiguration {

        @Bean(name = DEFAULT_JSONRPC_SERVLET_BEAN_NAME)
        @ConditionalOnMissingBean(name = DEFAULT_JSONRPC_SERVLET_BEAN_NAME)
        public JsonRpcServlet jsonRpcServlet(JsonRpcServer jsonRpcServer) {
            return new JsonRpcServlet(jsonRpcServer);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ServletRegistration.class)
    @EnableConfigurationProperties(JsonRpcProperties.class)
    @Import(JsonRpcServletConfiguration.class)
    static class JsonRpcServletRegistrationConfiguration {

        @Bean(name = DEFAULT_JSONRPC_SERVLET_REGISTRATION_BEAN_NAME)
        @ConditionalOnMissingBean(name = DEFAULT_JSONRPC_SERVLET_REGISTRATION_BEAN_NAME)
        @ConditionalOnBean(value = JsonRpcServlet.class, name = DEFAULT_JSONRPC_SERVLET_BEAN_NAME)
        public ServletRegistrationBean<JsonRpcServlet> jsonRpcServletRegistrationBean(JsonRpcServlet jsonRpcServlet,
                JsonRpcProperties jsonRpcProperties) {
            ServletRegistrationBean<JsonRpcServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                    jsonRpcServlet, jsonRpcProperties.getServer().getServlet().getPath());
            servletRegistrationBean.setLoadOnStartup(jsonRpcProperties.getServer().getServlet().getLoadOnStartup());
            servletRegistrationBean.setName(DEFAULT_JSONRPC_SERVLET_BEAN_NAME);

            return servletRegistrationBean;
        }
    }
}
