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

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

    @AfterEach
    public void init() throws IOException {
        Files.copy(propsorig.toPath(), props.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testCDIProps() throws InterruptedException, IOException {
        final TestBean testBean = CDI.current().select(TestBean.class).get();
        assertEquals("fieldprop", testBean.getFieldprop());
        assertEquals("paramprop", testBean.getParamprop());
        assertEquals("fieldpropro", testBean.getFieldpropro());
        assertEquals("parampropkey", testBean.getParampropkey());

        Files.write(props.toPath(), Files.readAllBytes(propsnew.toPath()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Thread.sleep(1100);

        assertEquals("fpUpdated", testBean.getFieldprop());
        assertEquals("ppUpdated", testBean.getParamprop());
        assertEquals("fieldpropro", testBean.getFieldpropro());
        assertEquals("ppkUpdated", testBean.getParampropkey());
    }
}
