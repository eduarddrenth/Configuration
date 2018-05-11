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
package com.vectorprint.configuration.binding.parameters.json;

/*
 * #%L
 * Config
 * %%
 * Copyright (C) 2015 VectorPrint
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
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessor;
import com.vectorprint.configuration.annotation.SettingsAnnotationProcessorImpl;
import com.vectorprint.configuration.binding.parameters.AbstractParameterizableBinding;
import com.vectorprint.configuration.binding.parameters.ParamBindingHelper;
import com.vectorprint.configuration.binding.parameters.ParameterHelper;
import com.vectorprint.configuration.generated.parser.JSONParser;
import com.vectorprint.configuration.generated.parser.ParameterizableParserImpl;
import com.vectorprint.configuration.generated.parser.ParseException;
import com.vectorprint.configuration.parameters.Parameter;
import com.vectorprint.configuration.parameters.Parameterizable;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Parser / serializer to support JSON syntax. This parser is less efficient than {@link ParameterizableParserImpl}
 * which instantiates and initializes a Parameterizable object during parsing. Robert Fischer's JSON parser is used
 * under the hood. This parser yields a generic Java datamodel (Map, List, String, BigDecimal, etc.) which is translated
 * into a Parameterizable object.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class JSONSupport extends AbstractParameterizableBinding<Object> {

   private Reader reader;

   private ParamBindingHelper bindingHelper;

   private final SettingsAnnotationProcessor sap = new SettingsAnnotationProcessorImpl();

   @Override
   public void setBindingHelper(ParamBindingHelper bindingHelper) {
      this.bindingHelper = bindingHelper;
   }

   public JSONSupport(Reader reader) {
      this.reader = reader;
   }

   /**
    * first do conversion to a value for the parameter than call {@link ParamBindingHelper#setValueOrDefault(com.vectorprint.configuration.parameters.Parameter, java.io.Serializable, boolean)
    * }
    *
    * @param parameter
    * @param values
    */
   @Override
   public void initParameter(Parameter parameter, Object values) {
      if (values != null) {
         convertAndSet(parameter, values, false);
      }
   }

   protected <TYPE extends Serializable> TYPE convert(Object values, Parameter<TYPE> parameter) {
      if (parameter.getValueClass().isArray()) {
         if (!(values instanceof List)) {
            throw new VectorPrintRuntimeException(EXAMPLE_SUPPORTED_JSON_SYNTAX);
         }
         List l = (List) values;
         List<String> sl = new ArrayList<>(l.size());
         for (Object o : l) {
            sl.add(String.valueOf(o));
         }
         if (String[].class.equals(parameter.getValueClass())) {
            return (TYPE) ArrayHelper.toArray(sl);
         } else {
            Serializable o = (Serializable) bindingHelper.convert(ArrayHelper.toArray(sl), parameter.getValueClass());
            return (TYPE) o;
         }
      } else {
         String s = String.valueOf(values);
         if (String.class.equals(parameter.getValueClass())) {
            return (TYPE) s;
         } else {
            Serializable o = (Serializable) bindingHelper.convert(s, parameter.getValueClass());
            return (TYPE) o;
         }
      }
   }

   protected void convertAndSet(Parameter parameter, Object values, boolean setDefault) {
      Serializable convert = convert(values, parameter);
      bindingHelper.setValueOrDefault(parameter, convert, setDefault);
   }

   @Override
   public <TYPE extends Serializable> TYPE parseAsParameterValue(String valueToParse, Parameter<TYPE> parameter) {
      try {
         Object parse = new JSONParser(valueToParse).parse();
         return convert(parse, parameter);
      } catch (ParseException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   private void serializeParam(Parameter par, StringBuilder sb) {
      sb.append("{'").append(par.getKey()).append("': ").append(bindingHelper.serializeValue(bindingHelper.getValueToSerialize(par, false))).append('}');
   }

   @Override
   public void serialize(Parameterizable p, Writer w) throws IOException {
      StringBuilder sb = new StringBuilder();
      sb.append("{'").append(p.getClass().getSimpleName()).append("': ");
      if (!p.getParameters().isEmpty()) {
         Collection<Parameter> c = new ArrayList<>(p.getParameters().size());
         for (Parameter par : p.getParameters().values()) {
            if ((include(par))) {
               c.add(par);
            }
         }
         if (!c.isEmpty()) {
            int max = c.size();
            int i = 0;
            sb.append('[');
            for (Parameter par : c) {
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
      } else {
         sb.append(" null");
      }
      w.write(sb.append('}').toString());
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
               sap.initSettings(c, settings);
            }
            try {
               parameterizable = (Parameterizable) c.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
            // init instance settings
            sap.initSettings(parameterizable, settings);
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
               String key = ParameterHelper.findKey(pa.getKey(), parameterizable.getClass(), getSettings(), ParameterHelper.SUFFIX.set_default);
               if (key != null) {
                  Serializable values = parseAsParameterValue(getSettings().getProperty(key), pa);
                  bindingHelper.setValueOrDefault(pa, values, true);
               }
               key = ParameterHelper.findKey(pa.getKey(), parameterizable.getClass(), getSettings(), ParameterHelper.SUFFIX.set_value);
               if (key != null) {
                  Serializable values = parseAsParameterValue(getSettings().getProperty(key), pa);
                  bindingHelper.setValueOrDefault(pa, values, false);
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
