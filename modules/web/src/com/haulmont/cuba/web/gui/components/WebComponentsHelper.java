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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.ComponentContainer;
import com.haulmont.cuba.gui.components.KeyCombination.Modifier;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.web.gui.components.util.ShortcutListenerDelegate;
import com.haulmont.cuba.web.widgets.*;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.haulmont.cuba.gui.components.Component.AUTO_SIZE;

public class WebComponentsHelper {

    @SuppressWarnings("unchecked")
    public static <T extends Component> Collection<T> getComponents(HasComponents container, Class<T> aClass) {
        List<T> res = new ArrayList<>();
        for (Object aContainer : container) {
            Component component = (Component) aContainer;
            if (aClass.isAssignableFrom(component.getClass())) {
                res.add((T) component);
            } else if (HasComponents.class.isAssignableFrom(component.getClass())) {
                res.addAll(getComponents((HasComponents) component, aClass));
            }
        }

        return res;
    }

    /**
     * Returns underlying Vaadin component implementation.
     *
     * @param component GUI component
     * @return          Vaadin component
     * @see #getComposition(com.haulmont.cuba.gui.components.Component)
     */
    public static Component unwrap(com.haulmont.cuba.gui.components.Component component) {
        Object comp = component;
        while (comp instanceof com.haulmont.cuba.gui.components.Component.Wrapper) {
            comp = ((com.haulmont.cuba.gui.components.Component.Wrapper) comp).getComponent();
        }

        return (Component) comp;
    }

    /**
     * Returns underlying Vaadin component, which serves as the outermost container for the supplied GUI component.
     * For simple components like {@link com.haulmont.cuba.gui.components.Button} this method returns the same
     * result as {@link #unwrap(com.haulmont.cuba.gui.components.Component)}.
     *
     * @param component GUI component
     * @return          Vaadin component
     * @see #unwrap(com.haulmont.cuba.gui.components.Component)
     */
    public static Component getComposition(com.haulmont.cuba.gui.components.Component component) {
        Object comp = component;
        while (comp instanceof com.haulmont.cuba.gui.components.Component.Wrapper) {
            comp = ((com.haulmont.cuba.gui.components.Component.Wrapper) comp).getComposition();
        }

        return (Component) comp;
    }

    public static void expand(AbstractOrderedLayout layout, Component component, String height, String width) {
        if (!isHorizontalLayout(layout)
                && (StringUtils.isEmpty(height) || AUTO_SIZE.equals(height) || height.endsWith("%"))) {
            component.setHeight(100, Sizeable.Unit.PERCENTAGE);
        }

        if (!isVerticalLayout(layout)
                && (StringUtils.isEmpty(width) || AUTO_SIZE.equals(width) || width.endsWith("%"))) {
            component.setWidth(100, Sizeable.Unit.PERCENTAGE);
        }

        layout.setExpandRatio(component, 1);
    }

    public static boolean isComponentExpanded(com.haulmont.cuba.gui.components.Component component) {
        Component vComponent = WebComponentsHelper.getComposition(component);
        if (vComponent.getParent() instanceof AbstractOrderedLayout) {
            AbstractOrderedLayout layout = (AbstractOrderedLayout) vComponent.getParent();
            return (int)layout.getExpandRatio(vComponent) == 1;
        }

        return false;
    }

    public static boolean isVerticalLayout(AbstractOrderedLayout layout) {
        return (layout instanceof VerticalLayout)
                || (layout instanceof CubaVerticalActionsLayout);
    }

    public static boolean isHorizontalLayout(AbstractOrderedLayout layout) {
        return (layout instanceof HorizontalLayout)
                || (layout instanceof CubaHorizontalActionsLayout);
    }

    /**
     * todo rework, do not use WebButton here
     */
    @Deprecated
    public static CubaButton createButton(String icon) {
        ComponentsFactory cf = AppBeans.get(ComponentsFactory.NAME);
        com.haulmont.cuba.gui.components.Button button =
                cf.createComponent(com.haulmont.cuba.gui.components.Button.class);
        button.setIcon(icon);
        return (CubaButton) unwrap(button);
    }

    /**
     * Checks if the component should be visible to the client. Returns false if
     * the child should not be sent to the client, true otherwise.
     *
     * @param child The child to check
     * @return true if the child is visible to the client, false otherwise
     */
    public static boolean isComponentVisibleToClient(Component child) {
        if (!child.isVisible()) {
            return false;
        }
        HasComponents parent = child.getParent();

        if (parent instanceof SelectiveRenderer) {
            if (!((SelectiveRenderer) parent).isRendered(child)) {
                return false;
            }
        }

        if (parent != null) {
            return isComponentVisibleToClient(parent);
        } else {
            if (child instanceof UI) {
                // UI has no parent and visibility was checked above
                return true;
            } else {
                // Component which is not attached to any UI
                return false;
            }
        }
    }

    /**
     * Tests if component visible and its container visible.
     *
     * @param child component
     * @return component visibility
     */
    public static boolean isComponentVisible(Component child) {
        if (child.getParent() instanceof TabSheet) {
            TabSheet tabSheet = (TabSheet) child.getParent();
            TabSheet.Tab tab = tabSheet.getTab(child);
            if (!tab.isVisible()) {
                return false;
            }
        }

        if (child.getParent() instanceof CubaGroupBox) {
            // ignore groupbox content container visibility
            return isComponentVisible(child.getParent());
        }

        return child.isVisible() && (child.getParent() == null || isComponentVisible(child.getParent()));
    }

