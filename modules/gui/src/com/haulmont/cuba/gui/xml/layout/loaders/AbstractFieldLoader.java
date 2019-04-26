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

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.gui.components.Buffered;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.HasDatatype;
import com.haulmont.cuba.gui.components.validation.*;
import com.haulmont.cuba.gui.components.validators.EmailValidator;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.*;
import java.util.function.BiFunction;

public abstract class AbstractFieldLoader<T extends Field> extends AbstractDatasourceComponentLoader<T> {

    protected static final Map<String, String> validatorsMap;

    static {
        Map<String, String> validators = new HashMap<>(18);
        validators.put("decimalMin", DecimalMinValidator.NAME);
        validators.put("decimalMax", DecimalMaxValidator.NAME);
        validators.put("digits", DigitsValidator.NAME);
        validators.put("futureOrPresent", FutureOrPresentValidator.NAME);
        validators.put("future", FutureValidator.NAME);
        validators.put("max", MaxValidator.NAME);
        validators.put("min", MinValidator.NAME);
        validators.put("negativeOrZero", NegativeOrZeroValidator.NAME);
        validators.put("negative", NegativeValidator.NAME);
        validators.put("notBlank", NotBlankValidator.NAME);
        validators.put("notEmpty", NotEmptyValidator.NAME);
        validators.put("notNull", NotNullValidator.NAME);
        validators.put("pastOrPresent", PastOrPresentValidator.NAME);
        validators.put("past", PastValidator.NAME);
        validators.put("positiveOrZero", PositiveOrZeroValidator.NAME);
        validators.put("positive", PositiveValidator.NAME);
        validators.put("regexp", RegexpValidator.NAME);
        validators.put("size", SizeValidator.NAME);
        validatorsMap = Collections.unmodifiableMap(validators);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);
        assignXmlDescriptor(resultComponent, element);

        loadContainer(resultComponent, element);
        if (resultComponent.getValueSource() == null) {
            loadDatasource(resultComponent, element);
        }

        loadVisible(resultComponent, element);
        loadEditable(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);

        loadCaption(resultComponent, element);
        loadIcon(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);

        loadValidators(resultComponent, element);
        loadValidation(resultComponent, element);

        loadRequired(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);
        loadAlign(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);
    }

    protected void loadRequired(Field component, Element element) {
        String required = element.attributeValue("required");
        if (StringUtils.isNotEmpty(required)) {
            component.setRequired(Boolean.parseBoolean(required));
        }

        String requiredMessage = element.attributeValue("requiredMessage");
        if (requiredMessage != null) {
            component.setRequiredMessage(loadResourceString(requiredMessage));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadValidators(Field component, Element element) {
        List<Element> validatorElements = element.elements("validator");

        if (!validatorElements.isEmpty()) {
            for (Element validatorElement : validatorElements) {
                Field.Validator validator = loadValidator(validatorElement);
                if (validator != null) {
                    component.addValidator(validator);
                }
            }

        } else if (component.getDatasource() != null) {
            MetaProperty property = component.getMetaProperty();
            Field.Validator validator = getDefaultValidator(property);
            if (validator != null) {
                component.addValidator(validator);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadValidation(Field component, Element element) {
        Element validatorsHolder = element.element("validators");
        if (validatorsHolder != null) {
            List<Element> validators = validatorsHolder.elements();

            for (Element validator : validators) {
                String beanName =  validatorsMap.get(validator.getName());
                if (beanName != null) {
                    component.addValidator(beanLocator.getPrototype(beanName, validator, getMessagesPack()));
                } else if (validator.getName().equals("email")) {
                    component.addValidator(new EmailValidator(validator, getMessagesPack()));
                }
            }
        }
    }

    protected void loadBuffered(Buffered component, Element element) {
        String buffered = element.attributeValue("buffered");
        if (StringUtils.isNotEmpty(buffered)) {
            component.setBuffered(Boolean.parseBoolean(buffered));
        }
    }

    protected void loadDatatype(HasDatatype component, Element element) {
        String datatypeAttribute = element.attributeValue("datatype");
        if (StringUtils.isNotEmpty(datatypeAttribute)) {
            //noinspection unchecked
            component.setDatatype(Datatypes.get(datatypeAttribute));
        }
    }
}