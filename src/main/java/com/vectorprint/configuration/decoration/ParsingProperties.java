
package com.vectorprint.configuration.decoration;

/*-
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 - 2018 VectorPrint
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


import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.binding.AbstractBindingHelperDecorator;
import com.vectorprint.configuration.binding.BindingHelper;
import com.vectorprint.configuration.binding.settings.EnhancedMapBindingFactory;
import com.vectorprint.configuration.binding.settings.SettingsBindingService;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsingProperties extends AbstractPropertiesDecorator {


   private List<URL> propertyUrls = new ArrayList<>(3);
   private final Map<String, List<String>> commentBeforeKeys = new HashMap<>(50);
   private final List<String> trailingComment = new ArrayList<>(0);
   
   public ParsingProperties(EnhancedMap properties) {
      super(properties);
   }

   /**
    * Calls {@link #loadFromReader(java.io.Reader) }, does not set {@link #getPropertyUrls() } and {@link #setId(java.lang.String) }.
    * @param properties
    * @param in
    * @throws IOException
    */
   public ParsingProperties(EnhancedMap properties, Reader... in) throws IOException {
      super(properties);
      for (Reader r : in) {
         loadFromReader(r);
      }
   }

   /**
    * Sets {@link #getPropertyUrls() } and {@link #setId(java.lang.String) }, calls {@link #loadFromUrls() }.
    * @param properties
    * @param in
    * @throws IOException
    */
   public ParsingProperties(EnhancedMap properties, URL... in) throws IOException {
      super(properties);
      propertyUrls.addAll(Arrays.asList(in));
      setId(propertyUrls.toString());
      loadFromUrls();
   }

   /**
    * Calls {@link #ParsingProperties(com.vectorprint.configuration.EnhancedMap, java.net.URL...) }
    * @param properties
    * @param url
    * @throws IOException
    */
   public ParsingProperties(EnhancedMap properties, String... url) throws IOException {
      this(properties, AbstractBindingHelperDecorator.parse(url, URL.class));
   }

   /**
    * Calls {@link #ParsingProperties(com.vectorprint.configuration.EnhancedMap, java.net.URL...) }
    * @param properties
    * @param files
    * @throws IOException
    */
   public ParsingProperties(EnhancedMap properties, File... files) throws IOException {
      this(properties, toURL(files));
   }
   
   public static URL[] toURL(File... files) {
      URL[] u = new URL[files.length];
      for (int i = 0; i < files.length; i++) {
         try {
            u[i] = files[i].toURI().toURL();
         } catch (MalformedURLException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      return u;
   }
   /**
    * loads properties (overwrites) from the Reader using {@link EnhancedMapBindingFactory#getParser(java.io.Reader) }.
    *
    * @param in
    * @throws IOException
    */
   protected void loadFromReader(Reader in) throws IOException {
      try (BufferedReader bi = new BufferedReader(in)) {
         SettingsBindingService.getInstance().getFactory().getParser(bi).parse(this);
      }
   }
   
   /**
    * calls {@link #addFromURL(java.net.URL) }.
    * @see BindingHelper#convert(java.lang.String, java.lang.Class) 
    * @param url
    * @throws IOException
    */
   public void addFromURL(String url) throws IOException {
      addFromURL(SettingsBindingService.getInstance().getFactory().getBindingHelper().convert(url, URL.class));
   }
   
   /**
    * adds properties from a URL, calls {@link #setId(java.lang.String) }.
    * @param u
    * @throws IOException
    */
   public void addFromURL(URL u) throws IOException {
      loadFromReader(new InputStreamReader(u.openStream()));
      propertyUrls.add(u);
      setId(propertyUrls.toString());
   }

   /**
    * loads properties (overwrites) from the urls determined at construction.
    *
    * @throws IOException
    */
   protected void loadFromUrls() throws IOException {
      for (URL u : propertyUrls) {
         loadFromReader(new InputStreamReader(u.openStream()));
      }
   }

   /**
    * save the properties to a url, using a {@link EnhancedMapBindingFactory#getSerializer() }.
    *
    * @param url
    * @throws IOException
    */
   public void saveToUrl(URL url) throws IOException {
      OutputStreamWriter osw = null;
      try {
         OutputStream o;
         if ("file".equals(url.getProtocol())) {
            o = new FileOutputStream(url.getFile());
         } else {
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(false);
            o = conn.getOutputStream();
         }
         osw  = new OutputStreamWriter(new BufferedOutputStream(o));
         SettingsBindingService.getInstance().getFactory().getSerializer().serialize(this, osw);
      } finally {
         if (osw != null) {
            osw.close();
         }
      }
   }

   /**
    * return the URL's from which the properties were loaded.
    * @return 
    */
   public List<URL> getPropertyUrls() {
      return propertyUrls;
   }

   @Override
   public void clear() {
      super.clear();
      trailingComment.clear();
      commentBeforeKeys.clear();
      propertyUrls.clear();
   }

   public List<String> getCommentBeforeKey(String key) {
      if (!commentBeforeKeys.containsKey(key)) {
         commentBeforeKeys.put(key, new ArrayList<>(1));
      }
      return commentBeforeKeys.get(key);
   }

   public List<String> getTrailingComment() {
      return trailingComment;
   }

   public EnhancedMap addCommentBeforeKey(String key, String comment) {
      if (!commentBeforeKeys.containsKey(key)) {
         commentBeforeKeys.put(key, new ArrayList<>(1));
      }
      commentBeforeKeys.get(key).add(comment);
      return this;
   }

   public EnhancedMap addTrailingComment(String comment) {
      trailingComment.add(comment);
      return this;
   }

   @Override
   public EnhancedMap clone() throws CloneNotSupportedException {
      ParsingProperties parsingProperties = (ParsingProperties) super.clone();
      parsingProperties.commentBeforeKeys.putAll(commentBeforeKeys);
      parsingProperties.propertyUrls = propertyUrls;
      parsingProperties.trailingComment.addAll(trailingComment);
      return parsingProperties;
   }
}
