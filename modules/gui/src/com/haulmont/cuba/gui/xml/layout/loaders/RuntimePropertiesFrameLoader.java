/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.gui.GuiDevelopmentException;
import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.components.Fragment;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.sys.FragmentImplementation;
import com.haulmont.cuba.gui.components.sys.FrameImplementation;
import com.haulmont.cuba.gui.config.WindowAttributesProvider;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.logging.ScreenLifeCycle;
import com.haulmont.cuba.gui.model.impl.ScreenDataImpl;
import com.haulmont.cuba.gui.screen.FrameOwner;
import com.haulmont.cuba.gui.screen.ScreenContext;
import com.haulmont.cuba.gui.screen.ScreenFragment;
import com.haulmont.cuba.gui.screen.ScreenOptions;
import com.haulmont.cuba.gui.sys.FragmentContextImpl;
import com.haulmont.cuba.gui.sys.ScreenContextImpl;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import com.haulmont.cuba.gui.xml.layout.LayoutLoader;
import com.haulmont.cuba.gui.xml.layout.ScreenXmlLoader;
import com.haulmont.cuba.gui.xml.layout.loaders.FragmentComponentLoader.FragmentLoaderInitTask;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.perf4j.StopWatch;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Objects;

import static com.haulmont.cuba.gui.logging.UIPerformanceLogger.createStopWatch;
import static com.haulmont.cuba.gui.screen.UiControllerUtils.*;
import static com.haulmont.cuba.gui.xml.layout.loaders.FragmentComponentLoader.FragmentLoaderInjectTask;

public class RuntimePropertiesFrameLoader extends ContainerLoader<Frame> {

    protected static final String DEFAULT_DESCRIPTOR =
            "/com/haulmont/cuba/gui/app/core/dynamicattributes/runtime-properties-frame.xml";

    protected ComponentLoader fragmentLoader;
    protected ComponentLoaderContext innerContext;

    @Override
    public ComponentContext getContext() {
        return (ComponentContext) super.getContext();
    }

    @Override
    public void setContext(Context context) {
        Preconditions.checkArgument(context instanceof ComponentContext,
                "'context' must implement com.haulmont.cuba.gui.xml.layout.ComponentLoader.ComponentContext");
        super.setContext(context);
    }

    @Override
    public void createComponent() {
        String src = element.attributeValue("src");
        String screenId = element.attributeValue("id");

        if (src == null) {
            src = DEFAULT_DESCRIPTOR;
        }

        String fragmentId = screenId != null ? screenId : src;

        WindowInfo windowInfo = createFakeWindowInfo(src, fragmentId);

        Fragment fragment = factory.create(Fragment.NAME);
        ScreenFragment controller = createController(windowInfo, fragment, windowInfo.asFragment());

        // setup screen and controller
        ComponentLoaderContext parentContext = (ComponentLoaderContext) getContext();

        FrameOwner hostController = parentContext.getFrame().getFrameOwner();

        ScreenContext hostScreenContext = getScreenContext(hostController);

        setHostController(controller, hostController);
        setWindowId(controller, windowInfo.getId());
        setFrame(controller, fragment);
        setScreenContext(controller,
                new ScreenContextImpl(windowInfo, parentContext.getOptions(),
                        hostScreenContext.getScreens(),
                        hostScreenContext.getDialogs(),
                        hostScreenContext.getNotifications(),
                        hostScreenContext.getFragments(),
                        hostScreenContext.getUrlRouting())
        );
        setScreenData(controller, new ScreenDataImpl());

        FragmentImplementation fragmentImpl = (FragmentImplementation) fragment;
        fragmentImpl.setFrameOwner(controller);
        fragmentImpl.setId(fragmentId);

        FragmentContextImpl frameContext = new FragmentContextImpl(fragment, innerContext);
        ((FrameImplementation) fragment).setContext(frameContext);

        // load from XML if needed

        if (windowInfo.getTemplate() != null) {
            String frameId = fragmentId;
            if (parentContext.getFullFrameId() != null) {
                frameId = parentContext.getFullFrameId() + "." + frameId;
            }

            innerContext = new ComponentLoaderContext(getContext().getOptions());
            innerContext.setCurrentFrameId(fragmentId);
            innerContext.setFullFrameId(frameId);
            innerContext.setFrame(fragment);
            innerContext.setParent(parentContext);

            LayoutLoader layoutLoader = beanLocator.getPrototype(LayoutLoader.NAME, innerContext);
            layoutLoader.setLocale(getLocale());
            layoutLoader.setMessagesPack(getMessagePack(windowInfo.getTemplate()));

            ScreenXmlLoader screenXmlLoader = beanLocator.get(ScreenXmlLoader.NAME);

            Element windowElement = screenXmlLoader.load(windowInfo.getTemplate(), windowInfo.getId(),
                    getContext().getParams());

            this.fragmentLoader = layoutLoader.createFragmentContent(fragment, windowElement);
        }

        this.resultComponent = fragment;
    }

