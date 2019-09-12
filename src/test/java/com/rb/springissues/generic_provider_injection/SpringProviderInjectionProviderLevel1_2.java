package com.rb.springissues.generic_provider_injection;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.context.annotation.ScopedProxyMode.NO;

import javax.inject.Provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public class SpringProviderInjectionProviderLevel1_2 implements Provider<String> {

    public static final String LEVEL1 = "provider_level1_2";

    @Bean(name = LEVEL1)
    @Scope(value = SCOPE_PROTOTYPE, proxyMode = NO)
    @Override
    public String get() {
        return "level 1";
    }
}
