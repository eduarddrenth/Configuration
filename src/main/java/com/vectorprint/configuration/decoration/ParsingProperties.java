/*
 * Copyright 2015 VectorPrint.
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
package com.vectorprint.configuration.decoration;

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.VectorPrintProperties;
import static com.vectorprint.configuration.VectorPrintProperties.EOL;
import com.vectorprint.configuration.parameters.MultipleValueParser;
import com.vectorprint.configuration.parser.ParseException;
import com.vectorprint.configuration.parser.PropertiesParser;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This decorator supports loading properties from streams, urls or files and saving to a url. A 
 * {@link PropertiesParser} will be used for parsing the properties.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParsingProperties extends AbstractPropertiesDecorator {


   private List<URL> propertyUrls = new ArrayList<URL>(3);
   private final Map<String, List<String>> commentBeforeKeys = new HashMap<String, List<String>>(50);
   private final List<String> trailingComment = new ArrayList<String>(0);

   private ParsingProperties(EnhancedMap properties) throws IOException, ParseException {
      super(properties);
   }

   /**
    * Calls {@link #loadFromReader(java.io.Reader) }, does not set {@link #getPropertyUrls() } and {@link #setId(java.lang.String) }.
    * @param properties
    * @param in
    * @throws IOException
    * @throws ParseException 
    */
   public ParsingProperties(EnhancedMap properties, Reader... in) throws IOException, ParseException {
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
    * @throws ParseException 
    */
   public ParsingProperties(EnhancedMap properties, URL... in) throws IOException, ParseException {
      super(properties);
      for (URL u : in) {
         propertyUrls.add(u);
      }
      setId(propertyUrls.toString());
      loadFromUrls();
   }

   /**
    * Calls {@link #ParsingProperties(com.vectorprint.configuration.EnhancedMap, java.net.URL) } with {@link
    * MultipleValueParser.URLParser#parseString(java.lang.String) }
    * @param properties
    * @param url
    * @throws IOException
    * @throws ParseException 
    */
   public ParsingProperties(EnhancedMap properties, String... url) throws IOException, ParseException {
      this(properties, toURL(url));
   }

   /**
    * Calls {@link #ParsingProperties(com.vectorprint.configuration.EnhancedMap, java.net.URL) }
    * @param properties
    * @param inFile
    * @throws IOException
    * @throws ParseException 
    */
   public ParsingProperties(EnhancedMap properties, File... inFile) throws IOException, ParseException {
      this(properties, toURL(inFile));
   }
   
   /**
    * @see MultipleValueParser.URLParser
    * @param urls
    * @return 
    */
   public static URL[] toURL(String... urls) {
      URL[] u = new URL[urls.length];
      for (int i = 0; i < urls.length; i++) {
         u[i] = MultipleValueParser.URL_PARSER.parseString(urls[i]);
      }
      return u;
   }
   public static URL[] toURL(File... urls) {
      URL[] u = new URL[urls.length];
      for (int i = 0; i < urls.length; i++) {
         try {
            u[i] = urls[i].toURI().toURL();
         } catch (MalformedURLException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      return u;
   }
   /**
    * loads properties (overwrites) from the stream determined at construction. If you put a \ at the end of a line in a
    * settings file the next line will be concatenated. If there is no \ at the end and the next line contains no "="
    * the next line will be concatenated with line.separator as glue.
    *
    * @throws IOException
    */
   protected void loadFromReader(Reader in) throws IOException, ParseException {
      BufferedReader bi = new BufferedReader(in);
      try {
         new PropertiesParser(bi).parse(this);
      } finally {
         bi.close();
      }
   }
   
   /**
    * calls {@link #addFromURL(java.net.URL) }.
    * @see MultipleValueParser.URLParser#parseString(java.lang.String) 
    * @param url
    * @throws IOException
    * @throws ParseException 
    */
   public void addFromURL(String url) throws IOException, ParseException {
      addFromURL(MultipleValueParser.URL_PARSER.parseString(url));
   }

   /**
    * adds properties from a URL, calls {@link #setId(java.lang.String) }.
    * @param url
    * @throws IOException
    * @throws ParseException 
    */
   public void addFromURL(URL u) throws IOException, ParseException {
      loadFromReader(new InputStreamReader(u.openStream()));
      propertyUrls.add(u);
      setId(propertyUrls.toString());
   }

   /**
    * loads properties (overwrites) from the urls determined at construction. If you put a \ at the end of a line in a
    * settings file the next line will be concatenated. If there is no \ at the end and the next line contains no "="
    * the next line will be concatenated with line.separator as glue.
    *
    * @see #loadFromReader(java.io.Reader)
    * @throws IOException
    */
   protected void loadFromUrls() throws IOException, ParseException {
      for (URL u : propertyUrls) {
         loadFromReader(new InputStreamReader(u.openStream()));
      }
   }

   /**
    * save the properties to a url, including those set by {@link #addFromArguments(String[])}.
    *
    * @throws IOException
    */
   public void saveToUrl(URL url) throws IOException {
      BufferedOutputStream bo = null;
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
         bo = new BufferedOutputStream(o);
         for (Map.Entry<String, String> entry : super.entrySet()) {
            for (String s : getCommentBeforeKey(entry.getKey())) {
               bo.write(s.getBytes());
            }
            bo.write((entry.getKey() + "=" + entry.getValue() + EOL).getBytes());
         }
         if (!trailingComment.isEmpty()) {
            for (String s : trailingComment) {
               bo.write(s.getBytes());
            }
         }
      } finally {
         if (bo != null) {
            bo.close();
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
         commentBeforeKeys.put(key, new ArrayList<String>(1));
      }
      return commentBeforeKeys.get(key);
   }

   public List<String> getTrailingComment() {
      return trailingComment;
   }

   public EnhancedMap addCommentBeforeKey(String key, String comment) {
      if (!commentBeforeKeys.containsKey(key)) {
         commentBeforeKeys.put(key, new ArrayList<String>(1));
      }
      commentBeforeKeys.get(key).add(comment);
      return this;
   }

   public EnhancedMap addTrailingComment(String comment) {
      trailingComment.add(comment);
      return this;
   }

   @Override
   public EnhancedMap clone() {
      ParsingProperties parsingProperties = (ParsingProperties) super.clone();
      parsingProperties.commentBeforeKeys.putAll(commentBeforeKeys);
      parsingProperties.propertyUrls = propertyUrls;
      parsingProperties.trailingComment.addAll(trailingComment);
      return parsingProperties;
   }
}
