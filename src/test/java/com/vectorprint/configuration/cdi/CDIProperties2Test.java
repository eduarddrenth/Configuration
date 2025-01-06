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

import com.vectorprint.configuration.NoValueException;
import jakarta.enterprise.inject.spi.CDI;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author eduard
 */
@ExtendWith(WeldJunit5Extension.class)
public class CDIProperties2Test {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(WeldInitiator.createWeld()
            .enableDiscovery().scanClasspathEntries()
    );

    private File props = new File("src/test/resources/test.properties");
    private File propsnew = new File("src/test/resources/testnew.properties");
    private File propsorig = new File("src/test/resources/testorig.properties");

    @AfterEach
    public void init() throws IOException {
        Files.copy(propsorig.toPath(), props.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    @Test
    public void testCDIPropsAppScope() throws InterruptedException, IOException {
        final TestBeanAppScope testBeanAppScope = CDI.current().select(TestBeanAppScope.class).get();
        assertEquals("fieldprop", testBeanAppScope.getFieldprop());
        assertEquals("paramprop", testBeanAppScope.getParamprop());
        assertEquals("fieldpropro", testBeanAppScope.getFieldpropro());
        assertEquals("parampropkey", testBeanAppScope.getParampropkey());
        assertEquals("parampropkey", testBeanAppScope.getParampropkey2());
        assertEquals("paramprop", testBeanAppScope.getProperties().getProperty(null,"setParamprop"));

        Files.write(props.toPath(), Files.readAllBytes(propsnew.toPath()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Thread.sleep(1100);

        assertEquals("fieldprop", testBeanAppScope.getFieldprop());
        assertEquals("ppUpdated", testBeanAppScope.getParamprop());
        assertEquals("fieldpropro", testBeanAppScope.getFieldpropro());
        assertEquals("ppkUpdated", testBeanAppScope.getParampropkey());
        assertEquals("ppkUpdated", testBeanAppScope.getParampropkey2());
        assertEquals("ppUpdated", testBeanAppScope.getProperties().getProperty(null,"setParamprop"));
        assertNull(testBeanAppScope.getKey());
    }

    @Test
    public void testNovalue() throws InterruptedException, IOException {
        final TestNovalue testNovalue = CDI.current().select(TestNovalue.class).get();
        assertThrows(NoValueException.class,() -> testNovalue.getNoValue());
    }

    @Test
    public void testMultiarg() throws InterruptedException, IOException {
        final TestMultiarg testMultiarg = CDI.current().select(TestMultiarg.class).get();
        assertThrows(IllegalStateException.class,() -> testMultiarg.getMultiarg());
    }
    
}
