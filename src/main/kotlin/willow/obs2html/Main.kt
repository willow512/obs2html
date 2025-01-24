package willow.obs2html

import picocli.CommandLine
import picocli.CommandLine.Command
import java.nio.file.Paths

@Command(name = "example", mixinStandardHelpOptions = true, version = ["Picocli example 4.0"])
class Example : Runnable {
    @CommandLine.Option(
        names = ["-v", "--verbose"],
        description = ["Verbose mode. Helpful for troubleshooting. Multiple -v options increase the verbosity."]
    )
    private var verbose = false

    @CommandLine.Option(
        names = ["--title"],
        description = ["Site title"]
    )
    private var title: String = "Obs 2 Html"

    @CommandLine.Option(
        names = ["-t"],
        description = ["Site template"]
    )
    private var template: String = "template.html"

    @CommandLine.Option(
        names = ["-i"],
        description = ["Source folder"]
    )
    private var source: String = "siteSrc"

    @CommandLine.Option(
        names = ["-o"],
        description = ["Target folder"]
    )
    private var target: String = "siteOut"

    override fun run() {
        SiteGenerator(
            Paths.get(source),
            Paths.get(target),
            Paths.get(template),
            title,
            verbose
        ).process()
    }
}

fun main(args: Array<String>) {
    CommandLine(Example()).execute(*args)
}

