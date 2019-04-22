/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.gui.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JavaDoc
 */
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface StudioComponent {
    String caption() default "";

    String description() default "";

    String category() default "";

    String icon() default "";

    String tag() default "";

    String defaultProperty() default "";

    String defaultEvent() default "";

    String[] unsupportedProperties() default {};

    boolean container() default false;

    ContainerType containerType() default ContainerType.VERTICAL;

    /**
     * JavaDoc
     *
     * todo extension point that can embed custom shortcuts to properties panel
     */
    String designer() default "";
}