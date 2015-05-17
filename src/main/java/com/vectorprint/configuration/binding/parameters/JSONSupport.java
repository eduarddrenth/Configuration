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
package com.vectorprint.configuration.binding.parameters;

import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.binding.JSONStringConversion;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.configuration.parser.JSONParser;
import com.vectorprint.configuration.parser.ParameterizableParserImpl;
import com.vectorprint.configuration.parser.ParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser / serializer to support JSON syntax. This parser is less efficient than {@link ParameterizableParserImpl} which
 * instantiates and initializes a Parameterizable object during parsing. Robert Fischer's JSON parser is used under the hood.
 * This parser yields a generic Java datamodel (Map, List, String, BigDecimal, etc.) which is translated into a Parameterizable
 * object.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class JSONSupport extends AbstractParameterizableParser<Object> {

   private Reader reader;

   {
      setStringConversion(new JSONStringConversion());
   }

   public JSONSupport(Reader reader) {
      this.reader = reader;
   }

   @Override
   public void initParameter(Parameter parameter, Object value) {
      if (value!=null) {
         setValueOrDefault(parameter, value, false);
      }
   }

   @Override
   public Object parseAsParameterValue(String valueToParse, String key) {
      try {
         return new JSONParser(valueToParse).parse();
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   @Override
   public void setValueOrDefault(Parameter parameter, Object values, boolean setDefault) {
      if (parameter.getValueClass().isArray()) {
         if (!(values instanceof List)) {
            throw new VectorPrintRuntimeException(EXAMPLE_SUPPORTED_JSON_SYNTAX);
         }
         List l = (List) values;
         List<String> sl = new ArrayList<String>(l.size());
         for (Object o : l) {
            sl.add(String.valueOf(o));
         }
         if (String[].class.equals(parameter.getValueClass())) {
            if (setDefault) {
               parameter.setDefault(sl.toArray());
            } else {
               parameter.setValue(sl.toArray());
            }
         } else {
            Serializable o = (Serializable) getStringConversion().parse(ArrayHelper.toArray(sl), parameter.getValueClass());
            if (setDefault) {
               parameter.setDefault(o);
            } else {
               parameter.setValue(o);
            }
         }
      } else {
         String s = String.valueOf(values);
         if (String.class.equals(parameter.getValueClass())) {
            if (setDefault) {
               parameter.setDefault(s);
            } else {
               parameter.setValue(s);
            }
         } else {
            Serializable o = (Serializable) getStringConversion().parse(s, parameter.getValueClass());
            if (setDefault) {
               parameter.setDefault(o);
            } else {
               parameter.setValue(o);
            }
         }
      }
   }

   private void serializeParam(Parameter par, StringBuilder sb) {
      sb.append("{'").append(par.getKey()).append("': ");
      if (par.getValueClass().isArray()) {
         sb.append('[');
      }
      getStringConversion().serializeValue(par.getValue(), sb, ",");
      if (par.getValueClass().isArray()) {
         sb.append(']');
      }
      sb.append('}');
   }

   @Override
   public void serialize(Parameterizable p, Writer w) throws IOException {
      StringBuilder sb = new StringBuilder();
      sb.append("{'").append(p.getClass().getSimpleName()).append("': ");
      if (!p.getParameters().isEmpty()) {
         sb.append('[');
         final int max = p.getParameters().size();
         int i = 0;
         for (Parameter par : p.getParameters().values()) {
            serializeParam(par, sb);
            if (i < max - 1) {
               sb.append(',');
            }
            i++;
         }
         sb.append(']');
      } else {
         sb.append(" null");
      }
      w.write(sb.append('}').toString());
   }

   @Override
   public void serialize(Parameter par, Writer w) throws IOException {
      StringBuilder sb = new StringBuilder();
      serializeParam(par, sb);
      w.write(sb.toString());
   }

   /**
    * <pre>
    * syntax looks like this: {'P':[{'a':[1,2]},{'b':3}]}, P is the Parameterizable, a and b are parameters
    *
    * first and only entry in the Map is the name of the Parameterizable class and its parameters in a List
    * each entry in the parameter List contains the parameter key and its value(s) in a Map
    * parameter values can be a List or a plain value
    * a plain value can be a BigInteger / BigDecimal, a String, a Boolean or null
    * </pre>
    *
    * @return
    */
   @Override
   public Parameterizable parseParameterizable() {
      Object parse = null;
      Parameterizable parameterizable = null;
      try {
         parse = new JSONParser(reader).parse();
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
      if (parse instanceof Map) {
         Map m = (Map) parse;
         if (m.size() == 1) {
            Map.Entry next = (Map.Entry) m.entrySet().iterator().next();
            String className = String.valueOf(next.getKey());
            EnhancedMap settings = getSettings();
            String pkg = getPackageName();
            Class c;
            try {
               c = (pkg != null) ? Class.forName(pkg + "." + className) : Class.forName(className);
            } catch (ClassNotFoundException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
            if (!Parameterizable.class.isAssignableFrom(c)) {
               throw new VectorPrintRuntimeException(String.format("%s is not a %s", c.getName(), Parameterizable.class.getName()));
            }
            if (settings != null) {
               // init static settings
               SettingsAnnotationProcessor.SAP.initSettings(c, settings);
            }
            try {
               parameterizable = (Parameterizable) c.newInstance();
            } catch (InstantiationException ex) {
               throw new VectorPrintRuntimeException(ex);
            } catch (IllegalAccessException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
            initParameterizable(parameterizable);
            if (next.getValue() != null) {
               if (next.getValue() instanceof List) {
                  List parameters = (List) next.getValue();
                  for (Object o : parameters) {
                     if (o instanceof Map) {
                        Map p = (Map) o;
                        if (p.size() == 1) {
                           Map.Entry par = (Map.Entry) p.entrySet().iterator().next();
                           String key = String.valueOf(par.getKey());
                           checkKey(parameterizable, key);
                           initParameter(parameterizable.getParameters().get(key), par.getValue());
                        } else {
                           throw new VectorPrintRuntimeException(EXAMPLE_SUPPORTED_JSON_SYNTAX);
                        }
                     } else {
                        throw new VectorPrintRuntimeException(EXAMPLE_SUPPORTED_JSON_SYNTAX);
                     }
                  }
               } else {
                  throw new VectorPrintRuntimeException(EXAMPLE_SUPPORTED_JSON_SYNTAX);
               }
            }
            for (Parameter pa : parameterizable.getParameters().values()) {
               String key = ParameterHelper.findDefaultKey(pa.getKey(), parameterizable.getClass(), getSettings());
               if (key != null) {
                  Object values = parseAsParameterValue(getSettings().getProperty(key), pa.getKey());
                  setValueOrDefault(pa, values, true);
               }
            }
         } else {
            throw new VectorPrintRuntimeException(EXAMPLE_SUPPORTED_JSON_SYNTAX);
         }
      } else {
         throw new VectorPrintRuntimeException(EXAMPLE_SUPPORTED_JSON_SYNTAX);
      }
      return parameterizable;
   }
   public static final String EXAMPLE_SUPPORTED_JSON_SYNTAX = "example supported json syntax: {'P':[{'a':[1,2]},{'b':3}]}";

   /**
    * only for serialization
    */
   public JSONSupport() {
      // not for parsing
   }

}
