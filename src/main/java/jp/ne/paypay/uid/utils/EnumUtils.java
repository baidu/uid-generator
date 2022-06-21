/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ne.paypay.uid.utils;

import org.apache.commons.lang3.Validate;

/**
 * EnumUtils provides the operations for {@link ValuedEnum} such as Parse, value of...
 *
 * @author yutianbao
 */
public final class EnumUtils {

    /** Private Constructor */
    private EnumUtils(){}

    /**
     * Parse the bounded value into ValuedEnum
     *
     * @param clz
     * @param value
     * @return
     */
    public static <T extends ValuedEnum<V>, V> T parse(Class<T> clz, V value) {
        Validate.notNull(clz, "clz can not be null");
        if (value == null) {
            return null;
        }

        for (T t : clz.getEnumConstants()) {
            if (value.equals(t.value())) {
                return t;
            }
        }
        return null;
    }

    /**
     * Null-safe valueOf function
     *
     * @param <T>
     * @param enumType
     * @param name
     * @return
     */
    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if (name == null) {
            return null;
        }

        return Enum.valueOf(enumType, name);
    }

}
