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

package spec.cuba.web.components.optionsgroup

import com.haulmont.cuba.gui.components.OptionsGroup
import com.haulmont.cuba.gui.screen.OpenMode
import com.haulmont.cuba.security.app.UserManagementService
import com.haulmont.cuba.web.testmodel.sales.OrderLine
import com.haulmont.cuba.web.testsupport.TestServiceProxy
import spec.cuba.web.UiScreenSpec
import spec.cuba.web.components.optionsgroup.screens.OptionsGroupTestScreen

import java.util.function.Consumer

class OptionsGroupTest extends UiScreenSpec {

    @SuppressWarnings(["GroovyAssignabilityCheck", "GroovyAccessibility"])
    void setup() {
        TestServiceProxy.mock(UserManagementService, Mock(UserManagementService) {
            getSubstitutedUsers(_) >> Collections.emptyList()
        })

        exportScreensPackages(['spec.cuba.web.components.optionsgroup.screens', 'com.haulmont.cuba.web.app.main'])
    }

    def "Value is propagated to ValueSource and entity from multiselect OptionsGroup"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def testScreen = screens.create(OptionsGroupTestScreen)
        testScreen.show()

        def optionsGroup = testScreen.optionsGroup as OptionsGroup<List<OrderLine>, OrderLine>

        def order = testScreen.orderDc.item
        def orderLine = testScreen.allOrderLinesDc.getItems().get(0)

        when: "Collection value is set to OptionsGroup"
        optionsGroup.setValue([orderLine])

        then: "Property CollectionContainer and Entity are updated"
        order.orderLines.size() == 1 && order.orderLines.contains(orderLine)
        testScreen.orderLinesDc.items.size() == 1 && testScreen.orderLinesDc.items.contains(orderLine)
    }

    def "Value is propagated to multiselect OptionsGroup and entity from ValueSource"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def testScreen = screens.create(OptionsGroupTestScreen)
        testScreen.show()

        def optionsGroup = testScreen.optionsGroup as OptionsGroup<List<OrderLine>, OrderLine>

        def order = testScreen.orderDc.item
        def orderLine = testScreen.allOrderLinesDc.getItems().get(0)

        when: "Collection value is set to ValueSource"
        testScreen.orderLinesDc.mutableItems.add(orderLine)

        then: "OptionsGroup and entity property values are updated"
        optionsGroup.value.size() == 1 && optionsGroup.value.contains(orderLine)
        order.orderLines.size() == 1 && order.orderLines.contains(orderLine)
    }

    def "Value is propagated to ValueSource and entity from single select OptionsGroup"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def testScreen = screens.create(OptionsGroupTestScreen)
        testScreen.show()

        def singleOptionGroup = testScreen.singleOptionGroup

        def orderLine = testScreen.orderLineDc.item
        def product = testScreen.allProductsDc.items.get(0)

        when: "A value is set to single select OptionsGroup"
        singleOptionGroup.setValue(product)

        then: "ValueSource and entity property are updated"
        testScreen.productDc.item == product
        orderLine.product == product
    }

    def "Value is propagated to single select OptionsGroup from ValueSource"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def testScreen = screens.create(OptionsGroupTestScreen)
        testScreen.show()

        def singleOptionGroup = testScreen.singleOptionGroup

        def product = testScreen.allProductsDc.items.get(0)

        when: "A value is set to ValueSource"
        testScreen.orderLineDc.item.product = product

        then: "Single select OptionsGroup is updated"
        singleOptionGroup.value == product
    }

    def "ValueChangeEvent is fired exactly once for multiselect OptionsGroup"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def testScreen = screens.create(OptionsGroupTestScreen)
        testScreen.show()

        def optionsGroup = testScreen.optionsGroup as OptionsGroup<List<OrderLine>, OrderLine>

        def valueChangeListener = Mock(Consumer)

        optionsGroup.addValueChangeListener(valueChangeListener)

        def order = testScreen.orderDc.item

        def orderLine = testScreen.allOrderLinesDc.items.get(0)
        def secondOrderLine = testScreen.allOrderLinesDc.items.get(1)

        when: "A value is set to OptionsGroup"
        optionsGroup.setValue([orderLine])

        then: "ValueChangeEvent is fired once"
        1 * valueChangeListener.accept(_)

        when: "An option is added to ValueSource"
        testScreen.orderLinesDc.mutableItems.add(secondOrderLine)

        then: "ValueChangeEvent is fired once"
        1 * valueChangeListener.accept(_)

        when: "Entity property value is set to null"
        order.orderLines = null

        then: "ValueChangeEvent is fired once"
        1 * valueChangeListener.accept(_)
    }
}