    /**
     * Tests if component enabled and visible and its container enabled.
     *
     * @param child component
     * @return component enabled state
     */
    public static boolean isComponentEnabled(Component child) {
        if (child.getParent() instanceof TabSheet) {
            TabSheet tabSheet = (TabSheet) child.getParent();
            TabSheet.Tab tab = tabSheet.getTab(child);
            if (!tab.isEnabled()) {
                return false;
            }
        }

        return child.isEnabled() && (child.getParent() == null || isComponentEnabled(child.getParent())) &&
                isComponentVisible(child);
    }

    public static boolean convertFieldGroupCaptionAlignment(FieldGroup.FieldCaptionAlignment captionAlignment) {
        if (captionAlignment == FieldGroup.FieldCaptionAlignment.LEFT)
            return true;
        else
            return false;
    }

    public static void setClickShortcut(Button button, String shortcut) {
        KeyCombination closeCombination = KeyCombination.create(shortcut);
        int[] closeModifiers = Modifier.codes(closeCombination.getModifiers());
        int closeCode = closeCombination.getKey().getCode();

        button.setClickShortcut(closeCode, closeModifiers);
    }

    @Deprecated
    public static void addEnterShortcut(TextField textField, Runnable runnable) {
        CubaTextField cubaTextField = textField.unwrap(CubaTextField.class);

        cubaTextField.addShortcutListener(
                new ShortcutListenerDelegate("", ShortcutAction.KeyCode.ENTER, null)
                    .withHandler((sender, target) ->
                            runnable.run()
                    ));
    }

    @Deprecated
    public static void focusProblemComponent(ValidationErrors errors) {
        com.haulmont.cuba.gui.components.Component component = null;
        if (!errors.getAll().isEmpty()) {
            component = errors.getAll().get(0).component;
        }
        if (component != null) {
            ComponentsHelper.focusComponent(component);
        }
    }

    public static ShortcutTriggeredEvent getShortcutEvent(com.haulmont.cuba.gui.components.Component source,
                                                          Component target) {
        Component vaadinSource = getVaadinSource(source);

        if (vaadinSource == target) {
            return new ShortcutTriggeredEvent(source, source);
        }

        if (source instanceof ComponentContainer) {
            ComponentContainer container = (ComponentContainer) source;
            com.haulmont.cuba.gui.components.Component childComponent =
                    findChildComponent(container, target);
            return new ShortcutTriggeredEvent(source, childComponent);
        }

        return new ShortcutTriggeredEvent(source, null);
    }

    protected static Component getVaadinSource(com.haulmont.cuba.gui.components.Component source) {
        Component component = source.unwrapComposition(Component.class);
        if (component instanceof AbstractSingleComponentContainer) {
            return ((AbstractSingleComponentContainer) component).getContent();
        }

        if (component instanceof CubaScrollBoxLayout) {
            return ((CubaScrollBoxLayout) component).getComponent(0);
        }

        return component;
    }

    /**
     * @return the direct child component of the layout which contains the component involved to event
     */
    protected static Component getDirectChildComponent(Component targetComponent, Component vaadinSource) {
        while (targetComponent != null
                && targetComponent.getParent() != vaadinSource) {
            targetComponent = targetComponent.getParent();
        }

        if (targetComponent instanceof CubaFieldWrapper) {
            targetComponent = ((CubaFieldWrapper) targetComponent).getContent();
        }

        return targetComponent;
    }

    @Nullable
    protected static com.haulmont.cuba.gui.components.Component findChildComponent(ComponentContainer container,
                                                                                   Component target) {
        Component vaadinSource = getVaadinSource(container);
        Collection<com.haulmont.cuba.gui.components.Component> components = container.getOwnComponents();

        return findChildComponent(components, vaadinSource, target);
    }

    @Nullable
    protected static com.haulmont.cuba.gui.components.Component findChildComponent(FieldGroup fieldGroup,
                                                                                   Component target) {
        Component vaadinSource = fieldGroup.unwrap(CubaFieldGroupLayout.class);
        Collection<com.haulmont.cuba.gui.components.Component> components = fieldGroup.getFields().stream()
                .map(FieldGroup.FieldConfig::getComponentNN)
                .collect(Collectors.toList());

        return findChildComponent(components, vaadinSource, target);
    }

    protected static com.haulmont.cuba.gui.components.Component findChildComponent(
            Collection<com.haulmont.cuba.gui.components.Component> components,
            Component vaadinSource, Component target) {
        Component targetComponent = getDirectChildComponent(target, vaadinSource);

        for (com.haulmont.cuba.gui.components.Component component : components) {
            Component unwrapped = component.unwrapComposition(Component.class);
            if (unwrapped == targetComponent) {
                com.haulmont.cuba.gui.components.Component child = null;

                if (component instanceof ComponentContainer) {
                    child = findChildComponent((ComponentContainer) component, target);
                }

                if (component instanceof HasButtonsPanel) {
                    ButtonsPanel buttonsPanel = ((HasButtonsPanel) component).getButtonsPanel();
                    if (getVaadinSource(buttonsPanel) == target) {
                        return buttonsPanel;
                    } else {
                        child = findChildComponent(buttonsPanel, target);
                    }
                }

                if (component instanceof FieldGroup) {
                    FieldGroup fieldGroup = (FieldGroup) component;
                    child = findChildComponent(fieldGroup, target);
                }

                return child != null ? child : component;
            }
        }
        return null;
    }
}