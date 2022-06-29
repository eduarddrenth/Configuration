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

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * @author eduard
 */
@EnableWeld
public class CDIPropertiesTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(WeldInitiator.createWeld()
            .enableDiscovery().scanClasspathEntries()
            .beanClasses(TestBean.class)
    );

    private File props = new File("src/test/resources/test.properties");
    private File propsnew = new File("src/test/resources/testnew.properties");
    private File propsorig = new File("src/test/resources/testorig.properties");

    @Inject
    @SingTestBean
    private TestBean testBean;

    @AfterEach
    public void init() throws IOException {
        Files.copy(propsorig.toPath(), props.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testCDIProps() throws InterruptedException, IOException {
        assertEquals("test", testBean.getTestProp());
        assertEquals("src/test/resources/test.properties", testBean.getFprop().getPath());
        assertEquals(true, testBean.isBprop());
        assertEquals(true, testBean.isBp());
        assertEquals(true, testBean.getProperties().getBooleanProperty(null, "bprop"));
        assertNull(testBean.getZprop());
        assertNull(testBean.getS());
        assertEquals(1, testBean.getI());

        Files.write(props.toPath(), Files.readAllBytes(propsnew.toPath()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Thread.sleep(1100);

        assertEquals(false, testBean.isBp());
        assertEquals(false, testBean.isBprop());
        assertEquals("s", testBean.getS());
        assertEquals(false, testBean.getProperties().getBooleanProperty(null, "bprop"));
        assertEquals(1, testBean.getI());

    }
}
