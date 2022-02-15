package svcs

fun main(args: Array<String>) {
    val vcs = Vcs()

    val command = if (args.size > 0) args[0] else ""
    when {
        command.isEmpty() -> vcs.help()
        command == "--help" -> vcs.help()
        command == "config" -> vcs.config(args)
        command == "add" -> vcs.add(args)
        command == "log" -> vcs.log()
        command == "commit" -> vcs.commit(args)
        command == "checkout" -> vcs.checkout(args)
        vcs.commandExists(command) -> vcs.commandHelp(command)
        else -> println("'$command' is not a SVCS command.")
    }
}