    @Override
    public void loadComponent() {
        if (getContext().getFrame() != null) {
            resultComponent.setFrame(getContext().getFrame());
        }

        String src = element.attributeValue("src");
        if (src == null) {
            src = DEFAULT_DESCRIPTOR;
        }
        String runtimeDs = element.attributeValue("runtimeDs");
        if (StringUtils.isEmpty(runtimeDs)) {
            throw new GuiDevelopmentException("runtimePropsDatasource is not set for runtimeProperties component",
                    getContext().getFullFrameId());
        }
        getContext().getParams().put("runtimeDs", runtimeDs);

        String categoriesDs = element.attributeValue("categoriesDs");
        if (StringUtils.isEmpty(categoriesDs)) {
            throw new GuiDevelopmentException("categoriesDs is not set for runtimeProperties component",
                    getContext().getFullFrameId());
        }
        getContext().getParams().put("categoriesDs", categoriesDs);

        String rows = element.attributeValue("rows");
        getContext().getParams().put("rows", rows);
        String cols = element.attributeValue("cols");
        getContext().getParams().put("cols", cols);
        String fieldWidth = element.attributeValue("fieldWidth");
        getContext().getParams().put("fieldWidth", fieldWidth);
        String fieldCaptionWidth = element.attributeValue("fieldCaptionWidth");
        getContext().getParams().put("fieldCaptionWidth", fieldCaptionWidth);

        String screenPath = Objects.equals(src, DEFAULT_DESCRIPTOR) ? "runtimeProperties" : src;
        if (element.attributeValue("id") != null) {
            screenPath = element.attributeValue("id");
        }
        if (getContext().getFrame() != null) {
            String parentId = getContext().getFullFrameId();
            if (StringUtils.isNotEmpty(parentId)) {
                screenPath = parentId + "." + screenPath;
            }
        }

        StopWatch loadStopWatch = createStopWatch(ScreenLifeCycle.LOAD, screenPath);

        // if fragment has XML descriptor

        if (fragmentLoader != null) {
            fragmentLoader.loadComponent();
        }

        // load properties after inner context, they must override values defined inside of fragment

        assignXmlDescriptor(resultComponent, element);
        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);

        loadAlign(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);

        loadStopWatch.stop();

        // propagate init phases

        ComponentLoaderContext parentContext = (ComponentLoaderContext) getContext();
        if (innerContext != null) {

            parentContext.getInjectTasks().addAll(innerContext.getInjectTasks());
            parentContext.getInitTasks().addAll(innerContext.getInitTasks());
            parentContext.getPostInitTasks().addAll(innerContext.getPostInitTasks());
        }

        ScreenOptions options = parentContext.getOptions();
        parentContext.addInjectTask(new FragmentLoaderInjectTask((Fragment) resultComponent, options, beanLocator));
        parentContext.addInitTask(new FragmentLoaderInitTask((Fragment) resultComponent, options, (ComponentLoaderContext) context, beanLocator));
    }

    @SuppressWarnings("unchecked")
    protected WindowInfo createFakeWindowInfo(String src, String fragmentId) {
        Element screenElement = DocumentHelper.createElement("screen");
        screenElement.addAttribute("template", src);
        screenElement.addAttribute("id", fragmentId);

        ScreenXmlLoader screenXmlLoader = beanLocator.get(ScreenXmlLoader.NAME);

        Element windowElement = screenXmlLoader.load(src, fragmentId, Collections.emptyMap());
        Class<? extends ScreenFragment> fragmentClass;

        String className = windowElement.attributeValue("class");
        if (StringUtils.isNotEmpty(className)) {
            fragmentClass = (Class<? extends ScreenFragment>) getScripting().loadClassNN(className);
        } else {
            fragmentClass = AbstractFrame.class;
        }

        return new WindowInfo(fragmentId, new WindowAttributesProvider() {
            @Override
            public WindowInfo.Type getType(WindowInfo wi) {
                return WindowInfo.Type.FRAGMENT;
            }

            @Override
            public String getTemplate(WindowInfo wi) {
                return src;
            }

            @Nonnull
            @Override
            public Class<? extends FrameOwner> getControllerClass(WindowInfo wi) {
                return fragmentClass;
            }

            @Override
            public WindowInfo resolve(WindowInfo windowInfo) {
                return windowInfo;
            }
        }, screenElement);
    }

    protected String getMessagePack(String descriptorPath) {
        if (descriptorPath.contains("/")) {
            descriptorPath = StringUtils.substring(descriptorPath, 0, descriptorPath.lastIndexOf("/"));
        }

        String messagesPack = descriptorPath.replace("/", ".");
        int start = messagesPack.startsWith(".") ? 1 : 0;
        messagesPack = messagesPack.substring(start);
        return messagesPack;
    }

    protected <T extends ScreenFragment> T createController(@SuppressWarnings("unused") WindowInfo windowInfo,
                                                            @SuppressWarnings("unused") Fragment fragment,
                                                            Class<T> screenClass) {
        Constructor<T> constructor;
        try {
            constructor = screenClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new DevelopmentException("No accessible constructor for screen class " + screenClass);
        }

        T controller;
        try {
            controller = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create instance of screen class " + screenClass);
        }

        return controller;
    }

    protected WindowConfig getWindowConfig() {
        return beanLocator.get(WindowConfig.NAME);
    }
}