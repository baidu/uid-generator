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
package com.baidu.fsg.uid.worker;

import com.baidu.fsg.uid.utils.ValuedEnum;

/**
 * WorkerNodeType
 * <li>CONTAINER: Such as Docker
 * <li>ACTUAL: Actual machine
 * 
 * @author yutianbao
 */
public enum WorkerNodeType implements ValuedEnum<Integer> {

    CONTAINER(1), ACTUAL(2);

    /**
     * Lock type
     */
    private final Integer type;

    /**
     * Constructor with field of type
     */
    private WorkerNodeType(Integer type) {
        this.type = type;
    }

    @Override
    public Integer value() {
        return type;
    }

}
