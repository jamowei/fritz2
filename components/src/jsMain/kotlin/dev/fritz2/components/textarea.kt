package dev.fritz2.components

import dev.fritz2.binding.Store
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Textarea
import dev.fritz2.dom.values
import dev.fritz2.styling.StyleClass
import dev.fritz2.styling.StyleClass.Companion.plus
import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.params.Style
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.staticStyle
import dev.fritz2.styling.theme.TextareaResize
import dev.fritz2.styling.theme.TextareaSize
import dev.fritz2.styling.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * This class handles the configuration of an textarea element
 *
 * The textarea can be configured for the following aspects:
 *  - the size of the element
 *  - the direction of resizing
 *  - some predefined styles
 *  - a default value
 *
 *  - the base options of the HTML input element can be set.
 *  [Attributes](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/textarea#Attributes)
 *
 *  * For a detailed explanation and examples of usage have a look at the [textarea] function !
 *
 */
@ComponentMarker
class TextareaComponent {

    companion object {
        val staticCss = staticStyle(
        "textareaContainer",
        """
            outline: 0px;
            position: relative;
            appearance: none;
            transition: all 0.2s ease 0s;
            min-height: 80px;
            
            """
        )
    }

    val basicInputStyles: Style<BasicParams> = {

        radius { normal }
        fontWeight { normal }


        border {
            width { thin }
            style { solid }
            color { light }

        }

        background { color { "white" } }

        disabled {
            background {
                color { disabled }
            }

        }

        focus {
            border {
                color { "#3182ce" }
            }
            boxShadow { outline }
        }
    }


    var value: Flow<String>? = null
    fun value(value: () -> Flow<String>) {
        this.value = value()
    }

    var placeholder: Flow<String>? = null

    fun placeholder(value: String) {
        placeholder = flowOf(value)
    }

    fun placeholder(value: Flow<String>) {
        placeholder = value
    }

    fun placeholder(value: () -> Flow<String>) {
        placeholder = value()

    }

    var disable: Flow<Boolean> = flowOf(false)

    fun disable(value: Boolean) {
        disable = flowOf(value)
    }

    fun disable(value: Flow<Boolean>) {
        disable = value
    }

    fun disable(value: () -> Flow<Boolean>) {
        disable = value()
    }

    var size: TextareaSize.() -> Style<BasicParams> = { Theme().textarea.size.normal }
    fun size(value: TextareaSize.() -> Style<BasicParams>) {
        size = value
    }

    var resizeBehavior: TextareaResize.() -> Style<BasicParams> = { Theme().textarea.resize.vertical }
    fun resizeBehavior(value: TextareaResize.() -> Style<BasicParams>) {
        resizeBehavior = value
    }

    var base: (Textarea.() -> Unit)? = null

    fun base(value: Textarea.() -> Unit) {
        base = value
    }


}

/**
 * This component generates a textarea.
 *
 * You can optionally pass a store in order to set the value and react to updates automatically.
 *
 * To enable or disable it or to make it readOnly, just use the well known attributes of HTML.
 *
 * Possible values to set are( default *) :
 *  - size : small | normal * | large
 *  - resizeBehavior : none | vertical *| horizontal
 *  - placeholder : String | Flow<String>
 *  - disable : Boolean | Flow<Boolean>
 *  - value -> maybe you want to set an initial value instead of a placeholder
 *  - base -> basic properties of an textarea
 *
 *  textarea(store = dataStore) {
 *        placeholder { "My placeholder" }  // render a placeholder text for empty textarea
 *        resizeBehavior { horizontal }    // resize textarea horizontal
 *        size { small }                   // render a smaller textarea
 *     }
 *
 *
 *   textarea({ // use the styling parameter
 *            background {
 *                color { dark }
 *               }
 *               color { light }
 *               radius { "1rem" }},store = dataStore) {
 *
 *               disable(true)              // textarea is disabled
 *               resizeBehavior { none }    // resizing is not possible
 *               size { large }             // render a large textarea
 *
 *               }
 *
 *   textarea {
 *          value { dataStore.data }  // value depends on value in store
 *          disable(true)             // editing is disabled, but resizing still works
 *          }
 *
 *   textarea {
 *         base{                                        // you have access to base properties of a textarea
 *         placeholder("Here is a sample placeholder")
 *         changes.values() handledBy dataStore.update
 *                 }
 *          }
 *
 * @see TextareaComponent
 *
 * @param styling a lambda expression for declaring the styling as fritz2's styling DSL
 * @param store optional [Store] that holds the data of the textarea
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param prefix the prefix for the generated CSS class resulting in the form ``$prefix-$hash``
 * @param init a lambda expression for setting up the component itself. Details in [TextareaComponent]
 */


fun RenderContext.textarea(
    styling: BasicParams.() -> Unit = {},
    store: Store<String>? = null,
    baseClass: StyleClass? = null,
    id: String? = null,
    prefix: String = "textarea",
    init: TextareaComponent.() -> Unit
) {

    val component = TextareaComponent().apply(init)

    (::textarea.styled(styling, baseClass + TextareaComponent.staticCss, id, prefix) {
        component.resizeBehavior.invoke(Theme().textarea.resize)()
        component.size.invoke(Theme().textarea.size)()
        component.basicInputStyles()

    }){
        placeholder(component.placeholder ?: emptyFlow())
        disabled(component.disable)
        value(component.value ?: emptyFlow())
        component.base?.invoke(this)
        store?.let {
            value(it.data)
            changes.values() handledBy it.update

        }
    }
}