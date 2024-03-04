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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.ConvertedParameterTransformer;
import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.HttpStatusCodeProvider;
import com.googlecode.jsonrpc4j.InvocationListener;
import com.googlecode.jsonrpc4j.JsonRpcBasicServer;
import com.googlecode.jsonrpc4j.JsonRpcInterceptor;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcClientProxyCreator;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;

/**
 *
 * @author jackiea
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(WelcomeConfiguration.class)
@ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "enabled", matchIfMissing = true)
public class JsonRpcAnnotationConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AutoJsonRpcServiceImplExporter.class)
    @ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "server.enabled", matchIfMissing = true)
    static class JsonRpcAnnotationServerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public AutoJsonRpcServiceImplExporter autoJsonRpcServiceImplExporter(
                ApplicationContext applicationContext,
                ObjectProvider<ObjectMapper> objectMapper,
                ObjectProvider<ErrorResolver> errorResolver,
                ObjectProvider<JsonRpcInterceptor> jsonRpcInterceptor,
                ObjectProvider<InvocationListener> invocationListener,
                ObjectProvider<ConvertedParameterTransformer> convertedParameterTransformer,
                ObjectProvider<HttpStatusCodeProvider> httpStatusCodeProvider,
                ObjectProvider<ExecutorService> batchExecutorService,
                List<AutoJsonRpcServiceImplExporterCustomizer> customizers) {
            AutoJsonRpcServiceImplExporter serviceExporter = new AutoJsonRpcServiceImplExporter();

            Environment environment = applicationContext.getEnvironment();

            Boolean registerTraceInterceptor = environment.getProperty(
                    JsonRpcProperties.JSONRPC_PREFIX + ".server.register-trace-interceptor", Boolean.class, false);
            boolean backwardsCompatible = environment.getProperty(
                    JsonRpcProperties.JSONRPC_PREFIX + ".server.backwards-compatible", Boolean.class, true);
            boolean rethrowExceptions = environment
                    .getProperty(JsonRpcProperties.JSONRPC_PREFIX + ".server.rethrow-exceptions", Boolean.class, false);
            boolean allowExtraParams = environment
                    .getProperty(JsonRpcProperties.JSONRPC_PREFIX + ".server.allow-extra-params", Boolean.class, false);
            boolean allowLessParams = environment
                    .getProperty(JsonRpcProperties.JSONRPC_PREFIX + ".server.allow-less-params", Boolean.class, false);
            boolean shouldLogInvocationErrors = environment.getProperty(
                    JsonRpcProperties.JSONRPC_PREFIX + ".server.should-log-invocation-errors", Boolean.class, true);
            String contentType = environment.getProperty(JsonRpcProperties.JSONRPC_PREFIX + ".server.content-type",
                    String.class, JsonRpcBasicServer.JSONRPC_CONTENT_TYPE);
            long parallelBatchProcessingTimeout = environment.getProperty(
                    JsonRpcProperties.JSONRPC_PREFIX + ".server.parallel-batch-processing-timeout", Long.class,
                    Duration.ofSeconds(30).toMillis());

            serviceExporter.setBackwardsCompatible(backwardsCompatible);
            serviceExporter.setAllowLessParams(allowLessParams);
            serviceExporter.setAllowExtraParams(allowExtraParams);
            serviceExporter.setRethrowExceptions(rethrowExceptions);
            serviceExporter.setShouldLogInvocationErrors(shouldLogInvocationErrors);
            serviceExporter.setParallelBatchProcessingTimeout(parallelBatchProcessingTimeout);
            serviceExporter.setContentType(contentType);
            serviceExporter.setRegisterTraceInterceptor(registerTraceInterceptor);

            serviceExporter.setObjectMapper(objectMapper.getIfAvailable(ObjectMapper::new));
            errorResolver.ifAvailable(serviceExporter::setErrorResolver);
            invocationListener.ifAvailable(serviceExporter::setInvocationListener);
            convertedParameterTransformer.ifAvailable(serviceExporter::setConvertedParameterTransformer);
            httpStatusCodeProvider.ifAvailable(serviceExporter::setHttpStatusCodeProvider);
            batchExecutorService.ifAvailable(serviceExporter::setBatchExecutorService);

            List<JsonRpcInterceptor> jsonRpcInterceptors = jsonRpcInterceptor.orderedStream()
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(jsonRpcInterceptors)) {
                serviceExporter.setInterceptorList(jsonRpcInterceptors);
            }

            this.customize(serviceExporter, customizers);

            return serviceExporter;
        }

        private void customize(AutoJsonRpcServiceImplExporter exporter,
                List<AutoJsonRpcServiceImplExporterCustomizer> customizers) {
            for (AutoJsonRpcServiceImplExporterCustomizer customizer : customizers) {
                customizer.customize(exporter);
            }
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AutoJsonRpcClientProxyCreator.class)
    @ConditionalOnProperty(prefix = JsonRpcProperties.JSONRPC_PREFIX, name = "client.enabled", matchIfMissing = false)
    static class JsonRpcAnnotationClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public AutoJsonRpcClientProxyCreator autoJsonRpcClientProxyCreator(
                ApplicationContext applicationContext,
                ObjectProvider<ObjectMapper> objectMapper) {
            AutoJsonRpcClientProxyCreator autoJsonRpcClientProxyCreator = new AutoJsonRpcClientProxyCreator();

            Environment environment = applicationContext.getEnvironment();

            String scanPackage = environment.getProperty(JsonRpcProperties.JSONRPC_PREFIX + ".client.scan-package");
            String baseUrl = environment.getProperty(JsonRpcProperties.JSONRPC_PREFIX + ".client.base-url");
            String contentType = environment.getProperty(JsonRpcProperties.JSONRPC_PREFIX + ".client.content-type");

            Assert.hasText(scanPackage, "JsonRpcClient scanPackage must not be null.");
            Assert.hasText(baseUrl, "JsonRpcClient baseUrl must not be null.");

            autoJsonRpcClientProxyCreator.setScanPackage(scanPackage);
            autoJsonRpcClientProxyCreator.setBaseUrl(this.resolveBaseUrl(baseUrl));
            autoJsonRpcClientProxyCreator.setContentType(contentType);

            autoJsonRpcClientProxyCreator.setObjectMapper(objectMapper.getIfAvailable(ObjectMapper::new));

            return autoJsonRpcClientProxyCreator;
        }

        private URL resolveBaseUrl(String baseUrl) {
            try {
                return new URL(baseUrl);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("JsonRpcClient baseUrl is illegal.", ex);
            }
        }

    }

}
