package com.vectorprint.configuration.cdi;

import com.vectorprint.configuration.EnhancedMap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;


public class WeldJUnit4Runner extends BlockJUnit4ClassRunner {

    private final Class<?> klass;
    private final Weld weld;
    private final WeldContainer container;

    public WeldJUnit4Runner(final Class<Object> klass) throws InitializationError {
        super(klass);
        this.klass = klass;
        this.weld = new Weld();
        this.container = weld
                .addPackages(PropertyLocationProvider.class.getPackage())
                .addBeanClass(CDIProperties.class).addBeanClass(EnhancedMap.class).addBeanClass(PropertyResolver.class)
                .addBeanClass(PropertyLocationProvider.class).addBeanClass(RequiredInterceptor.class)
                .initialize();
    }

    @Override
    protected Object createTest() throws Exception {
        final Object test = container.instance().select(klass).get();

        return test;
    }
}