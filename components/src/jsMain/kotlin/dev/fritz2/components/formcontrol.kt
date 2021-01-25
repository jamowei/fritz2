package dev.fritz2.components

import SelectFieldComponent
import dev.fritz2.binding.Store
import dev.fritz2.components.FormControlComponent.Control
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextArea
import dev.fritz2.styling.StyleClass
import dev.fritz2.styling.className
import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.params.Style
import dev.fritz2.styling.params.styled
import dev.fritz2.styling.staticStyle
import dev.fritz2.styling.whenever
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import selectField


/**
 * This component class manages the configuration of a [formControl] and some render centric functionalities.
 * The former are important for clients of a [formControl], the latter for extending or changing the default behaviors.
 *
 * A [formControl] can be configured in different aspects:
 * - a label for a description of the control as a whole
 * - mark the control as _required_
 * - an optional helper text
 * - provide an error message as a [Flow<String>]]; if it is none empty, the message will get rendered.
 * - disable the control
 *
 * Customizing the control:
 *
 * To add a new control, extend this class and add a new control function that wraps the desired control component
 * factory function like [FormControlComponent.inputField], [FormControlComponent.selectField],
 * [FormControlComponent.checkbox], [FormControlComponent.checkboxGroup] and [FormControlComponent.radioGroup] do.
 *
 * In order to simply change the target of some of the default control wrapping function to a different control
 * component, extend this class and override the desired function. Be aware that you cannot provide default arguments
 * for an overridden function, so you must offer a new function with default arguments that just directs to
 * the overridden one.
 *
 * Be aware of the render strategy - pick whether your control should be rendered as a single control or a group.
 * - [SingleControlRenderer] for a control that consists of a single element
 * - [ControlGroupRenderer] for a control that consists of multiple parts (like checkBoxes etc.)
 *
 * If those do not fit, just implement the [ControlRenderer] interface and pair it with the string based key of the
 * related control wrapping function. Have a look at the init block, [renderStrategies] field and [Control.assignee]
 * field to learn how the mapping between control and rendering strategy is done.
 *
 */
// TODO: Add support for disable
// TODO: Add support for invalid for every control
@ComponentMarker
open class FormControlComponent {
    companion object {
        val staticCss = staticStyle(
            "formControl",
            """
                display: inline-flex;
                position: relative;
                vertical-align: middle;
                height: 2.5rem;
                appearance: none;
                align-items : center;
                justify-content: center;
                transition: all 250ms;
                white-space: nowrap;
                outline: none;
                width: 100%;
            """
        )

        const val invalidClassName = "invalid"

        val invalidCss: Style<BasicParams> = {
            boxShadow { danger }
            border {
                width { thin }
                style { solid }
                color { danger }
            }

            hover {
                border {
                    color { danger }
                }
            }

            focus {
                boxShadow { danger }
            }
        }

        object ControlNames {
            const val inputField = "inputField"
            const val textArea = "textArea"
            const val switch = "switch"
            const val selectField = "selectField"
            const val radioGroup = "radioGroup"
            const val checkbox = "checkbox"
            const val checkboxGroup = "checkboxGroup"
        }
    }

    class Control {

        private val overflows: MutableList<String> = mutableListOf()
        var assignee: Pair<String, (RenderContext.() -> Unit)>? = null

        fun set(controlName: String, component: (RenderContext.() -> Unit)) {
            if (assignee == null) {
                assignee = Pair(controlName, component)
            } else {
                overflows.add(controlName)
            }
        }

        fun assert() {
            if (overflows.isNotEmpty()) {
                console.error(
                    UnsupportedOperationException(
                        message = "Only one control within a formControl is allowed! Accepted control: ${assignee?.first}"
                                + " The following controls are not applied and overflow this form: "
                                + overflows.joinToString(", ")
                                + " Please remove those!"
                    )
                )
            }
        }
    }

    protected val renderStrategies: MutableMap<String, ControlRenderer> = mutableMapOf()
    protected val control = Control()

    var label: String = ""

    fun label(value: () -> String) {
        label = value()
    }

    var disabled: Flow<Boolean> = flowOf(false)

    fun disabled(value: () -> Flow<Boolean>) {
        disabled = value()
    }

    var required: Boolean = false

    fun required(value: Boolean) {
        required = value
    }

    var helperText: String? = null

    fun helperText(value: () -> String) {
        helperText = value()
    }

    var errorMessage: Flow<String> = flowOf("")

    fun errorMessage(value: () -> Flow<String>) {
        errorMessage = value()
    }

