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

import com.vectorprint.VectorPrintRuntimeException;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for yielding key value pairs, based on (command line) arguments
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ArgumentParser {

   /**
    * identifies the start of a key
    */
   public static final String START_OF_KEY = "-";
   public static final String WRONGKEYMESSAGE = "keys in the argument list should start with a " + START_OF_KEY;

   /**
    * a "-" at the start of a String identifies a key, all other Strings will be values. A key may be followed by a key,
    * in that case the value for the first key will be an empty String.
    *
    * @param args
    * @return
    * @see #START_OF_KEY
    */
   public static Map<String, String> parseArgs(String[] args) {
      if (args == null) {
         return null;
      }
      Map<String, String> p = new HashMap<String, String>(args.length / 2);
      int i = 0;
      boolean expectKey = true;
      for (String s : args) {
         if (s.startsWith(START_OF_KEY)) {
            int j = i + 1;
            p.put(s.substring(1), (j < args.length && !args[j].startsWith(START_OF_KEY)) ? args[j] : "");
            expectKey = j < args.length && args[j].startsWith(START_OF_KEY);
         } else if (expectKey) {
            throw new VectorPrintRuntimeException(WRONGKEYMESSAGE + ": " + s);
         } else {
            expectKey = true;
         }
         i++;
      }
      return p;
   }
}
