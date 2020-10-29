import dev.fritz2.binding.const
import dev.fritz2.binding.handledBy
import dev.fritz2.components.flexBox
import dev.fritz2.components.styled
import dev.fritz2.components.themeProvider
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.render
import dev.fritz2.dom.mount
import dev.fritz2.routing.router
import dev.fritz2.styling.StyleClass
import dev.fritz2.styling.params.BasicParams
import dev.fritz2.styling.theme.currentTheme
import dev.fritz2.styling.theme.render
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf

/*
val themes = listOf<Pair<String, ExtendedTheme>>(
    ("small Fonts") to SmallFonts(),
    ("large Fonts") to LargeFonts()
)

 */


fun HtmlElements.myLink(
    styling: BasicParams.() -> Unit = {},
    baseClass: StyleClass? = null,
    id: String? = null,
    prefix: String = "my-link",
    init: A.() -> Unit
): A =
    ::a.styled(styling, baseClass, id, prefix) {
        fontSize { giant }
    }(init)


fun HtmlElements.myRedLink(
    styling: BasicParams.() -> Unit = {},
    baseClass: StyleClass? = null,
    id: String? = null,
    prefix: String = "my-red-link",
    init: A.() -> Unit
): A =
    ::myLink.styled(styling, baseClass, id, prefix) {
        color { danger }
    }(init)


fun HtmlElements.myBorderedRedLink(
    styling: BasicParams.() -> Unit = {},
    baseClass: StyleClass? = null,
    id: String? = null,
    prefix: String = "mbrl",
    init: A.() -> Unit
): A =
    ::myRedLink.styled(styling, baseClass, id, prefix) {
        border {
            width { "1px" }
        }
    }(init)


@ExperimentalCoroutinesApi
fun main() {
    val themes = listOf<ExtendedTheme>(
        SmallFonts(),
        LargeFonts()
    )

    val router = router("")

    render { theme: ExtendedTheme ->
        themeProvider {
            // grab the ThemeStore to pass it down towards components, that need to access either the
            // data or to change the selected theme
            val store = themeStore
            // could be the default? (So only need to set it to ``false`` actively?)
            resetCss { true }
            // Override default theme by a new list of themes
            themes { themes }
            items {
                section {
                    flexBox({
                        height { "60px" }
                        wrap { nowrap }
                        direction { row }
                        justifyContent { spaceEvenly }
                        alignItems { center }
                    }) {
                        (::a.styled {
                            fontSize { large }
                        }) {
                            +"text"
                            href = const("#text")
                        }
                        (::a.styled {
                            fontSize { large }
                        }) {
                            +"flexBox"
                            href = const("#flexBoxDemo")
                        }
                        (::a.styled {
                            fontSize { large }
                        }) {
                            +"gridBox"
                            href = const("#gridBoxDemo")
                        }
                        (::a.styled {
                            fontSize { large }
                        }) {
                            +"input"
                            href = const("#input")
                        }
                        (::a.styled {
                            fontSize { large }
                        }) {
                            +"formcontrol"
                            href = const("#formcontrol")
                        }
                        (::a.styled {
                            fontSize { large }
                        }) {
                            +"stack"
                            href = const("#stack")
                        }
                    }
                    router.render { site ->
                        when (site) {
                            "input" -> inputDemo()
//                    "buttons" -> buttonDemo(theme)
                            "formcontrol" -> formControlDemo()
                            "text" -> textDemo()
                            "flexBoxDemo" -> flexBoxDemo(store, themes, theme)
                            "gridBoxDemo" -> gridBoxDemo()
                            else -> stackDemo(theme)
                        }
                    }.bind()
                }
            }
        }
    }.mount("target")
}