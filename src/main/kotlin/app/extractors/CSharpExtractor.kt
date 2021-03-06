// Copyright 2017 Sourcerer Inc. All Rights Reserved.
// Author: Liubov Yaronskaya (lyaronskaya@sourcerer.io)
// Author: Anatoly Kislov (anatoly@sourcerer.io)

package app.extractors

import app.model.CommitStats
import app.model.DiffFile

class CSharpExtractor : ExtractorInterface {
    companion object {
        const val LANGUAGE_NAME = Lang.CSharp
        val LIBRARIES = ExtractorInterface.getLibraries("cs")
        val evaluator by lazy {
            ExtractorInterface.getLibraryClassifier(LANGUAGE_NAME)
        }
        val importRegex = Regex("""^.*using\s+(\w+[.\w+]*)""")
        val commentRegex = Regex("""^([^\n]*//)[^\n]*""")
        val extractImportRegex = Regex("""using\s+(\w+[.\w+]*)""")
    }

    override fun extract(files: List<DiffFile>): List<CommitStats> {
        files.map { file -> file.language = LANGUAGE_NAME }
        return super.extract(files)
    }

    override fun extractImports(fileContent: List<String>): List<String> {
        val imports = mutableSetOf<String>()

        fileContent.forEach {
            val res = extractImportRegex.find(it)
            if (res != null) {
                val importedName = res.groupValues[1]
                LIBRARIES.forEach { library ->
                    if (importedName.startsWith(library)) {
                        imports.add(library)
                    }
                }
            }
        }

        return imports.toList()
    }

    override fun tokenize(line: String): List<String> {
        val importRegex = Regex("""^.*using\s+(\w+[.\w+]*)""")
        val commentRegex = Regex("""^([^\n]*//)[^\n]*""")
        var newLine = importRegex.replace(line, "")
        newLine = commentRegex.replace(newLine, "")
        return super.tokenize(newLine)
    }

    override fun getLineLibraries(line: String,
                                  fileLibraries: List<String>): List<String> {

        return super.getLineLibraries(line, fileLibraries, evaluator,
            LANGUAGE_NAME)
    }
}
