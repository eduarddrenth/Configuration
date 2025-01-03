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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


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
        assertEquals("test", testBeanAppScope.getTestProp());
        assertEquals("src/test/resources/test.properties", testBeanAppScope.getFprop().getPath());
        assertEquals(true, testBeanAppScope.isBprop());
        assertEquals(true, testBeanAppScope.isBp());
        assertEquals(true, testBeanAppScope.getProperties().getBooleanProperty(null, "bprop"));
        assertNull(testBeanAppScope.getZprop());
        assertNull(testBeanAppScope.getS());
        assertEquals(1, testBeanAppScope.getI());

        Files.write(props.toPath(), Files.readAllBytes(propsnew.toPath()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Thread.sleep(5000);

        System.out.println("NOTE: UPDATING INJECTED PROPERTIES ONLY WORKS FOR STATICS, for method parameters, for @Properties AND IN SINGLETON BEANS!!");
        assertEquals("test2", testBeanAppScope.getTestProp());
        assertEquals(false, testBeanAppScope.isBp());
        assertEquals(true, testBeanAppScope.isBprop(),"SHOULD BE false, CHANGE THIS assertion!!!");
        assertEquals(false, testBeanAppScope.getProperties().getBooleanProperty(null, "bprop"));
        assertEquals("s", testBeanAppScope.getS());
    }

}
