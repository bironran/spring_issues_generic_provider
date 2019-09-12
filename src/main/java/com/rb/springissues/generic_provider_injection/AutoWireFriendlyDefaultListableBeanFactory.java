package com.rb.springissues.generic_provider_injection;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.google.common.collect.Maps;

public class AutoWireFriendlyDefaultListableBeanFactory extends DefaultListableBeanFactory {
    private Map<String, String> beanToProviderFactories = new ConcurrentHashMap<>();
    public static final String NO_FACTORY = "!!!NO_FACTORY!!!;";
    public static final String MANY_FACTORIES = "!!!MANY_FACTORIES!!!;";

    public AutoWireFriendlyDefaultListableBeanFactory() {
    }

    public AutoWireFriendlyDefaultListableBeanFactory(BeanFactory parentBeanFactory) {
        super(parentBeanFactory);
    }

    @Override
    protected Map<String, Object> findAutowireCandidates(String beanName, Class<?> requiredType,
            DependencyDescriptor descriptor) {
        String qualifierName = getNamedAnnotationFromDescriptor(descriptor);
        String cachedProvider;
        if(qualifierName != null) {
            cachedProvider = beanToProviderFactories.get(qualifierName);
        } else {
            cachedProvider = null;
        }

        //noinspection StringEquality
        if(cachedProvider == NO_FACTORY) {
            return Collections.emptyMap();
        }

        //noinspection StringEquality
        if(cachedProvider == MANY_FACTORIES) {
            return super.findAutowireCandidates(beanName, requiredType, descriptor);
        }

        if(cachedProvider != null) {
            Map<String, Object> result = Maps.newHashMapWithExpectedSize(1);
            result.put(cachedProvider, getBean(cachedProvider));
            return result;
        }

        if (isConfigurationFrozen() && descriptor != null && descriptor.getField() != null &&
                Objects.equals(descriptor.getField().getType(), Provider.class) &&
                descriptor.getAnnotations() != null && hasNamedAnnotation(descriptor.getAnnotations())) {
            String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                    this, requiredType, true, descriptor.isEager());
            List<String> filteredCandidates = new ArrayList<>();
            for (String candidateName : candidateNames) {
                if (!isSelfReference(beanName, candidateName) && isAutowireCandidate(candidateName, descriptor)) {
                    filteredCandidates.add(candidateName);
                }
            }
            if (filteredCandidates.isEmpty()) {
                DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
                for (String candidateName : candidateNames) {
                    if (!candidateName.equals(beanName) && isAutowireCandidate(candidateName, fallbackDescriptor)) {
                        filteredCandidates.add(candidateName);
                    }
                }
            }

            if (filteredCandidates.size() == 1) {
                String provider = filteredCandidates.get(0);
                beanToProviderFactories.put(qualifierName, provider);
                Map<String, Object> result = Maps.newHashMapWithExpectedSize(1);
                result.put(provider, getBean(provider));
                return result;
            }

            if(filteredCandidates.size() == 0) {
                beanToProviderFactories.put(qualifierName, NO_FACTORY);
                return Collections.emptyMap();
            }

            //noinspection ConstantConditions
            if(filteredCandidates.size() > 1) {
                beanToProviderFactories.put(qualifierName, MANY_FACTORIES);
                return super.findAutowireCandidates(beanName, requiredType, descriptor);
            }
        }

        //noinspection ConstantConditions
        return super.findAutowireCandidates(beanName, requiredType, descriptor);
    }

    private String getNamedAnnotationFromDescriptor(DependencyDescriptor descriptor) {
        if(descriptor != null && descriptor.getAnnotations() != null) {
            for (Annotation annotation : descriptor.getAnnotations()) {
                if (Objects.equals(annotation.annotationType(), Named.class)) {
                    Named named = (Named) annotation;
                    return named.value();
                }
            }
            return null;
        }
        return null;
    }

    private boolean hasNamedAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (Objects.equals(annotation.annotationType(), Named.class)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSelfReference(String beanName, String candidateName) {
        return (beanName != null && candidateName != null &&
                (beanName.equals(candidateName) || (containsBeanDefinition(candidateName) &&
                        beanName.equals(getMergedLocalBeanDefinition(candidateName).getFactoryBeanName()))));
    }
}
