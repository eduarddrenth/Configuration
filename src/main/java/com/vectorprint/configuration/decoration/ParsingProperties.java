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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This decorator supports loading properties from a stream, url or file, saving to a url. A 
 * {@link PropertiesParser} will be used for parsing the properties.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParsingProperties extends AbstractPropertiesDecorator {


   private URL propertyUrl;
   private final Map<String, List<String>> commentBeforeKeys = new HashMap<String, List<String>>(50);
   private final List<String> trailingComment = new ArrayList<String>(0);

   private ParsingProperties(EnhancedMap properties) throws IOException, ParseException {
      super(properties);
   }

   /**
    * Calls {@link #loadFromReader(java.io.Reader) }, does not set {@link #getPropertyUrl() } and {@link #setId(java.lang.String) }.
    * @param properties
    * @param in
    * @throws IOException
    * @throws ParseException 
    */
   public ParsingProperties(EnhancedMap properties, Reader in) throws IOException, ParseException {
      super(properties);
      loadFromReader(in);
   }

   /**
    * Sets {@link #getPropertyUrl() } and {@link #setId(java.lang.String) }, calls {@link #loadFromUrl() }.
    * @param properties
    * @param in
    * @throws IOException
    * @throws ParseException 
    */
   public ParsingProperties(EnhancedMap properties, URL in) throws IOException, ParseException {
      super(properties);
      this.propertyUrl = in;
      setId(in.toString());
      loadFromUrl();
   }

   /**
    * Calls {@link #ParsingProperties(com.vectorprint.configuration.EnhancedMap, java.net.URL) } with {@link
    * MultipleValueParser#URL_PARSER#parse(java.lang.String) }.
    * @param properties
    * @param url
    * @throws IOException
    * @throws ParseException 
    */
   public ParsingProperties(EnhancedMap properties, String url) throws IOException, ParseException {
      this(properties, MultipleValueParser.URL_PARSER.parseString(url));
   }

   /**
    * Calls {@link #ParsingProperties(com.vectorprint.configuration.EnhancedMap, java.net.URL) }
    * @param properties
    * @param inFile
    * @throws IOException
    * @throws ParseException 
    */
   public ParsingProperties(EnhancedMap properties, File inFile) throws IOException, ParseException {
      this(properties, inFile.toURI().toURL());
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
    * loads properties (overwrites) from the url determined at construction. If you put a \ at the end of a line in a
    * settings file the next line will be concatenated. If there is no \ at the end and the next line contains no "="
    * the next line will be concatenated with line.separator as glue.
    *
    * @see #loadFromReader(java.io.Reader)
    * @throws IOException
    */
   protected void loadFromUrl() throws IOException, ParseException {
      loadFromReader(new InputStreamReader(propertyUrl.openStream()));
   }

   /**
    * save the properties to the url the properties were initialized from, including those set by
    * {@link #addFromArguments(String[])} .
    *
    * @throws IOException
    */
   public void saveToUrl() throws IOException {
      saveToUrl(propertyUrl);
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

   public URL getPropertyUrl() {
      return propertyUrl;
   }

   @Override
   public void clear() {
      super.clear();
      trailingComment.clear();
      commentBeforeKeys.clear();
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
      try {
         ParsingProperties parsingProperties = new ParsingProperties(getEmbeddedProperties().clone());
         parsingProperties.commentBeforeKeys.putAll(commentBeforeKeys);
         parsingProperties.propertyUrl=propertyUrl;
         parsingProperties.trailingComment.addAll(trailingComment);
         return parsingProperties;
      } catch (IOException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }
}
