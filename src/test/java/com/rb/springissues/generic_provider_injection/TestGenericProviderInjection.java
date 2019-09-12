package com.rb.springissues.generic_provider_injection;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestGenericProviderInjection {

    public static final String PROVIDER_CLASS_NAME_GENERIC =
            "com.rb.springissues.generic_provider_injection.SpringProviderInjectionProviderLevel";

    @Test
    public void testRegularAppContext() {
        AtomicLong counter = new AtomicLong();
        Supplier<DefaultListableBeanFactory> beanFactorySupplier = () -> new DefaultListableBeanFactory() {
            @Override
            public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
                    throws NoSuchBeanDefinitionException {
                counter.incrementAndGet();
                return super.isAutowireCandidate(beanName, descriptor);
            }
        };
        System.out.println(executeTest(counter, beanFactorySupplier));
        /*
            height: 1, width: 1, complexity: 0
            height: 1, width: 2, complexity: 0
            height: 1, width: 3, complexity: 0
            height: 1, width: 4, complexity: 0
            height: 1, width: 5, complexity: 0
            height: 2, width: 1, complexity: 1
            height: 2, width: 2, complexity: 3
            height: 2, width: 3, complexity: 5
            height: 2, width: 4, complexity: 7
            height: 2, width: 5, complexity: 9
            height: 3, width: 1, complexity: 4
            height: 3, width: 2, complexity: 10
            height: 3, width: 3, complexity: 16
            height: 3, width: 4, complexity: 22
            height: 3, width: 5, complexity: 28
            height: 4, width: 1, complexity: 9
            height: 4, width: 2, complexity: 21
            height: 4, width: 3, complexity: 33
            height: 4, width: 4, complexity: 45
            height: 4, width: 5, complexity: 57
            height: 5, width: 1, complexity: 16
            height: 5, width: 2, complexity: 36
            height: 5, width: 3, complexity: 56
            height: 5, width: 4, complexity: 76
            height: 5, width: 5, complexity: 96
         */
    }

    @Test
    public void testCachingAppContext() {
        AtomicLong counter = new AtomicLong();
        Supplier<AutoWireFriendlyDefaultListableBeanFactory> beanFactorySupplier =
                () -> new AutoWireFriendlyDefaultListableBeanFactory() {
                    @Override
                    public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
                            throws NoSuchBeanDefinitionException {
                        counter.incrementAndGet();
                        return super.isAutowireCandidate(beanName, descriptor);
                    }
                };
        System.out.println(executeTest(counter, beanFactorySupplier));
        /*
            height: 1, width: 1, complexity: 0
            height: 1, width: 2, complexity: 0
            height: 1, width: 3, complexity: 0
            height: 1, width: 4, complexity: 0
            height: 1, width: 5, complexity: 0
            height: 2, width: 1, complexity: 0
            height: 2, width: 2, complexity: 0
            height: 2, width: 3, complexity: 0
            height: 2, width: 4, complexity: 0
            height: 2, width: 5, complexity: 0
            height: 3, width: 1, complexity: 0
            height: 3, width: 2, complexity: 0
            height: 3, width: 3, complexity: 0
            height: 3, width: 4, complexity: 0
            height: 3, width: 5, complexity: 0
            height: 4, width: 1, complexity: 0
            height: 4, width: 2, complexity: 0
            height: 4, width: 3, complexity: 0
            height: 4, width: 4, complexity: 0
            height: 4, width: 5, complexity: 0
            height: 5, width: 1, complexity: 0
            height: 5, width: 2, complexity: 0
            height: 5, width: 3, complexity: 0
            height: 5, width: 4, complexity: 0
            height: 5, width: 5, complexity: 0
         */
    }

    private StringBuilder executeTest(AtomicLong counter,
            Supplier<? extends DefaultListableBeanFactory> countingBeanFactorySupplier) {
        StringBuilder sb = new StringBuilder();
        for (int height = 1; height <= 5; height++) {
            for (int width = 1; width <= 5; width++) {
                AnnotationConfigApplicationContext appContext = buildApplicationContext(width, height,
                        countingBeanFactorySupplier.get());
                //prime cache
                appContext.getBean("provider_level" + height + "_1", String.class);
                //execute runtime
                counter.set(0);
                appContext.getBean("provider_level" + height + "_1", String.class);
                sb.append("height: " + height + ", width: " + width + ", complexity: " + counter.get() + "\n");
            }
        }
        return sb;
    }

    private AnnotationConfigApplicationContext buildApplicationContext(int width, int depth,
            DefaultListableBeanFactory beanFactory) {
        if (depth > 5) {
            throw new IllegalArgumentException("depth must be 5 or less");
        }

        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext(beanFactory);
        //too lazy to rename classes to level0
        for (int i = 1; i <= depth; i++) {
            for (int j = 1; j <= width; j++) {
                GenericBeanDefinition beanDef = new GenericBeanDefinition();
                beanDef.setBeanClassName(PROVIDER_CLASS_NAME_GENERIC + i + "_" + j);
                appContext.registerBeanDefinition("providerBean_" + j + "_level_" + i, beanDef);
            }
        }

        appContext.refresh();
        return appContext;
    }
}
