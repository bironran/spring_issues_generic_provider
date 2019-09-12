package com.rb.springissues.generic_provider_injection;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.context.annotation.ScopedProxyMode.NO;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import lombok.Setter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public class SpringProviderInjectionProviderLevel5_4 implements Provider<String> {

    public static final String LEVEL5 = "provider_level5_4";

    @Inject
    @Named(SpringProviderInjectionProviderLevel4_1.LEVEL4)
    @Setter
    private Provider<String> level1Provider;

    @Bean(name = LEVEL5)
    @Scope(value = SCOPE_PROTOTYPE, proxyMode = NO)
    @Override
    public String get() {
        return level1Provider.get() + " - level 5";
    }
}
