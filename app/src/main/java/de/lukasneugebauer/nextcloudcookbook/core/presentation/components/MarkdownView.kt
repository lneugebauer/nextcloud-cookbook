package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import timber.log.Timber

@Composable
fun MarkdownView(
    markdown: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    fun trimTrailingNewline(cs: CharSequence): CharSequence {
        var text = cs
        while (text[text.length - 1] == '\n') {
            text = text.subSequence(0, text.length - 1)
        }
        return text
    }

    val extensions: List<Extension> = remember { listOf(StrikethroughExtension.create()) }
    val parser: Parser = remember { Parser.builder().extensions(extensions).build() }
    val document: Node = parser.parse(markdown)
    val renderer: HtmlRenderer =
        remember {
            HtmlRenderer
                .builder()
                .extensions(extensions)
                .build()
        }
    val html = renderer.render(document)

    val style = LocalTextStyle.current.copy(color = LocalContentColor.current)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, style.fontSize.value)
                setTextColor(style.color.toArgb())
                setOnClickListener {
                    onClick.invoke()
                }
                setOnLongClickListener {
                    onLongClick.invoke()
                    true
                }
                isClickable = true
            }
        },
        update = {
            it.text = trimTrailingNewline(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT))
        }
    )
}

@Preview
@Composable
private fun SmallMarkdownPreview() {
    NextcloudCookbookTheme {
        Surface {
            MarkdownView(
                markdown = """
                    Lorem Ipsum
                """.trimIndent(),
                //style = LocalTextStyle.current.copy(color = MaterialTheme.colors.onSurface)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewMarkdownView() {
    NextcloudCookbookTheme {
        Surface {
            MarkdownView(
                markdown =
                    """
                    ---
                    __Advertisement :)__
                    
                    - __[pica](https://nodeca.github.io/pica/demo/)__ - high quality and fast image
                      resize in browser.
                    - __[babelfish](https://github.com/nodeca/babelfish/)__ - developer friendly
                      i18n with plurals support and easy syntax.
                    
                    You will like those projects!
                    
                    ---
                    
                    # h1 Heading 8-)
                    ## h2 Heading
                    ### h3 Heading
                    #### h4 Heading
                    ##### h5 Heading
                    ###### h6 Heading
                    
                    
                    ## Horizontal Rules
                    
                    ___
                    
                    ---
                    
                    ***
                    
                    
                    ## Typographic replacements
                    
                    Enable typographer option to see result.
                    
                    (c) (C) (r) (R) (tm) (TM) (p) (P) +-
                    
                    test.. test... test..... test?..... test!....
                    
                    !!!!!! ???? ,,  -- ---
                    
                    "Smartypants, double quotes" and 'single quotes'
                    
                    
                    ## Emphasis
                    
                    **This is bold text**
                    
                    __This is bold text__
                    
                    *This is italic text*
                    
                    _This is italic text_
                    
                    ~~Strikethrough~~
                    
                    
                    ## Blockquotes
                    
                    
                    > Blockquotes can also be nested...
                    >> ...by using additional greater-than signs right next to each other...
                    > > > ...or with spaces between arrows.
                    
                    
                    ## Lists
                    
                    Unordered
                    
                    + Create a list by starting a line with `+`, `-`, or `*`
                    + Sub-lists are made by indenting 2 spaces:
                      - Marker character change forces new list start:
                        * Ac tristique libero volutpat at
                        + Facilisis in pretium nisl aliquet
                        - Nulla volutpat aliquam velit
                    + Very easy!
                    
                    Ordered
                    
                    1. Lorem ipsum dolor sit amet
                    2. Consectetur adipiscing elit
                    3. Integer molestie lorem at massa
                    
                    
                    1. You can use sequential numbers...
                    1. ...or keep all the numbers as `1.`
                    
                    Start numbering with offset:
                    
                    57. foo
                    1. bar
                    
                    
                    ## Code
                    
                    Inline `code`
                    
                    Indented code
                    
                        // Some comments
                        line 1 of code
                        line 2 of code
                        line 3 of code
                    
                    
                    Block code "fences"
                    
                    ```
                    Sample text here...
                    ```
                    
                    Syntax highlighting
                    
                    ``` js
                    var foo = function (bar) {
                      return bar++;
                    };
                    
                    console.log(foo(5));
                    ```
                    
                    ## Tables
                    
                    | Option | Description |
                    | ------ | ----------- |
                    | data   | path to data files to supply the data that will be passed into templates. |
                    | engine | engine to be used for processing templates. Handlebars is the default. |
                    | ext    | extension to be used for dest files. |
                    
                    Right aligned columns
                    
                    | Option | Description |
                    | ------:| -----------:|
                    | data   | path to data files to supply the data that will be passed into templates. |
                    | engine | engine to be used for processing templates. Handlebars is the default. |
                    | ext    | extension to be used for dest files. |
                    
                    
                    ## Links
                    
                    [link text](http://dev.nodeca.com)
                    
                    [link with title](http://nodeca.github.io/pica/demo/ "title text!")
                    
                    Autoconverted link https://github.com/nodeca/pica (enable linkify to see)
                    
                    
                    ## Images
                    
                    ![Minion](https://octodex.github.com/images/minion.png)
                    ![Stormtroopocat](https://octodex.github.com/images/stormtroopocat.jpg "The Stormtroopocat")
                    
                    Like links, Images also have a footnote style syntax
                    
                    ![Alt text][id]
                    
                    With a reference later in the document defining the URL location:
                    
                    [id]: https://octodex.github.com/images/dojocat.jpg  "The Dojocat"
                    
                    
                    ## Plugins
                    
                    The killer feature of `markdown-it` is very effective support of
                    [syntax plugins](https://www.npmjs.org/browse/keyword/markdown-it-plugin).
                    
                    
                    ### [Emojies](https://github.com/markdown-it/markdown-it-emoji)
                    
                    > Classic markup: :wink: :cry: :laughing: :yum:
                    >
                    > Shortcuts (emoticons): :-) :-( 8-) ;)
                    
                    see [how to change output](https://github.com/markdown-it/markdown-it-emoji#change-output) with twemoji.
                    
                    
                    ### [Subscript](https://github.com/markdown-it/markdown-it-sub) / [Superscript](https://github.com/markdown-it/markdown-it-sup)
                    
                    - 19^th^
                    - H~2~O
                    
                    
                    ### [\<ins>](https://github.com/markdown-it/markdown-it-ins)
                    
                    ++Inserted text++
                    
                    
                    ### [\<mark>](https://github.com/markdown-it/markdown-it-mark)
                    
                    ==Marked text==
                    
                    
                    ### [Footnotes](https://github.com/markdown-it/markdown-it-footnote)
                    
                    Footnote 1 link[^first].
                    
                    Footnote 2 link[^second].
                    
                    Inline footnote^[Text of inline footnote] definition.
                    
                    Duplicated footnote reference[^second].
                    
                    [^first]: Footnote **can have markup**
                    
                        and multiple paragraphs.
                    
                    [^second]: Footnote text.
                    
                    
                    ### [Definition lists](https://github.com/markdown-it/markdown-it-deflist)
                    
                    Term 1
                    
                    :   Definition 1
                    with lazy continuation.
                    
                    Term 2 with *inline markup*
                    
                    :   Definition 2
                    
                            { some code, part of Definition 2 }
                    
                        Third paragraph of definition 2.
                    
                    _Compact style:_
                    
                    Term 1
                      ~ Definition 1
                    
                    Term 2
                      ~ Definition 2a
                      ~ Definition 2b
                    
                    
                    ### [Abbreviations](https://github.com/markdown-it/markdown-it-abbr)
                    
                    This is HTML abbreviation example.
                    
                    It converts "HTML", but keep intact partial entries like "xxxHTMLyyy" and so on.
                    
                    *[HTML]: Hyper Text Markup Language
                    
                    ### [Custom containers](https://github.com/markdown-it/markdown-it-container)
                    
                    ::: warning
                    *here be dragons*
                    :::

                    """.trimIndent(),
                modifier = Modifier.verticalScroll(state = rememberScrollState()),
                //style = LocalTextStyle.current.copy(color = MaterialTheme.colors.onSurface)
            )
        }
    }
}