    init {
        renderStrategies[ControlNames.inputField] = SingleControlRenderer(this)
        renderStrategies[ControlNames.switch] = SingleControlRenderer(this)
        renderStrategies[ControlNames.textArea] = SingleControlRenderer(this)
        renderStrategies[ControlNames.selectField] = SingleControlRenderer(this)
        renderStrategies[ControlNames.checkbox] = SingleControlRenderer(this)
        renderStrategies[ControlNames.checkboxGroup] = ControlGroupRenderer(this)
        renderStrategies[ControlNames.radioGroup] = ControlGroupRenderer(this)
    }

    open fun inputField(
        styling: BasicParams.() -> Unit = {},
        store: Store<String>? = null,
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = ControlNames.inputField,
        init: Input.() -> Unit
    ) {
        val msg = errorMessage.map { it.isNotEmpty() }
        control.set(ControlNames.inputField)
        {
            inputField(styling, store, baseClass, id, prefix) {
                element {
                    className(StyleClass(invalidClassName).whenever(msg))
                    init()
                }
            }
        }
    }

    open fun switch(
        styling: BasicParams.() -> Unit = {},
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = ControlNames.switch,
        init: SwitchComponent.() -> Unit
    ) {
        val msg = errorMessage.map { it.isNotEmpty() }
        control.set(ControlNames.inputField)
        {
            switch(styling, baseClass, id, prefix) {
                className(StyleClass(invalidClassName).whenever(msg))
                init()
            }
        }
    }

    open fun textArea(
        styling: BasicParams.() -> Unit = {},
        store: Store<String>? = null,
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = ControlNames.textArea,
        init: TextArea.() -> Unit
    ) {
        val msg = errorMessage.map { it.isNotEmpty() }
        control.set(ControlNames.textArea)
        {
            textArea(styling, store, baseClass, id, prefix) {
                element {
                    className(StyleClass(invalidClassName).whenever(msg))
                    init()
                }
            }
        }
    }

    open fun checkbox(
        styling: BasicParams.() -> Unit = {},
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = ControlNames.checkbox,
        build: CheckboxComponent.() -> Unit
    ) {
        //val msg = errorMessage.map { it.isNotEmpty() }
        val msg = flowOf(true)

            control.set(ControlNames.checkbox)
            {
                checkbox({
                    styling()
                    StyleClass(invalidClassName).whenever(msg) { it }
                }, baseClass, id, prefix) {
                    build()
                }
            }


    }

    open fun <T>checkboxGroup(
        styling: BasicParams.() -> Unit = {},
        items: List<String>,
        store: Store<List<T>>,
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = ControlNames.checkboxGroup,
        build: CheckboxGroupComponent<T>.() -> Unit
    ) {
        control.set(ControlNames.checkboxGroup) {
            checkboxGroup(styling, items, store, baseClass, id, prefix) {
                build()
            }
        }
    }

    open fun <T>radioGroup(
        styling: BasicParams.() -> Unit = {},
        store: Store<T>,
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = ControlNames.radioGroup,
        build: RadioGroupComponent<T>.() -> Unit
    ) {
        control.set(ControlNames.radioGroup) {
            radioGroup(styling, store, baseClass, id, prefix) {
                build()
            }
        }
    }

    open fun <T>selectField(
        styling: BasicParams.() -> Unit = {},
        store: Store<T>,
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = ControlNames.selectField,
        init: SelectFieldComponent<T>.() -> Unit
    ) {
        val msg = errorMessage.map { it.isNotEmpty() }
        control.set(ControlNames.selectField)
        {
            selectField<T>(
                styling,
                store,
                baseClass,
                id,
                prefix
            ) {
                className(StyleClass(invalidClassName).whenever(msg))
                init()
            }
        }
    }

    fun render(
        styling: BasicParams.() -> Unit = {},
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = "formControl",
        renderContext: RenderContext
    ) {
        control.assignee?.second?.let {
            renderStrategies[control.assignee?.first]?.render(
                {
                    children(".$invalidClassName", invalidCss)
                    styling()
                }, baseClass, id, prefix, renderContext, it
            )
        }
        control.assert()
    }

    fun renderHelperText(renderContext: RenderContext) {
        renderContext.div {
            helperText?.let {
                (::p.styled {
                    color { dark }
                    fontSize { smaller }
                    lineHeight { smaller }
                }) { +it }
            }
        }
    }

    fun renderErrorMessage(renderContext: RenderContext) {
        renderContext.div {
            errorMessage.render {
                if (it.isNotEmpty()) {
                    lineUp({
                        color { danger }
                        fontSize { small }
                        lineHeight { small }
                    }) {
                        spacing { tiny }
                        items {
                            icon { fromTheme { warning } }
                            p { +it }
                        }
                    }
                }
            }
        }
    }

