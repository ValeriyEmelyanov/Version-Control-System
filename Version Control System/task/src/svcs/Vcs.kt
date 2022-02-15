package svcs

import java.io.File
import java.security.MessageDigest

class Vcs {
    val dirVcsName = "vcs"
    val configName = dirVcsName + "/config.txt"
    val indexName = dirVcsName + "/index.txt"
    val logName = dirVcsName + "/log.txt"
    val dirCommitsName = dirVcsName + "/commits"

    init {
        val dirVcs = File(dirVcsName)
        if (!dirVcs.exists()) dirVcs.mkdir()
    }

    val commandDescriptions = createCommandDescriptions()

    fun help() {
        println("These are SVCS commands:")
        for ((k, v) in commandDescriptions) {
            println(String.format("%-10s %s", k, v))
        }
    }

    fun commandHelp(command: String) {
        println(commandDescriptions[command])
    }

    fun commandExists(command: String): Boolean {
        return commandDescriptions.containsKey(command)
    }

    fun config(args: Array<String>) {
        val configFile = File(configName)

        if (args.size == 1) {
            if (!configFile.exists()) {
                println("Please, tell me who you are.")
            } else {
                val username = configFile.readText()
                println("The username is $username.")
            }
            return
        }

        if (args.size > 1) {
            configFile.writeText(args[1])
            println("The username is ${args[1]}.")
        }
    }

    fun add(args: Array<String>) {
        when (args.size) {
            1 -> add()
            2 -> add(args[1])
            else -> println("Неправильное число аргументов команды 'add'.")
        }
    }

    private fun add() {
        val indexFile = File(indexName)
        if (!indexFile.exists()) {
            commandHelp("add")
            return
        }

        println("Tracked files:")
        indexFile.readLines().forEach { println(it) }
    }

    private fun add(fileName: String) {
        val indexFile = File(indexName)

        val file = File(fileName)
        if (!file.exists()) {
            println("Can't find '$fileName'.")
            return
        }

        if (indexFile.exists()) {
            val found = indexFile.readLines().filter { it == fileName }
            if (found.size == 0) {
                indexFile.appendText("\n" + fileName)
            }
        } else {
            indexFile.writeText(fileName)
        }
        println("The file '$fileName' is tracked.")
    }

    fun log() {
        val logFile = File(logName)
        if (!logFile.exists()) {
            println("No commits yet.")
            return
        }

        val lines = logFile.readLines()
        if (lines.size == 0) {
            println("No commits yet.")
            return
        }

        for (i in lines.size - 1 downTo 0 step 4) {
            if (i - 2 < 0) break
            println(lines[i - 2])
            println(lines[i - 1])
            println(lines[i])
            println()
        }
    }

    fun commit(args: Array<String>) {
        if (args.size < 2) {
            println("Message was not passed.")
            return
        }

        val indexFile = File(indexName)
        if (!indexFile.exists()) {
            println("Nothing to commit.")
            return
        }

        val fileNames = indexFile.readLines()
        val hash = getHash(fileNames)
        val dirHash = File(dirCommitsName + "/" + hash)
        if (dirHash.exists()) {
            println("Nothing to commit.")
            return
        }

        commitFiles(dirHash, fileNames)
        writeToLog(hash, args[1])
        println("Changes are committed.")
    }

    private fun writeToLog(hash: String, message: String) {
        val logFile = File(logName)
        if (!logFile.exists()) {
            logFile.writeText(getLogText(hash, message))
        } else {
            logFile.appendText("\n" + getLogText(hash, message))
        }
    }

    private fun getLogText(hash: String, message: String): String {
        val username = File(configName).readText()
        return """commit $hash
            |Author: $username
            |$message
            |""".trimMargin()
    }


    private fun commitFiles(dirHash: File, fileNames: List<String>) {
        dirHash.mkdir()
        for (fileName in fileNames) {
            val source = File(fileName)
            if (!source.exists()) {
                println("Файл '$fileName' не существует.")
                continue
            }
            val target = dirHash.resolve(fileName)
            source.copyTo(target)
        }
    }

    private fun getHash(fileNames: List<String>): String {
        val md = MessageDigest.getInstance("SHA-256")

        for (fileName in fileNames) {
            md.update(File(fileName).readBytes())
        }

        val digest = md.digest()
        val hexSb = StringBuffer()
        for (i in 0..digest.lastIndex - 1) {
            hexSb.append(Integer.toHexString(0xFF and digest[i].toInt()))
        }
        return hexSb.toString()
    }

    fun checkout(args: Array<String>) {
        if (args.size < 2) {
            println("Commit id was not passed.")
            return
        }

        val id = args[1]
        val dirId = File(dirCommitsName).resolve(id)
        if (!dirId.exists()) {
            println("Commit does not exist.")
            return
        }

        checkoutToId(dirId)
        println("Switched to commit $id.")
    }

    private fun checkoutToId(dirId: File) {
        val fileNames = dirId.list()
        for (fileName in fileNames) {
            val source = dirId.resolve(fileName)
            val target = File(fileName)
            source.copyTo(target, true)
        }
    }

    private fun createCommandDescriptions(): Map<String, String> =
        linkedMapOf(
            "config" to "Get and set a username.",
            "add" to "Add a file to the index.",
            "log" to "Show commit logs.",
            "commit" to "Save changes.",
            "checkout" to "Restore a file.",
        )
}
