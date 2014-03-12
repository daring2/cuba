/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.cuba.gui.app.core.entityinspector;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.Session;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ExcelAction;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.global.UserSession;

import javax.inject.Inject;
import java.util.*;

/**
 * @author korotkov
 * @version $Id$
 */
public class EntityInspectorBrowse extends AbstractLookup {

    public static final String SCREEN_NAME = "entityInspector.browse";
    public static final WindowManager.OpenType WINDOW_OPEN_TYPE = WindowManager.OpenType.THIS_TAB;
    public static final String DEFAULT_FIELD_WIDTH = "300";

    @Inject
    protected Metadata metadata;

    @Inject
    protected BoxLayout lookupBox;

    @Inject
    protected BoxLayout tableBox;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected DsContext dsContext;

    @Inject
    protected Filter filter;

    protected LookupField entities;
    protected Button showButton;
    protected Table entitiesTable;

    /**
     * Buttons
     */
    protected Button createButton;
    protected Button editButton;
    protected Button removeButton;
    protected Button excelButton;
    protected Button refreshButton;

    protected CollectionDatasource entitiesDs;
    protected MetaClass selectedMeta;

    @Override
    public void init(Map<String, Object> params) {
        String entityName = (String) params.get("entity");
        if (entityName != null) {
            Session session = metadata.getSession();
            selectedMeta = session.getClass(entityName);
            createEntitiesTable(selectedMeta);
            if (frame instanceof Lookup) {
                setLookupComponent(entitiesTable);
            }
        } else {
            entities = componentsFactory.createComponent(LookupField.NAME);
            entities.setWidth(DEFAULT_FIELD_WIDTH);
            entities.setOptionsMap(getEntitiesLookupFieldOptions());
            entities.setId("entitiesLookup");
            entities.setFrame(frame);

            showButton = componentsFactory.createComponent(Button.NAME);
            showButton.setIcon("icons/refresh.png");
            showButton.setAction(new ShowButtonAction("show"));
            showButton.setCaption(messages.getMessage(EntityInspectorBrowse.class, "show"));
            showButton.setId("showButton");
            showButton.setFrame(frame);

            lookupBox.add(entities);
            lookupBox.add(showButton);
        }
    }

    protected Map<String, Object> getEntitiesLookupFieldOptions() {
        Map<String, Object> options = new TreeMap<>();

        for (MetaClass metaClass : metadata.getTools().getAllPersistentMetaClasses()) {
            if (readPermitted(metaClass)) {
                Class javaClass = metaClass.getJavaClass();
                if (Entity.class.isAssignableFrom(javaClass)) {
                    options.put(messages.getTools().getEntityCaption(metaClass) + " (" + metaClass.getName() + ")", metaClass);
                }
            }
        }

        return options;
    }

    protected class ShowButtonAction extends AbstractAction {
        protected ShowButtonAction(String id) {
            super(id);
        }

        @Override
        public void actionPerform(Component component) {
            selectedMeta = entities.getValue();
            if (selectedMeta != null) {
                createEntitiesTable(selectedMeta);

                //TODO: set tab caption
                //EntityInspectorBrowse.this.setCaption(selectedMeta.getName());
            }
        }
    }

    protected void createEntitiesTable(MetaClass meta) {
        if (entitiesTable != null)
            tableBox.remove(entitiesTable);

        entitiesTable = componentsFactory.createComponent(Table.NAME);
        entitiesTable.setFrame(frame);

        //collect properties in order to add non-system columns first
        LinkedList<Table.Column> nonSystemPropertyColumns = new LinkedList<>();
        LinkedList<Table.Column> systemPropertyColumns = new LinkedList<>();
        for (MetaProperty metaProperty : meta.getProperties()) {
            //don't show embedded & multiple referred entities
            if (isEmbedded(metaProperty))
                continue;
            if (metaProperty.getRange().getCardinality().isMany())
                continue;

            Table.Column column = new Table.Column(meta.getPropertyPath(metaProperty.getName()));
            if (!metadata.getTools().isSystem(metaProperty)) {
                column.setCaption(getPropertyCaption(meta, metaProperty));
                nonSystemPropertyColumns.add(column);
            } else {
                column.setCaption(metaProperty.getName());
                systemPropertyColumns.add(column);
            }
        }
        for (Table.Column column : nonSystemPropertyColumns)
            entitiesTable.addColumn(column);

        for (Table.Column column : systemPropertyColumns)
            entitiesTable.addColumn(column);

        if (entitiesDs != null) {
            ((DsContextImplementation) getDsContext()).unregister(entitiesDs);
        }

        entitiesDs = new DsBuilder(getDsContext())
                .setId("entitiesDs")
                .setMetaClass(meta)
                .setView(createView(meta))
                .buildCollectionDatasource();

        entitiesDs.setQuery(String.format("select e from %s e", meta.getName()));

        entitiesTable.setDatasource(entitiesDs);

        tableBox.add(entitiesTable);
        entitiesTable.setWidth("100%");
        tableBox.expand(entitiesTable);

        ButtonsPanel buttonsPanel = createButtonsPanel();
        entitiesTable.setButtonsPanel(buttonsPanel);

        RowsCount rowsCount = componentsFactory.createComponent(RowsCount.NAME);
        rowsCount.setDatasource(entitiesDs);
        entitiesTable.setRowsCount(rowsCount);

        Action editAction = new EditAction();
        entitiesTable.setEnterPressAction(editAction);
        entitiesTable.setItemClickAction(editAction);
        entitiesTable.setMultiSelect(true);
        filter.setDatasource(entitiesDs);
        filter.setVisible(true);
        filter.apply(true);
    }

