/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.core.sys.servlet.events;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

import javax.servlet.ServletContext;

/**
 * Application lifecycle event.
 * <p>
 * Published when Servlet and Application are about to be shut down. Enables to free resources manually.
 */
public class ServletContextDestroyedEvent extends ApplicationEvent {
    protected ApplicationContext applicationContext;

    public ServletContextDestroyedEvent(ServletContext source, ApplicationContext applicationContext) {
        this(source);
        this.applicationContext = applicationContext;
    }

    public ServletContextDestroyedEvent(ServletContext source) {
        super(source);
    }

    @Override
    public ServletContext getSource() {
        return (ServletContext) super.getSource();
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
