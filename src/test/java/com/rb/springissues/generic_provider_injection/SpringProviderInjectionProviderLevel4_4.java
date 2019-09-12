package com.rb.springissues.generic_provider_injection;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.context.annotation.ScopedProxyMode.NO;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import lombok.Setter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public class SpringProviderInjectionProviderLevel4_4 implements Provider<String> {

    public static final String LEVEL4 = "provider_level4_4";

    @Inject
    @Named(SpringProviderInjectionProviderLevel3_1.LEVEL3)
    @Setter
    private Provider<String> level1Provider;

    @Bean(name = LEVEL4)
    @Scope(value = SCOPE_PROTOTYPE, proxyMode = NO)
    @Override
    public String get() {
        return level1Provider.get() + " - level 4";
    }
}
