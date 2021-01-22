package dev.fritz2.components

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.watch
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.states
import dev.fritz2.identification.uniqueId
import dev.fritz2.styling.StyleClass
import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.params.Style
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.theme.CheckboxSizes
import dev.fritz2.styling.theme.IconDefinition
import dev.fritz2.styling.theme.Icons
import dev.fritz2.styling.theme.Theme
import kotlinx.coroutines.flow.*


/**
 * This class combines the _configuration_ and the core styling of a checkbox group.
 * The rendering itself is also done within the companion object.
 *
 * In order to render a checkbox group use the [checkboxGroup] factory function!
 *
 * This class offers the following _configuration_ features:
 *  - the items as a flowOf(List<T>)
 *  - the label(mapping) of a switch (static, dynamic via a [Flow<String>] or customized content of a Div.RenderContext ) the the example below
 *  - some predefined styling variants (size)
 *  - the style of the items (checkbox)
 *  - the style checked state
 *  - the style of the label
 *  - the checked icon ( use our icon library of our theme )
 *  - link an external boolean flow to set the disabled state of the box *
 *  - choose the direction of checkbox elements (row vs column)
 *
 *  This can be done within a functional expression that is the last parameter of the factory function, called
 *  ``build``. It offers an initialized instance of this [CheckboxGroupComponent] class as receiver, so every mutating
 *  method can be called for configuring the desired state for rendering the checkbox.
 *
 * Example usage
 * ```
 * // simple use case showing the core functionality
 * val options = listOf("A", "B", "C")
 * val myStore = storeOf(<List<String>>)
 * checkboxGroup(store = myStore) {
 *      items(flowOf(options)) or use items(options) // provide a list of items you can
 * }
 *
 * // use case showing some styling options and a store of List<Pair<Int,String>>
 *   val myPairs = listOf((1 to "ffffff"), (2 to "rrrrrr" ), (3 to "iiiiii"), (4 to "tttttt"), ( 5 to "zzzzzz"), (6 to "222222"))
 *  val myStore = storeOf(<List<Pair<Int,String>>)
 * checkboxGroup(store = myStore) {
 *      label(it.second)
 *      size { large }
 *      items(flowOf(options)) or use items(options) // provide a list of items you can
 *      checkedStyle {{
 *           background { color {"green"}}
 *      }}
 * }
 * ```
 */
class CheckboxGroupComponent<T> : InputFormProperties by InputForm() {
    companion object {
        object CheckboxGroupLayouts {
            val column: Style<BasicParams> = {
                display {
                    inlineGrid
                }
            }
            val row: Style<BasicParams> = {
                display {
                    inlineFlex
                }
            }
        }
    }

    var items = DynamicComponentProperty<List<T>>(flowOf(emptyList()))
    var icon = ComponentProperty<Icons.() -> IconDefinition> { Theme().icons.check }
    var label = ComponentProperty<(item: T) -> String> { it.toString() }
    var size = ComponentProperty<CheckboxSizes.() -> Style<BasicParams>> { Theme().checkbox.sizes.normal }

    var direction: Style<BasicParams> = CheckboxGroupLayouts.column
    fun direction(value: CheckboxGroupLayouts.() -> Style<BasicParams>) {
        direction = CheckboxGroupLayouts.value()
    }

    var itemStyle: Style<BasicParams> = { Theme().checkbox.default() }
    fun itemStyle(value: () -> Style<BasicParams>) {
        itemStyle = value()
    }

    var labelStyle: Style<BasicParams> = { Theme().checkbox.label() }
    fun labelStyle(value: () -> Style<BasicParams>) {
        labelStyle = value()
    }

    var checkedStyle: Style<BasicParams> = { Theme().checkbox.checked() }
    fun checkedStyle(value: () -> Style<BasicParams>) {
        checkedStyle = value()
    }

    // TODO
    var selectedItems: Flow<List<T>> = flowOf(emptyList())

    val selectionStore: SelectionStore<T> = SelectionStore()

    class EventsContext<T>(val selected: Flow<List<T>>) {
    }

