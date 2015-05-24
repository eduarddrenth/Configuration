/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration;

/*
 * #%L
 * VectorPrintConfig3.0
 * %%
 * Copyright (C) 2011 - 2013 VectorPrint
 * %%
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
 * #L%
 */
import com.vectorprint.ArrayHelper;
import com.vectorprint.ClassHelper;
import com.vectorprint.testing.ThreadTester;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.BindingHelperImpl;
import com.vectorprint.configuration.decoration.AbstractPropertiesDecorator;
import com.vectorprint.configuration.decoration.CachingProperties;
import com.vectorprint.configuration.decoration.Changes;
import com.vectorprint.configuration.decoration.FindableProperties;
import com.vectorprint.configuration.decoration.HelpSupportedProperties;
import com.vectorprint.configuration.decoration.Observable;
import com.vectorprint.configuration.decoration.ObservableProperties;
import com.vectorprint.configuration.decoration.Observer;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.decoration.PreparingProperties;
import com.vectorprint.configuration.decoration.ReadonlyProperties;
import com.vectorprint.configuration.decoration.ThreadSafeProperties;
import com.vectorprint.configuration.decoration.visiting.ParsingVisitor;
import com.vectorprint.configuration.observing.HandleEmptyValues;
import com.vectorprint.configuration.observing.PrepareKeyValue;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.CharPasswordParameter;
import com.vectorprint.configuration.parameters.FloatArrayParameter;
import com.vectorprint.configuration.parameters.IntArrayParameter;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parameters.PasswordParameter;
import com.vectorprint.configuration.parser.ParameterizableParserImpl;
import com.vectorprint.configuration.parser.ParseException;
import com.vectorprint.configuration.binding.parameters.EscapingBindingHelper;
import com.vectorprint.configuration.binding.parameters.json.JSONSupport;
import com.vectorprint.configuration.binding.parameters.ParameterHelper;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.configuration.binding.parameters.ParameterizableParser;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl;
import com.vectorprint.configuration.binding.parameters.ParameterizableSerializer;
import com.vectorprint.configuration.binding.parameters.json.JSONBindingHelper;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactoryImpl;
import com.vectorprint.configuration.binding.settings.EnhancedMapParser;
import com.vectorprint.configuration.parser.PropertiesParser;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class PropertyTest {

   @BeforeClass
   public static void setUpClass() throws IOException {
      Logger.getLogger(Settings.class.getName()).setLevel(Level.FINE);
   }

   @Test
   public void testMultiThreadProps() throws Exception {
      final PropCreator pc = new PropCreator();
      ThreadTester.testInThread(pc);
      Collection<Runnable> toRun = new HashSet<Runnable>(1);
      toRun.add(new Runnable() {
         @Override
         public void run() {
            try {
               assertFalse(pc.mtp.containsKey("stoponerror"));
               fail("properties should not be reached in sibling threads");
            } catch (VectorPrintRuntimeException ex) {
               // expected
            }
         }
      });
      ThreadTester.testInThread(toRun);
      try {
         assertNull(pc.mtp.get("stoponerror"));
         fail("properties should not be reached in sibling threads");
      } catch (VectorPrintRuntimeException ex) {
         // expected
      }
   }

   private class PropCreator implements Runnable {

      private ThreadSafeProperties mtp;

      @Override
      public void run() {
         try {
            mtp = new ThreadSafeProperties(new ParsingProperties(new Settings(), "src/test/resources/config"
                + File.separator + "run.properties"));
            assertTrue(mtp.containsKey("stoponerror"));
            assertEquals("true", mtp.getProperty("stoponerror"));
            assertTrue(mtp.getBooleanProperty(false, "stoponerror"));
         } catch (IOException ex) {
            Logger.getLogger(PropertyTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (VectorPrintRuntimeException ex) {
            Logger.getLogger(PropertyTest.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   @Test
   public void testGetArrayProps() throws IOException {
      ParsingProperties mtp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties", "src/test/resources/config"
          + File.separator + "run.properties");
      assertEquals(true, mtp.getColorProperties(null, "markcolors").length > 0);
      assertEquals(true, mtp.getDoubleProperties(null, "marks").length > 0);
      assertEquals(true, mtp.getFloatProperties(null, "marks").length > 0);
      assertEquals(true, mtp.getIntegerProperties(null, "marks").length > 0);
      assertEquals(true, mtp.getLongProperties(null, "marks").length > 0);
      assertEquals(true, mtp.getBooleanProperties(null, "bol").length > 0);
      assertEquals(true, mtp.getColorProperties(new Color[]{}, "notthere").length == 0);
      assertEquals(true, mtp.getDoubleProperties(new double[]{}, "notthere").length == 0);
      assertEquals(true, mtp.getFloatProperties(new float[]{}, "notthere").length == 0);
      assertEquals(true, mtp.getIntegerProperties(new int[]{}, "notthere").length == 0);
      assertEquals(true, mtp.getLongProperties(new long[]{}, "notthere").length == 0);
      assertEquals(true, mtp.getBooleanProperties(new boolean[]{}, "notthere").length == 0);
      try {
         mtp.getLongProperties(null, "notthere");
         fail("exception expected");
      } catch (VectorPrintRuntimeException ex) {
         //expected
      }
   }

   @Test
   public void testGetProps() throws IOException {
      ParsingProperties mtp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties");
      assertEquals(true, mtp.getDoubleProperty(null, "alpha") == 200);
      assertEquals(true, mtp.getFloatProperty(null, "alpha") == 200);
      assertEquals(true, mtp.getIntegerProperty(null, "alpha") == 200);
      assertEquals(true, mtp.getLongProperty(null, "alpha") == 200);
      assertEquals(true, mtp.getDoubleProperty(100d, "alpha") == 200);
      assertEquals(true, mtp.getFloatProperty(100f, "alpha") == 200);
      assertEquals(true, mtp.getIntegerProperty(100, "alpha") == 200);
      assertEquals(true, mtp.getLongProperty(100l, "alpha") == 200);
      assertEquals(true, mtp.getDoubleProperty(100d, "notthere") == 100);
      assertEquals(true, mtp.getFloatProperty(100f, "notthere") == 100);
      assertEquals(true, mtp.getIntegerProperty(100, "notthere") == 100);
      assertEquals(true, mtp.getLongProperty(100l, "notthere") == 100);
      try {
         mtp.getLongProperties(null, "notthere");
         fail("exception expected");
      } catch (VectorPrintRuntimeException ex) {
         //expected
      }
   }

   @Test
   public void testMultipleKeys() throws IOException, ParseException {
      EnhancedMap eh = new Settings();
      eh.put("k", "v");
      assertEquals("v", eh.getGenericProperty(null, String.class, "a", "b", "k"));
      try {
         eh.getGenericProperty(null, String.class, "a", "b");
      } catch (VectorPrintRuntimeException e) {
         // expected
      }
   }

   @Test
   public void testHelp() throws IOException, ParseException {
      EnhancedMap mtp = new HelpSupportedProperties(new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties"), new URL("file:src/test/resources/help.properties"));
      assertEquals("wat een mooie\nhelp tekst\n\nis dit", mtp.getHelp("stoponerror").getExplanation());
      assertEquals("boolean", mtp.getHelp("stoponerror").getType());
   }

   @Test
   public void testChangeProperty() throws IOException, ParseException {

      ParsingProperties mtp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties", "src/test/resources/config"
          + File.separator + "run.properties");

      String marks = mtp.get("marks")[0];
      assertNotNull(marks);
      mtp.put("marks", marks + "_nieuwe waarde");
      assertFalse(marks.equals(mtp.get("marks")[0]));
   }

   static class MyObserver implements Observer {

      private Changes changes = null;

      @Override
      public void update(Observable object, Changes changes) {
         this.changes = changes;
      }
   }

   @Test
   public void testObserving() throws IOException, ParseException {

      ObservableProperties mtp = new ObservableProperties(new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties"));

      MyObserver os = new MyObserver();
      mtp.addObserver(os);

      String marks = mtp.get("marks")[0];
      mtp.put("marks", marks + "_nieuwe waarde");
      assertTrue(os.changes.getChanged().contains("marks"));

      mtp.put("testerdetest", "_nieuwe waarde");
      assertNull(os.changes.getChanged());
      assertTrue(os.changes.getAdded().contains("testerdetest"));

      Map<String, String[]> mm = new HashMap<String, String[]>(2);
      mm.put("marks", new String[]{"weerveranderd"});
      mm.put("nogeennieuwe", new String[]{"bla"});
      mtp.putAll(mm);
      assertTrue(os.changes.getAdded().contains("nogeennieuwe"));
      assertTrue(os.changes.getChanged().contains("marks"));

      mtp.remove("marks");
      assertTrue(os.changes.getDeleted().contains("marks"));

      mtp.clear();
      assertTrue(os.changes.getDeleted().contains("testerdetest"));
   }

   @Test
   public void testSplitValues() throws IOException, ParseException {

      EnhancedMap mtp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties");
      EnhancedMapParser parser = EnhancedMapBindingFactoryImpl.getFactory(PropertiesParser.class, PropertiesParser.class,new BindingHelperImpl()).getParser(new StringReader("a=c;d\nb=c\\;d"));
      parser.parse(mtp);
      assertEquals(2, mtp.getStringProperties(null, "a").length);
      assertEquals(1, mtp.getStringProperties(null, "b").length);
      assertEquals("c", mtp.getStringProperties(null, "a")[0]);
      assertEquals("c;d", mtp.getStringProperties(null, "b")[0]);

      // test config using one backslash for escaping
      assertEquals(2, mtp.getStringProperties(null, "splittest").length);
   }
   @Test
   public void testRemoveProperty() throws IOException, ParseException {
      ParsingProperties mtp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties", "src/test/resources/config"
          + File.separator + "run.properties");

      String[] marks = mtp.get("marks");
      assertNotNull(marks);
      mtp.remove("marks");
      assertNull(mtp.get("marks"));
   }

   @Test
   public void testSave() throws IOException, ParseException {
      ParsingProperties mtp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties");
      assertFalse(mtp.containsKey("m"));
//      mtp.addFromArguments(new String[]{"-m"});
//      assertTrue(mtp.containsKey("m"));

      File f = File.createTempFile("test", "props");

      System.out.println(f.toURI().toURL());
      mtp.saveToUrl(f.toURI().toURL());

      mtp = new ParsingProperties(new Settings(), f.getPath());
//      assertTrue(mtp.containsKey("m"));
      assertTrue(mtp.getTrailingComment().get(1).contains("bla"));
      assertTrue(mtp.getCommentBeforeKey("diameter").get(0).contains("To change"));
   }

   @Test
   public void testCombineProps() throws IOException, AssertionError, RuntimeException, InterruptedException, ParseException {
      ParsingProperties mp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties", "src/test/resources/config"
          + File.separator + "run.properties");
      final ThreadSafeProperties mtp = new ThreadSafeProperties(mp);
      assertNotNull(mtp.getProperty("stoponerror"));
      assertNotNull(mtp.getProperty("marks"));
      assertNotNull(mtp.getProperty("stoponerror"));
      assertTrue(mtp.containsValue(new String[]{"true"}));
      assertTrue(mtp.values().contains(new String[]{"7"}));
      assertTrue(mtp.keySet().contains("stoponerror"));
      assertTrue(mtp.keySet().contains("marks"));
      ThreadTester.testInThread(new Runnable() {
         @Override
         public void run() {
            assertNotNull(mtp.getProperty("stoponerror"));
         }
      });
   }

   @Test
   public void testIndependenceThreads() throws IOException, AssertionError, RuntimeException, InterruptedException, ParseException {
      ParsingProperties mp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties", "src/test/resources/config"
          + File.separator + "run.properties");
      final ThreadSafeProperties mtp = new ThreadSafeProperties(mp);
      assertFalse(mtp.containsKey("bla"));
      mtp.put("bla", "bla");
      ThreadTester.testInThread(new Runnable() {
         @Override
         public void run() {
            mtp.put("bla", "ookbla");
         }
      });
      assertEquals("ookbla", mtp.getProperty("bla"));
      assertTrue(mtp.containsKey("stoponerror"));
   }

   @Test
   public void testHandleEmptyValues() throws IOException, ParseException {
      new ParsingProperties(new Settings(), "src/test/resources/config" + File.separator + "styling.properties");

      HandleEmptyValues emtiesOK = new HandleEmptyValues(true);
      HandleEmptyValues emtiesNOTOK = new HandleEmptyValues(false);

      List<PrepareKeyValue<String, String[]>> observers = new LinkedList<PrepareKeyValue<String, String[]>>();
      observers.add(emtiesOK);

      try {
         new PreparingProperties(new ParsingProperties(new Settings(), "src/test/resources/config" + File.separator + "styling.properties"), observers);
      } catch (VectorPrintRuntimeException ex) {
         fail("no excption expected: " + ex.getMessage());
      }

      observers.clear();

      observers.add(emtiesNOTOK);

      try {
         new ParsingProperties(new PreparingProperties(new Settings(), observers), "src/test/resources/config" + File.separator + "styling.properties");
         fail("excption expected for empty value ");
      } catch (VectorPrintRuntimeException ex) {
      }

      emtiesNOTOK.addKeyToSkip("empty").addKeyToSkip("klantlogo");
      try {
         new PreparingProperties(new ParsingProperties(new Settings(), "src/test/resources/config" + File.separator + "styling.properties"), observers);
      } catch (VectorPrintRuntimeException ex) {
         fail("no excption expected: " + ex.getMessage());
      }
   }

//   @Test
//   public void testArguments() throws IOException, ParseException {
//      ParsingProperties vp = new ParsingProperties(new Settings(), "src/test/resources/config"
//          + File.separator + "styling.properties");
//      vp.addFromArguments(new String[]{"-t", "-d", "-n", "-m", "m"});
//      assertTrue(vp.containsKey("t"));
//      assertTrue(vp.containsKey("d"));
//      assertTrue(vp.containsKey("n"));
//      assertTrue(vp.containsKey("m"));
//      assertEquals("m", vp.getPropertyNoDefault("m"));
//      vp = new ParsingProperties(new Settings(), "src/test/resources/config" + File.separator + "styling.properties");
//      try {
//         vp.addFromArguments(new String[]{"t", "d", "n", "m", "m"});
//      } catch (VectorPrintRuntimeException e) {
//         assertTrue(e.getMessage().startsWith(ArgumentParser.WRONGKEYMESSAGE));
//      }
//   }
   @Test
   public void testFindProperties() throws IOException, VectorPrintException, ParseException {
      new FindableProperties(new ParsingProperties(new Settings(), "src/test/resources/config" + File.separator + "styling.properties"));
      assertNotNull(FindableProperties.findContains("styling.properties"));
   }

//   @Test
//   public void testTrim() throws IOException, ParseException {
//      PreparingProperties vp = new PreparingProperties(new Settings());
//      vp.addObserver(new TrimKeyValue());
//      vp.addFromArguments(new String[]{"-t ", "-d ", "-n ", "-m ", " m "});
//      assertTrue(vp.containsKey("t"));
//      assertTrue(vp.containsKey("d"));
//      assertTrue(vp.containsKey("n"));
//      assertTrue(vp.containsKey("m"));
//      assertEquals("m", vp.getPropertyNoDefault("m"));
//   }
   @Test
   public void testRecordUnused() throws IOException, ParseException {
      EnhancedMap vp = new ParsingProperties(new Settings(), "src/test/resources/config" + File.separator + "styling.properties");
      assertTrue(vp.getUnusedKeys().contains("small"));
      assertTrue(vp.getUnusedKeys().contains("bigbold"));
      vp.getPropertyNoDefault("small");
      assertFalse(vp.getUnusedKeys().contains("small"));
      assertTrue(vp.getUnusedKeys().contains("bigbold"));
      vp.remove("bigbold");
      assertFalse(vp.getUnusedKeys().contains("small"));
      assertFalse(vp.getUnusedKeys().contains("bigbold"));
      vp.clear();
      String n = null;
      vp.put("small", n);
      vp.keySet().remove("small");
      assertFalse(vp.getUnusedKeys().contains("small"));
   }

   @Test
   public void testRecordNotPresent() throws IOException, ParseException {
      EnhancedMap vp = new ParsingProperties(new Settings(), "src/test/resources/config" + File.separator + "styling.properties");
      assertFalse(vp.getKeysNotPresent().contains("small"));
      assertFalse(vp.getKeysNotPresent().contains("bigbold"));
      vp.getProperty("smalllll");
      assertTrue(vp.getKeysNotPresent().contains("smalllll"));
      vp.remove("bigbold");
      vp.getProperty("bigbold");
      assertTrue(vp.getKeysNotPresent().contains("bigbold"));
      vp.clear();
      assertFalse(vp.getKeysNotPresent().contains("small"));
      assertFalse(vp.getKeysNotPresent().contains("smalllll"));
      assertFalse(vp.getKeysNotPresent().contains("bigbold"));
   }

   @Test
   public void testReadonly() throws IOException, ParseException {
      EnhancedMap vp = new ReadonlyProperties(new ParsingProperties(new Settings(), "src/test/resources/config" + File.separator + "styling.properties"));
      assertTrue(vp.containsKey("bold"));
      try {
         vp.put("not", "possible");
         fail("should be readonly");
      } catch (VectorPrintRuntimeException ex) {
         //expected
      }
      try {
         vp.putAll(vp);
         fail("should be readonly");
      } catch (VectorPrintRuntimeException ex) {
         //expected
      }
      try {
         vp.remove("bold");
         fail("should be readonly");
      } catch (VectorPrintRuntimeException ex) {
         //expected
      }
      try {
         vp.clear();
         fail("should be readonly");
      } catch (VectorPrintRuntimeException ex) {
         //expected
      }
   }

   @Test
   public void testClone() throws IOException, ParseException {
      ParsingProperties mtp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties");
      ParsingProperties clone = (ParsingProperties) mtp.clone();
      assertEquals(clone, mtp);
      assertEquals(clone.getCommentBeforeKey("marks"), mtp.getCommentBeforeKey("marks"));
   }

   @Test
   public void testDecorators() throws IOException, VectorPrintException, ParseException {
      Settings vp = new Settings();
      AbstractPropertiesDecorator mtp = new ThreadSafeProperties(new FindableProperties(new HelpSupportedProperties(new ParsingProperties(vp, "src/test/resources/config"
          + File.separator + "chart.properties"), new URL("file:src/test/resources/help.properties"))));
      assertTrue(mtp.hasProperties(FindableProperties.class));
      assertTrue(mtp.hasProperties(HelpSupportedProperties.class));
      assertTrue(mtp.hasProperties(ThreadSafeProperties.class));
      assertTrue(mtp.hasProperties(ParsingProperties.class));
      assertTrue(mtp.hasProperties(Settings.class));

      assertTrue(vp.getDecorators().get(2).equals(FindableProperties.class));
      assertTrue(vp.getDecorators().get(1).equals(HelpSupportedProperties.class));
      assertTrue(vp.getDecorators().get(3).equals(ThreadSafeProperties.class));
      assertTrue(vp.getDecorators().get(0).equals(ParsingProperties.class));

      assertTrue(vp.getOutermostWrapper() instanceof ThreadSafeProperties);

      try {
         new ThreadSafeProperties(mtp);
         fail("expected exception, decorating with a certain class only allowed once");
      } catch (VectorPrintRuntimeException vectorPrintRuntimeException) {
      }
   }

   @Test
   public void testHiding() {
      try {
         AbstractPropertiesDecorator mtp = new CachingProperties(new ThreadSafeProperties(new Settings()));
         fail("caching hides threadsafety");
      } catch (VectorPrintRuntimeException e) {
         System.out.println(e.getMessage());
      }
   }

   @Test
   public void testCaching() throws IOException, VectorPrintException, ParseException {
      EnhancedMap vp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties");
      EnhancedMap ch = new CachingProperties(vp);

      long start = System.currentTimeMillis();
      // fill cache should be slower
      for (int i = 0; i < 10000; i++) {
         ch.getColorProperties(null, "markcolors");
      }
      long ch1 = System.currentTimeMillis() - start;

      // cache filled should be faster
      start = System.currentTimeMillis();
      for (int i = 0; i < 10000; i++) {
         ch.getColorProperties(null, "markcolors");
      }
      long ch2 = System.currentTimeMillis() - start;

      // cache filled should be faster
      start = System.currentTimeMillis();
      for (int i = 0; i < 10000; i++) {
         ch.getColorProperties(null, "markcolors");
      }
      long ch3 = System.currentTimeMillis() - start;

      vp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties");
      ch = new CachingProperties(vp);

      // don't measure first two passes
      start = System.currentTimeMillis();
      for (int i = 0; i < 10000; i++) {
         vp.getColorProperties(null, "markcolors");
      }
      long vp1 = System.currentTimeMillis() - start;

      start = System.currentTimeMillis();
      for (int i = 0; i < 10000; i++) {
         vp.getColorProperties(null, "markcolors");
      }
      long vp2 = System.currentTimeMillis() - start;

      // second pass will be much faster
      start = System.currentTimeMillis();
      for (int i = 0; i < 10000; i++) {
         vp.getColorProperties(null, "markcolors");
      }
      long vp3 = System.currentTimeMillis() - start;

      assertTrue(String.format("caching not faster: %d <= %d", ch2 + ch3, vp2 + vp3), ch2 + ch3 < vp2 + vp3);

      ch.put("tt", "racing");
      ch.getPropertyNoDefault("tt");
      try {
         ch.getStringProperties(null, "tt");
      } catch (VectorPrintRuntimeException e) {
         assertTrue(e.getMessage().contains("Removed from cache"));
         ch.getStringProperties(null, "tt");
      }

      ch.put("prim", "1");
      ch.getGenericProperty(null, Integer.class, "prim");
      ch.getIntegerProperty(null, "prim");

   }

   @Test
   public void testSerializable() throws IOException, VectorPrintException, ParseException, InterruptedException, ClassNotFoundException {
      FindableProperties.clearStaticReferences();
      EnhancedMap mtp = new ThreadSafeProperties(new FindableProperties(new HelpSupportedProperties(new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties"), new URL("file:src/test/resources/help.properties"))));

      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bo);
      oos.writeObject(mtp);
      oos.close();

      ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray()));

      final EnhancedMap deserialized = (EnhancedMap) oin.readObject();

      assertTrue(mtp.size() == deserialized.size());
      for (Map.Entry<String, String[]> e : mtp.entrySet()) {
         assertArrayEquals(e.getValue(), deserialized.get(e.getKey()));
      }

      assertEquals("wat een mooie\nhelp tekst\n\nis dit", deserialized.getHelp("stoponerror").getExplanation());

      ThreadTester.testInThread(new Runnable() {

         @Override
         public void run() {
            deserialized.put("childThreadProp", "child");
            assertTrue(deserialized.containsKey("childThreadProp"));
         }
      });

      assertTrue(deserialized.containsKey("childThreadProp"));

      deserialized.getGenericProperty(null, Color[].class, "markcolors");
   }

   @Test
   public void testSerializableParameters() throws IOException, VectorPrintException, ParseException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

      for (Class c : ClassHelper.fromPackage(Parameter.class.getPackage())) {
         if (!Modifier.isAbstract(c.getModifiers())) {
            if (ParameterImpl.class.isAssignableFrom(c)) {
               Constructor con = c.getConstructor(String.class, String.class);
               Parameter p = (Parameter) con.newInstance(c.getSimpleName(), "some help");
               if (p.getValueClass().equals(Integer[].class)) {
                  IntArrayParameter ip = (IntArrayParameter) p;
                  ip.setValue(new int[]{1, 2});
               }

               ByteArrayOutputStream bo = new ByteArrayOutputStream();
               ObjectOutputStream oos = new ObjectOutputStream(bo);
               oos.writeObject(p);
               oos.close();

               ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray()));

               final Parameter deserialized = (Parameter) oin.readObject();
               if (p.getValueClass().equals(int[].class)) {
                  IntArrayParameter ip = (IntArrayParameter) p;
                  IntArrayParameter dip = (IntArrayParameter) deserialized;
                  assertArrayEquals((int[]) ip.getValue(), (int[]) deserialized.getValue());
               }

               assertEquals(p.toString(), deserialized.toString());
               assertEquals(p, deserialized);
            }
         }
      }
   }

   private <TYPE extends Serializable> void setVal(Parameter<TYPE> parameter, EnhancedMap settings) {
      ParameterizableParser objectParser = ParameterizableBindingFactoryImpl.getFactory().getParser(new StringReader(""));
      ParameterizableBindingFactoryImpl.getFactory().getBindingHelper().setValueOrDefault(
          parameter,
          (TYPE) objectParser.parseAsParameterValue(settings.getPropertyNoDefault(parameter.getKey()), parameter),
          false);
   }

   @Test
   public void testParameters() throws Exception {
      String[] testStrings = new String[]{"1", "aap", "1|2|aap", "1|2.4", "aap|noot", "http://a.b", "#eeeeee", "a.*(\\;.*\\)", "true", "java.lang.String"};
      IntArrayParameter ia = new IntArrayParameter("k", "h");
      ia.setValue(new int[]{1, 2});
      FloatArrayParameter fa = new FloatArrayParameter("k", "h");
      fa.setValue(new float[]{1f, 1f});
      Settings settings = new Settings();
      for (Class c : ClassHelper.fromPackage(Parameter.class.getPackage())) {
         if (!Modifier.isAbstract(c.getModifiers())) {
            if (ParameterImpl.class.isAssignableFrom(c)) {
               Constructor con = c.getConstructor(String.class, String.class);
               Parameter p = (Parameter) con.newInstance(c.getSimpleName(), "some help");
               Parameter cl = p.clone();
               assertEquals(p, cl);
               assertNotNull(cl.getValueClass());
               if (p instanceof BooleanParameter || Number.class.isAssignableFrom(p.getValueClass())) {
                  assertNotNull(p.getValue());
                  assertNotNull(p.getDefault());
                  assertNotNull(cl.getValue());
                  assertNotNull(cl.getDefault());
               }
               for (String init : testStrings) {
                  try {
                     settings.clear();
                     EnhancedMapParser parser = EnhancedMapBindingFactoryImpl.getFactory(PropertiesParser.class, PropertiesParser.class,new BindingHelperImpl()).getParser(new StringReader(c.getSimpleName() + "=" + init));
                     parser.parse(settings);
                     setVal(p, settings);
                     assertNotNull(p.toString(), p.getValue());
                     if (!(p instanceof BooleanParameter && !"true".equals(init)) && !(p instanceof CharPasswordParameter) && !(p instanceof PasswordParameter)) {
                        assertNotEquals(p.getValue(), cl.getValue());
                     }
                     if (p instanceof PasswordParameter || p instanceof CharPasswordParameter) {
                        // password cleared by getValue
                        assertNull(p.getValue());
                        continue;
                     }
                     ParameterizableSerializer ps = ParameterizableBindingFactoryImpl.getFactory().getSerializer().setPrintOnlyNonDefault(false);
                     StringWriter sw = new StringWriter();
                     ps.serialize(p, sw);
                     String conf = sw.toString();
                     
                     if (conf != null && !"".equals(conf)) {
                        BindingHelper stringConversion = ParameterizableBindingFactoryImpl.getFactory().getBindingHelper();
                        StringBuilder sb = new StringBuilder();
                        stringConversion.serializeValue(p.getValue(), sb);
                        assertEquals(sb.toString(), conf.substring(conf.indexOf('=') + 1));
                        
                        Serializable parseAsParameterValue = ParameterizableBindingFactoryImpl.getFactory().getParser(new StringReader("")).parseAsParameterValue(sb.toString(), p);
                        if (p.getValueClass().isArray()) {
                           if (!ParameterHelper.isArrayEqual(p.getValueClass(), p.getValue(), parseAsParameterValue)) {
                              System.out.println("");
                           }
                           assertTrue(String.format("%s: %s != %s", p.getValueClass().getName(),p.getValue(),parseAsParameterValue),ParameterHelper.isArrayEqual(p.getValueClass(), p.getValue(), parseAsParameterValue));
                        } else {
                              assertEquals(String.valueOf(p.getValue()), String.valueOf(parseAsParameterValue));
                        }
                     }
                  } catch (NumberFormatException runtimeException) {
                     runtimeException.printStackTrace();
                  } catch (VectorPrintRuntimeException runtimeException) {
                     if (runtimeException.getCause() instanceof MalformedURLException
                         || runtimeException.getCause() instanceof NoSuchFieldException
                         || runtimeException.getCause() instanceof ClassNotFoundException) {
                        runtimeException.printStackTrace();
                     } else {
                        throw runtimeException;
                     }
                  }
               }
            }
         }
      }
   }

   @Test
   public void testParmeterizable() throws IOException, ParseException {
      Parameterizable p = new P();
      assertEquals(p.getClass(), p.getParameter("b", BooleanParameter.class).getDeclaringClass());
      assertEquals("v", p.getParameter("s", String.class).getDefault());
      assertEquals("v", p.getValue("s", String.class));

      EnhancedMap vp = new Settings();
      vp.put("ParameterizableImpl.s", "w");
      ParameterizableParser parser = ParameterizableBindingFactoryImpl.getFactory().getParser(new StringReader("P")).setSettings(vp).setPackageName(P.class.getPackage().getName());
      Parameterizable parse = parser.parseParameterizable();

      assertEquals("w", parse.getValue("s", String.class));

      StringWriter sw = new StringWriter();
      ParameterizableBindingFactoryImpl.getFactory().getSerializer().serialize(p, sw);
      String sp = sw.toString();

      StringReader sr = new StringReader(sp);
      parse = ParameterizableBindingFactoryImpl.getFactory().getParser(sr).setSettings(vp).setPackageName(P.class.getPackage().getName()).parseParameterizable();

      sw = new StringWriter();
      ParameterizableBindingFactoryImpl.getFactory().getSerializer().serialize(parse, sw);
      String sp2 = sw.toString();

      assertEquals(sp, sp2);

   }

   @Before
   public void setup() {
      ParameterizableBindingFactoryImpl.getFactory(ParameterizableParserImpl.class, ParameterizableParserImpl.class, new EscapingBindingHelper());
   }

   @Test
   public void testJsonParser() throws IOException, ParseException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
      String obj = "{'P':[{'a':[1,2]},{'b': true}]}";
      ParameterizableBindingFactory factoryImpl = ParameterizableBindingFactoryImpl.getFactory(JSONSupport.class, JSONSupport.class, new JSONBindingHelper());
      EnhancedMap settings = new Settings();
      settings.put("staticBoolean", "true");
      settings.put("P.c", "'red'");
      ParameterizableParser op = factoryImpl.getParser(new StringReader(obj)).
          setPackageName(P.class.getPackage().getName()).setSettings(settings);
      P p = (P) op.parseParameterizable();
      int i = p.getValue("a", int[].class)[0];
      int j = p.getValue("a", int[].class)[1];
      assertEquals(i,1);
      assertEquals(j,2);
      assertTrue(p.getValue("b", Boolean.class));
      assertEquals(Color.red,p.getValue("c", Color.class));
      assertTrue(p.isFf());
      p.setValue("e", EnumParam.E.E);
      Writer w = new StringWriter();
      factoryImpl.getSerializer().serialize(p, w);
      
      System.out.println(w.toString());

      op = factoryImpl.getParser(new StringReader(w.toString())).
          setPackageName(P.class.getPackage().getName()).setSettings(settings);
      P pp = (P) op.parseParameterizable();
      p.setValue("c", Color.red);
      assertEquals(pp, p);
   }

   @Test
   public void testSettingAnnotations() throws IOException, ParseException {
      Fields f = new Fields();
      AbstractPropertiesDecorator vp = new ParsingProperties(new Settings(), "src/test/resources/config"
          + File.separator + "chart.properties");
      vp.put("b", "true");
      vp.put("B", "false");
      vp.put("u", "file:src/test/resources/config/run.properties");
      vp.put("f", "10"); // setter will be called
      vp.put("ff", new String[]{"10", "20"});
      vp.put("", "true");
      try {
         SettingsAnnotationProcessorImpl.SAP.initSettings(f, vp);
      } catch (VectorPrintRuntimeException e) {
         // expected, no default and no setting
      }
      vp.put("nodefault", "true");
      SettingsAnnotationProcessorImpl.SAP.initSettings(f, vp);
      try {
         f.getSettings().put("notallowed", "");
      } catch (VectorPrintRuntimeException e) {
         // expected, readonly
      }
      assertEquals(Boolean.TRUE, f.isB());
      assertEquals(Boolean.FALSE, f.getB());
      assertEquals(new URL("file:src/test/resources/config/run.properties"), f.getU());
      assertEquals(Float.valueOf("50"), f.getF()); //  not 10 because setter sets it to 50
      assertArrayEquals(ArrayHelper.wrap(f.getFf()), ArrayHelper.wrap(new float[]{10, 20}));
      // the settings annotation wraps in an ObservableProperties and CachingProperties
      AbstractPropertiesDecorator settings = (AbstractPropertiesDecorator) f.getSettings();
      assertTrue(settings.hasProperties(ObservableProperties.class));
      assertTrue(settings.hasProperties(ReadonlyProperties.class));
      assertTrue(settings.hasProperties(CachingProperties.class));
      assertTrue(settings.hasProperties(ParsingProperties.class));

      vp.accept(new ParsingVisitor(f.getU()));
      assertTrue(vp.containsKey("dataclass"));
   }
}
