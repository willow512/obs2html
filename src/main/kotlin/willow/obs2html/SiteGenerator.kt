package willow.obs2html

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension
import java.nio.file.*
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

class SiteGenerator(
    private val source: Path = Paths.get("siteSrc"),
    private val target: Path = Paths.get("site"),
    template: Path = Paths.get("template.html"),
    private val title: String = "Obs 2 Html",
    private val verbose: Boolean = false,
) {
    private enum class ItemType { PAGE, MEDIA }

    private val options = MutableDataSet().apply {
        val extensions = mutableListOf<Extension>()
        extensions.add(TablesExtension.create())
        extensions.add(StrikethroughExtension.create())
        set(Parser.EXTENSIONS, extensions)
    }
    private val parser: Parser = Parser.builder(options).build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder(options).build()
    private val template = Files.readString(template)

    private val items = parseTree(source)

    init {
        if (!Files.exists(source)) {
            error("$source does not exist")
        }
    }

    private fun sourcePath(path: Path): Path = source.resolve(path)
    private fun targetPath(path: Path): Path = target.resolve(targetBasePath(path))
    private fun targetBasePath(path: Path): Path = sanitize(convertExtension(path))
    private fun title(path: Path): String = path.nameWithoutExtension
    private fun sanitize(path: Path): Path =
        path.map { Path.of(it.name.lowercase().replace("\\s".toRegex(), "_")) }.reduce(Path::resolve)

    private fun convertExtension(path: Path): Path =
        path.resolveSibling(path.nameWithoutExtension + "." + getExtension(path))

    private fun getExtension(path: Path): String =
        when (type(path)) {
            ItemType.PAGE -> "html"
            ItemType.MEDIA -> path.extension
        }

    private fun type(path: Path): ItemType =
        when (path.extension) {
            "md" -> ItemType.PAGE
            else -> ItemType.MEDIA
        }

    private fun htmlContent(path: Path, src: Path = path): String {
        var content = Files
            .readString(sourcePath(src))

        // Replace links
        for (item in items) {
            val t = title(item)
            val parent = targetBasePath(path).parent
            val rel = if (parent == null) targetBasePath(item) else parent.relativize(targetBasePath(item))
            content = content.replace("[[$t]]", "<a href='$rel'>${t}</a>")
        }
        // Replace media
        for (item in items) {
            val parent = targetBasePath(path).parent
            val rel = if (parent == null) targetBasePath(item) else parent.relativize(targetBasePath(item))
            content = content.replace("![[${item.fileName}]]", "<img src='$rel'>")
        }

        return renderer.render(parser.parse(content))
    }

    private fun parseTree(root: Path): List<Path> {
        val content = mutableListOf<Path>()

        val queue = ArrayDeque<Path>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val c = queue.removeFirst()
            if (Files.isDirectory(c)) {
                queue.addAll(Files.list(c).toList())
            } else {
                content.add(root.relativize(c))
            }
        }

        return content.toList()
    }

    fun process() {
        println("Generating site...")

        for (item in items) {
            if (verbose) {
                println(title(item) + ": " + targetPath(item))
            }
            val outPath = targetPath(item)
            outPath.parent.createDirectories()

            when (type(item)) {
                ItemType.PAGE -> {
                    val content = template
                        .replace("{title}", title)
                        .replace("{content}", htmlContent(item))
                        .replace("{menu}", htmlContent(item, Paths.get("menu.md")))

                    Files.write(outPath, content.toByteArray())
                }

                else -> {
                    Files.copy(sourcePath(item), outPath, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }
}