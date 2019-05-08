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

package spec.cuba.web.fragments.screens;

import com.haulmont.cuba.gui.Fragments;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;

import javax.inject.Inject;

@UiController
public class ScreenWithFragmentListener extends Screen {
    @Inject
    protected Fragments fragments;

    @Subscribe
    protected void onInit(InitEvent event) {
        FragmentWithParentListener fragment = fragments.create(this, FragmentWithParentListener.class);
        getWindow().add(fragment.getFragment());
    }
}