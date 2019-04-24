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

package com.haulmont.cuba.web.widgets;

import com.vaadin.ui.CustomLayout;

import java.io.IOException;
import java.io.InputStream;

public class CubaCustomLayout extends CustomLayout {

    public CubaCustomLayout() {
        super();
    }

    public CubaCustomLayout(InputStream templateStream) throws IOException {
        super(templateStream);
    }

    public CubaCustomLayout(String template) {
        super(template);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return getState().requiredIndicatorVisible;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        getState().requiredIndicatorVisible = visible;
    }
}
