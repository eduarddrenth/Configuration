/*
 * Copyright 2018 Fryske Akademy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vectorprint.configuration.cdi;

import com.vectorprint.configuration.EnhancedMap;
import jakarta.ejb.Local;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;

/**
 *
 * @author eduard
 */
@Singleton
@Local(TestBean.class)
public class TestBean {

    @Inject
    @Property(keys = "prop", required = true)
    private String testProp;
    @Inject
    @Property(keys = "fprop", required = true)
    private File fprop;
    @Inject
    @Property(required = true)
    private boolean bprop;
    @Inject
    @Property(keys = "zprop", required = false)
    private String zprop;

    @Inject
    @Property(keys = "i", defaultValue = "1", updatable = false)
    private int i;

    @Inject
    @Properties
    private EnhancedMap properties;

    public EnhancedMap getProperties() {
        return properties;
    }

    /*
        trick to be able to verify method parameter injection and update are working
        An injected Testbean in the test class itself doesn't get updated
         */
    private static boolean bp;

    private static String s;

    @Inject
    public void setS(@Property(required = false) String s) {
        TestBean.s = s;
    }

    public static String getS() {
        return s;
    }

    @Inject
    public void setSome(@Property Boolean bprop) {
        bp = bprop;
    }

    public String getTestProp() {
        return testProp;
    }

    public File getFprop() {
        return fprop;
    }

    public boolean isBprop() {
        return bprop;
    }

    public String getZprop() {
        return zprop;
    }

    public int getI() {
        return i;
    }

    public boolean isBp() {
        return bp;
    }
}
