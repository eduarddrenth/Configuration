
package com.vectorprint.configuration.binding;

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

public interface BindingHelper {

    /**
     * use this from {@link #serializeValue(java.lang.Object) } if you need to
     * escape syntax specific characters. Note that overriding only this method
     * when extending {@link AbstractBindingHelperDecorator} will not work,
     * because the overridden method will not be called by the encapsulated
     * {@link BindingHelper}.
     *
     * @see #setEscapeChars(char[])
     * @param value
     * @return
     */
    public String escape(String value);

    /**
     * set characters to be escaped, do this from the constructors when
     * extending {@link AbstractBindingHelperDecorator}.
     *
     * @param chars
     */
    public void setEscapeChars(char[] chars);

    /**
     * supports arrays of primitives and their wrappers, enums, URL, Color,
     * Date, Pattern and String
     *
     * @param <T>
     * @param values
     * @param clazz
     * @return
     */
    <T> T convert(String[] values, Class<T> clazz);

    /**
     * supports primitives and their wrappers, enums, URL, Color, Date, Pattern
     * and String
     *
     * @param <T>
     * @param value
     * @param clazz
     * @return
     */
    <T> T convert(String value, Class<T> clazz);

    /**
     * set separator to be used for array values, do this from the constructors
     * when extending {@link AbstractBindingHelperDecorator}.
     *
     * @param separator
     */
    public void setArrayValueSeparator(char separator);

    char getArrayValueSeparator();

    /**
     * Serialize Objects and arrays of Objects and primitives in a specific
     * syntax. Array values will be separated by the
     * {@link #setArrayValueSeparator(char) separator}.
     *
     * @param value
     * @return the String
     */
    String serializeValue(Object value);

}
