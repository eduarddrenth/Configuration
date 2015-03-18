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

package com.vectorprint.configuration.decoration.visiting;

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.decoration.ParsingProperties;
import com.vectorprint.configuration.parser.ParseException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ParsingVisitor implements DecoratorVisitor<ParsingProperties>{
   
   public static final Logger logger = Logger.getLogger(ParsingVisitor.class.getName());

   @Override
   public Class<ParsingProperties> getClazz() {
      return ParsingProperties.class;
   }

   /**
    * when the object is a String or URL calls {@link ParsingProperties#addFromURL(java.lang.String) } or
    * {@link ParsingProperties#addFromURL(java.net.URL) }.
    * @param e
    * @param o 
    */
   @Override
   public void visit(ParsingProperties e, Object o) {
      try {
         if (o instanceof URL) {
            e.addFromURL((URL) o);
         } else if (o instanceof String) {
            e.addFromURL((String) o);
         } else {
            logger.warning(String.format("will not add properties from %s", o));
         }
      } catch (IOException iOException) {
         throw new VectorPrintRuntimeException(iOException);
      } catch (ParseException parseException) {
         throw new VectorPrintRuntimeException(parseException);
      }
   }

}
