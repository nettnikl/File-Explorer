package com.raival.fileexplorer.root

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.IOException

class RootUtils {
    companion object {
        private val UNIX_SHELL_CONTROL_CHARACTERS = "([\\s()\\[\\]{}'\"`&?\\\\])".toRegex()

        fun exists(directory: File): Boolean {
            return try {
                executeElevated("ls -la " + escapeUnixShellString(directory.toString()))
                true
            } catch (e: InterruptedException) {
                false
            } catch (e: IOException) {
                false
            }
        }

        fun listFiles(directory: File, includeHiddenFiles: Boolean = true): List<File> {
            val reader = executeElevated("ls -la " + escapeUnixShellString(directory.toString()))

            return reader.useLines { it }
                .filter { line -> line.startsWith(".") && !includeHiddenFiles }
                .map { File(it) }
                .toList()
        }

        private fun escapeUnixShellString(input: String): String {
            return input.replace(UNIX_SHELL_CONTROL_CHARACTERS, "\\\\$1")
        }

        private fun executeElevated(cmd: String): BufferedReader {
            val elevatedProcess = Runtime.getRuntime().exec("su")
            val reader = elevatedProcess.inputStream.reader().buffered()
            val errReader = elevatedProcess.errorStream.reader().buffered()

            val outputStream = DataOutputStream(elevatedProcess.outputStream)
            outputStream.writeBytes(cmd + "\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()

            val exitCode = elevatedProcess.waitFor()
            val err = errReader.readText()
            if (exitCode != 0 || err.isNotBlank()) {
                throw IOException("$exitCode: $err")
            }

            return reader
        }
    }
}