    val requiredMarker: RenderContext.() -> Unit = {
        if (required) {
            (::span.styled {
                color { danger }
                margins { left { tiny } }
            }) { +"*" }
        }
    }
}

interface ControlRenderer {
    fun render(
        styling: BasicParams.() -> Unit = {},
        baseClass: StyleClass? = null,
        id: String? = null,
        prefix: String = "formControl",
        renderContext: RenderContext,
        control: RenderContext.() -> Unit
    )
}

class SingleControlRenderer(private val component: FormControlComponent) : ControlRenderer {
    override fun render(
        styling: BasicParams.() -> Unit,
        baseClass: StyleClass?,
        id: String?,
        prefix: String,
        renderContext: RenderContext,
        control: RenderContext.() -> Unit
    ) {
        renderContext.stackUp(
            {
                alignItems { start }
                width { full }
                styling()
            },
            baseClass = baseClass,
            id = id,
            prefix = prefix
        ) {
            spacing { tiny }
            items {
                label {
                    +component.label
                    component.requiredMarker(this)
                }
                control(this)
                component.renderHelperText(this)
                component.renderErrorMessage(this)
            }
        }
    }

}

class ControlGroupRenderer(private val component: FormControlComponent) : ControlRenderer {
    override fun render(
        styling: BasicParams.() -> Unit,
        baseClass: StyleClass?,
        id: String?,
        prefix: String,
        renderContext: RenderContext,
        control: RenderContext.() -> Unit
    ) {
        renderContext.box({
            width { full }
        }) {
            (::fieldset.styled(baseClass, id, prefix) {
                styling()
            }) {
                legend { +component.label }
                control(this)
                component.renderHelperText(this)
                component.renderErrorMessage(this)
            }
        }
    }
}


/**
 * This component wraps input elements like [inputField], [selectField], [checkbox], [checkboxGroup], [radioGroup].
 * It enriches those controls with a describing text or label, an optional helper message and also an optional
 * error message. On top it marks a control as _required_ if that should be exposed.
 *
 * The controls themselves offer the same API as if used stand alone. They must be just declared within the build
 * parameter expression of this factory function.
 *
 * Be aware that only one control within a formControl is allowed! If more than one are configured, only the first will
 * get rendered; the remaining ones will be reported as errors in the log.
 *
 * This component can be customized in different ways and thus is quite flexible to...
 * - ... adapt to new input elements
 * - ... get rendered in a new way.
 * In order to achieve this, one can provide new implementations of the rendering strategies or override the control
 * wrapping functions as well. For details have a look at the [ControlRenderer] interface and the control functions
 * [FormControlComponent.inputField], [FormControlComponent.checkbox], [FormControlComponent.checkboxGroup],
 * [FormControlComponent.radioGroup], and [FormControlComponent.selectField].
 *
 * Have a look at some example calls
 * ```
 * // wrap an input field
 * formControl {
 *     label { "Some describing label" }
 *     required { true } // mark the above label with a small red star
 *     helperText { "You can provide a hint here" }
 *     // provide a Flow<String> where each none empty content will lead to the display of the error
 *     errorMessage { const("Sorry, always wrong in this case") }
 *     // just use the appropriate control with its specific API!
 *     inputField(store = someStore) {
 *         placeholder("Some text to type")
 *     }
 * }
 *
 * // providing more than one control results in errors:
 * // - the first will get rendered
 * // - starting with the second all others will be logged as errors
 * formControl {
 *     // leave out label and so on
 *     // ...
 *     // first control function called -> ok, will get rendered
 *     inputField(store = someStore) {
 *         placeholder("Some text to type")
 *     }
 *     // second call -> more than one control -> will not get rendered, but instead be logged as error!
 *     checkBox {
 *          checked { someStore.data }
 *          events {
 *              changes.states() handledBy someStore.someHandler
 *          }
 *     }
 *     // probably more calls to controls -> also reported as errors!
 * }
 * ```
 *
 * For details about the configuration options, have a look at [FormControlComponent].
 *
 * @see FormControlComponent
 *
 * @param styling a lambda expression for declaring the styling as fritz2's styling DSL
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param prefix the prefix for the generated CSS class resulting in the form ``$prefix-$hash``
 * @param build a lambda expression for setting up the component itself. Details in [FormControlComponent]
 */
fun RenderContext.formControl(
    styling: BasicParams.() -> Unit = {},
    baseClass: StyleClass? = null,
    id: String? = null,
    prefix: String = "formControl",
    build: FormControlComponent.() -> Unit = {}
) {
    val component = FormControlComponent().apply(build)
    component.render(styling, baseClass, id, prefix, this)
}

