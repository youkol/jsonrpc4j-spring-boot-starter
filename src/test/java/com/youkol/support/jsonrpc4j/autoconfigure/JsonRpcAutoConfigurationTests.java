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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.RequestInterceptor;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcClientProxyCreator;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;
import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.youkol.support.jsonrpc4j.server.DelegatingRequestInterceptor;
import com.youkol.support.jsonrpc4j.server.JsonRpcMultiServer;
import com.youkol.support.jsonrpc4j.service.welcome.WelcomeService;
import com.youkol.support.jsonrpc4j.servlet.JsonRpcServlet;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
class JsonRpcAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JsonRpcAutoConfiguration.class));

    @Test
    void jsonRpcServerEnabledMissing() {
        this.contextRunner
                .run(context -> {
                    assertThat(context.getBean(JsonRpcServer.class))
                            .isInstanceOf(JsonRpcMultiServer.class);
                });
    }

    @Test
    void jsonRpcServerWelcomeDisabled() {
        this.contextRunner
                .withPropertyValues("youkol.jsonrpc4j.server.welcome.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(WelcomeService.class);
                    assertThat(context).hasSingleBean(JsonRpcServer.class);
                    assertThat(context.getBean(JsonRpcServer.class))
                            .isInstanceOf(JsonRpcMultiServer.class);
                });
    }

    @Test
    void autoJsonRpcClientProxyCreatorEnabled() {
        this.contextRunner.withPropertyValues("youkol.jsonrpc4j.client.enabled=true",
                "youkol.jsonrpc4j.client.base-url=https://github.com/youkol/jsonrpc4j-spring-boot-starter",
                "youkol.jsonrpc4j.client.scan-package=com.youkol.support.jsonrpc4j.service")
                .run(context -> {
                    assertThat(context.getBean(AutoJsonRpcClientProxyCreator.class)).isNotNull();
                });
    }

    @Test
    void autoJsonRpcServiceImplExporterEnabled() {
        this.contextRunner.withPropertyValues("youkol.jsonrpc4j.server.rethrow-exceptions=true",
                "youkol.jsonrpc4j.server.allow-extra-params=true",
                "youkol.jsonrpc4j.server.allow-less-params=true")
                .run(context -> {
                    assertThat(context.getBean(AutoJsonRpcServiceImplExporter.class)).isNotNull();
                    assertThat(context.getBean(JsonServiceExporter.class)).isNotNull();
                });
    }

    @Test
    void jsonRpcServerParallelBatchEnabled() {
        this.contextRunner.withUserConfiguration(TaskExecutionAutoConfiguration.class)
                .withPropertyValues("youkol.jsonrpc4j.server.parallel-enabled=true")
                .run(context -> {
                    assertThat(context.getBean(ExecutorService.class)).isNotNull();
                    assertThat(context.getBean(ExecutorService.class)).isInstanceOf(ThreadPoolExecutor.class);
                });
    }

    @Test
    void jsonRpcServerServletEnabled() {
        new WebApplicationContextRunner().withConfiguration(AutoConfigurations.of(JsonRpcAutoConfiguration.class))
                .run(context -> {
                    assertThat(context.getBean(JsonRpcServlet.class)).isNotNull();
                    ServletRegistrationBean<?> registration = context.getBean(ServletRegistrationBean.class);
                    assertThat(registration.getUrlMappings()).containsExactly("/jsonrpc");
                });
    }

    @Test
    void jsonRpcServerServletDisabled() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JsonRpcAutoConfiguration.class))
                .withPropertyValues("youkol.jsonrpc4j.server.servlet.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JsonRpcServlet.class)
                            .doesNotHaveBean(
                                    JsonRpcServerServletConfiguration.DEFAULT_JSONRPC_SERVLET_REGISTRATION_BEAN_NAME);
                });
    }

    @Test
    void jsonRpcServerWithRequestInterceptor() {
        this.contextRunner
                .withUserConfiguration(RequestInterceptorConfiguration.class)
                .run(context -> {
                    assertThat(context.getBean(DelegatingRequestInterceptor.class)).isNotNull();
                    assertThat(context.getBean(JsonRpcServer.class)).isNotNull();
                    JsonRpcServer jsonRpcServer = context.getBean(JsonRpcServer.class);
                    assertThat(jsonRpcServer.getRequestInterceptor()).isInstanceOf(DelegatingRequestInterceptor.class);
                });
    }

    @Test
    void jsonRpcServerWithoutRequestInterceptor() {
        this.contextRunner
                .run(context -> {
                    assertThat(context.getBeansOfType(RequestInterceptor.class)).isEmpty();
                    assertThat(context.getBeansOfType(DelegatingRequestInterceptor.class)).isEmpty();
                    assertThat(context.getBean(JsonRpcServer.class)).isNotNull();
                    JsonRpcServer jsonRpcServer = context.getBean(JsonRpcServer.class);
                    assertThat(jsonRpcServer.getRequestInterceptor()).isNull();
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class RequestInterceptorConfiguration {

        @Bean
        public RequestInterceptor requestInterceptor() {
            return request -> {
            };
        }
    }

}