    protected boolean isEmbedded(MetaProperty metaProperty) {
        return metaProperty.getAnnotatedElement().isAnnotationPresent(javax.persistence.Embedded.class);
    }

    protected ButtonsPanel createButtonsPanel() {
        ButtonsPanel buttonsPanel = componentsFactory.createComponent("buttonsPanel");

        createButton = componentsFactory.createComponent(Button.NAME);
        createButton.setCaption(messages.getMessage(EntityInspectorBrowse.class, "create"));
        createButton.setAction(new CreateAction());
        createButton.setIcon("icons/create.png");
        createButton.setFrame(frame);

        editButton = componentsFactory.createComponent(Button.NAME);
        editButton.setCaption(messages.getMessage(EntityInspectorBrowse.class, "edit"));
        EditAction editAction = new EditAction();
        editButton.setAction(editAction);
        entitiesDs.addListener(editAction);
        editButton.setIcon("icons/edit.png");
        editButton.setFrame(frame);

        removeButton = componentsFactory.createComponent(Button.NAME);
        removeButton.setCaption(messages.getMessage(EntityInspectorBrowse.class, "remove"));
        RemoveAction removeAction = new RemoveAction(entitiesTable);
        entitiesDs.addListener(removeAction);
        removeButton.setAction(removeAction);
        removeButton.setIcon("icons/remove.png");
        removeButton.setFrame(frame);

        excelButton = componentsFactory.createComponent(Button.NAME);
        excelButton.setCaption(messages.getMessage(EntityInspectorBrowse.class, "excel"));
        excelButton.setAction(new ExcelAction(entitiesTable));
        excelButton.setIcon("icons/excel.png");
        excelButton.setFrame(frame);

        refreshButton = componentsFactory.createComponent(Button.NAME);
        refreshButton.setCaption(messages.getMessage(EntityInspectorBrowse.class, "refresh"));
        refreshButton.setAction(new RefreshAction(entitiesTable));
        refreshButton.setIcon("icons/refresh.png");
        refreshButton.setFrame(frame);

        buttonsPanel.addButton(createButton);
        buttonsPanel.addButton(editButton);
        buttonsPanel.addButton(removeButton);
        buttonsPanel.addButton(excelButton);
        buttonsPanel.addButton(refreshButton);
        return buttonsPanel;
    }

    protected View createView(MetaClass meta) {
        View view = new View(meta.getJavaClass(), false);
        for (MetaProperty metaProperty : meta.getProperties()) {
            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    view.addProperty(metaProperty.getName());
                    break;
                case ASSOCIATION:
                case COMPOSITION:
                    View minimal = metadata.getViewRepository()
                            .getView(metaProperty.getRange().asClass(), View.MINIMAL);
                    View propView = new View(minimal, metaProperty.getName() + "Ds", false);
                    view.addProperty(metaProperty.getName(), propView);
                    break;
                default:
                    throw new IllegalStateException("unknown property type");
            }
        }
        return view;
    }

    protected class CreateAction extends AbstractAction {

        public CreateAction() {
            super("create");
        }

        @Override
        public void actionPerform(Component component) {
            Map<String, Object> editorParams = new HashMap<>();
            editorParams.put("metaClass", selectedMeta.getName());
            Window window = openWindow("entityInspector.edit", WINDOW_OPEN_TYPE, editorParams);
            window.addListener(new CloseListener() {
                @Override
                public void windowClosed(String actionId) {
                    entitiesDs.refresh();
                    entitiesTable.requestFocus();
                }
            });
        }
    }

    protected class EditAction extends ItemTrackingAction {

        public EditAction() {
            super("edit");
        }

        @Override
        public void actionPerform(Component component) {
            Set selected = entitiesTable.getSelected();
            if (selected.size() != 1)
                return;

            Entity item = (Entity) selected.toArray()[0];
            Map<String, Object> editorParams = new HashMap<>();
            editorParams.put("item", item);
            Window window = openWindow("entityInspector.edit", WINDOW_OPEN_TYPE, editorParams);
            window.addListener(new CloseListener() {
                @Override
                public void windowClosed(String actionId) {
                    entitiesDs.refresh();
                    entitiesTable.requestFocus();
                }
            });
        }
    }

    protected String getPropertyCaption(MetaClass meta, MetaProperty metaProperty) {
        int idx = meta.getName().indexOf('$') + 1;
        return messages.getMessage(meta.getJavaClass(), meta.getName().substring(idx)
                + "." + metaProperty.getFullName());
    }

    protected boolean readPermitted(MetaClass metaClass) {
        return entityOpPermitted(metaClass, EntityOp.READ);
    }

    protected boolean entityOpPermitted(MetaClass metaClass, EntityOp entityOp) {
        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
        return session.isEntityOpPermitted(metaClass, entityOp);
    }
}