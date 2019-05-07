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

package com.haulmont.cuba.gui.sys;

import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext;

public class FragmentContextImpl extends FrameContextImpl {

    protected ComponentLoaderContext loaderContext;

    protected boolean initialized = false;

    // is set to true if fragment is created programmatically using Fragments factory
    protected boolean manualInitRequired = false;

    public FragmentContextImpl(Frame window, ComponentLoaderContext parentFrame) {
        super(window);
        this.loaderContext = parentFrame;
    }

    public ComponentLoaderContext getLoaderContext() {
        return loaderContext;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isManualInitRequired() {
        return manualInitRequired;
    }

    public void setManualInitRequired(boolean manualInitRequired) {
        this.manualInitRequired = manualInitRequired;
    }
}