    var eventsExpression: EventsContext<T>.() -> Unit = {}
    fun events(expr: EventsContext<T>.() -> Unit) {
        eventsExpression = expr
    }

}

// TODO
class SelectionStore<T> : RootStore<List<T>>(emptyList()) {
    val toggle = handleAndEmit<T, List<T>> { selectedRows, new ->
        val newSelection = if (selectedRows.contains(new))
            selectedRows - new
        else
            selectedRows + new
        emit(newSelection)
        newSelection
    }
}

/**
 * This component generates a *group* of checkboxes.
 *
 * You can set different kind of properties like the label text or different styling aspects like the colors of the
 * background, the label or the checked state. Further more there are configuration functions for accessing the checked
 * state of each box or totally disable it.
 * For a detailed overview about the possible properties of the component object itself, have a look at
 * [CheckboxGroupComponent]
 *
 * Example usage
 * ```
 * // simple use case showing the core functionality
 * val options = listOf("A", "B", "C")
 * val myStore = storeOf(<List<String>>)
 * checkboxGroup(store = myStore) {
 *      items(flowOf(options)) or use items(options) // provide a list of items you can
 * }
 *
 * // use case showing some styling options and a store of List<Pair<Int,String>>
 * val myPairs = listOf((1 to "ffffff"), (2 to "rrrrrr" ), (3 to "iiiiii"), (4 to "tttttt"), ( 5 to "zzzzzz"), (6 to "222222"))
 * val myStore = storeOf(<List<Pair<Int,String>>)
 * checkboxGroup(store = myStore) {
 *      label(it.second)
 *      size { large }
 *      items(flowOf(options)) or use items(options) // provide a list of items you can
 *      checkedStyle {{
 *           background { color { "green" }}
 *      }}
 * }
 * ```
 *
 * @see CheckboxGroupComponent
 *
 * @param styling a lambda expression for declaring the styling as fritz2's styling DSL
 * @param store a store of List<T>
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param prefix the prefix for the generated CSS class resulting in the form ``$prefix-$hash``
 * @param build a lambda expression for setting up the component itself. Details in [CheckboxGroupComponent]
 */
fun <T> RenderContext.checkboxGroup(
    styling: BasicParams.() -> Unit = {},
    store: Store<List<T>>,
    baseClass: StyleClass? = null,
    id: String? = null,
    prefix: String = "checkboxGroupComponent",
    build: CheckboxGroupComponent<T>.() -> Unit = {}
) {
    val component = CheckboxGroupComponent<T>().apply(build)

    val toggle = store.handle<T> { list, item ->
        if (list.contains(item)) {
            list - item
        } else {
            list + item
        }
    }


    val grpId = id ?: uniqueId()
    (::div.styled(styling, baseClass, id, prefix) {
        component.direction()
    }) {
        //component.selectionStore.update(store.current)
        //store?.let { it.data.watch() }
        //component.selectedItems.watch()
        //component.selectionStore.data.watch()
        //component.selectionStore.data.onEach { println(it) }
        //component.selectedItems handledBy component.selectionStore.update
        //component.selectionStore.syncBy(store.update)


        component.items.values.renderEach { item ->
            val checkedFlow = store.data.map { it.contains(item) }.distinctUntilChanged()
            checkbox(styling = component.itemStyle, id = grpId + "-grp-item-" + uniqueId()) {
                size { component.size.value.invoke(Theme().checkbox.sizes) }
                icon { component.icon.value(Theme().icons) }
                labelStyle { component.labelStyle }
                checkedStyle { component.checkedStyle }
                label(component.label.value(item))
                checked(checkedFlow)
                disabled(component.disabled.values)
                events {
                    changes.states().map { item } handledBy toggle
                    //changes.states().map { item } handledBy component.selectionStore.toggle
                }
            }
        }

        /*
        CheckboxGroupComponent.EventsContext(component.selectionStore.toggle).apply {
            component.eventsExpression(this)
            store?.let { selected handledBy it.update }
        }

         */
    }
}

