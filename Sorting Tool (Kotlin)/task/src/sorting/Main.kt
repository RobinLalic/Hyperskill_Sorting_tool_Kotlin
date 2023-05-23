package sorting

import java.io.File
import java.util.*
import kotlin.system.exitProcess

data class ProcessedData(
    val count: Int,
    val maxElement: String,
    val maxOccurrences: Int,
    val maxPercentage: Int
)

enum class DataType {
    LONG,
    LINE,
    WORD
}

enum class SortingType {
    NATURAL,
    BYCOUNT
}


fun main(args: Array<String>) {
    val dataType = parseDataTypeArgument(args)
    val sortingType = parseSortingTypeArgument(args)
    val inputFile = parseInputFileArgument(args)
    val outputFile = parseOutputFileArgument(args)

    val inputData = if (inputFile != null) {
        readInputDataFromFile(inputFile, dataType)
    } else {
        readInputData(dataType)
    }


    if (outputFile != null) {
        writeDataToFile(outputFile, inputData, dataType, sortingType)
    } else {
        printData(inputData, dataType, sortingType)
    }
}

fun readInputData(dataType: DataType): List<String> {
    val scanner = Scanner(System.`in`)
    val inputData = mutableListOf<String>()

    when (dataType) {
        DataType.LONG -> {
            while (scanner.hasNextLong()) {
                try {
                    val number = scanner.nextLong().toString()
                    if (number.toLongOrNull() != null) {
                        inputData.add(number)
                    } else {
                        throw IllegalArgumentException("$number is not a long. It will be skipped")
                    }
                } catch (e: IllegalArgumentException) {
                    println(e.message)
                }
            }
        }

        DataType.LINE -> {
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                inputData.add(line)
            }
        }

        DataType.WORD -> {
            while (scanner.hasNext()) {
                val inputLine = scanner.nextLine().trim()
                inputData.addAll(inputLine.split(Regex("\\s+")))
            }
        }
    }
    return inputData
}

fun readInputDataFromFile(fileName: String, dataType: DataType): List<String> {
    val file = File(fileName)
    if (!file.exists()) {
        println("File $fileName does not exist")
        exitProcess(1)
    }
    val scanner = Scanner(file)

    val inputData = when (dataType) {
        DataType.LONG -> {
            val numbers = mutableListOf<String>()
            while (scanner.hasNextLong()) {
                try {
                    val number = scanner.nextLong().toString()
                    if (number.toLongOrNull() != null) {
                        numbers.add(number)
                    } else {
                        throw IllegalArgumentException("$number is not a long. It will be skipped")
                    }
                } catch (e: IllegalArgumentException) {
                    println(e.message)
                }
            }
            numbers
        }
        DataType.LINE -> scanner.useDelimiter("\\n").asSequence().toList()
        DataType.WORD -> scanner.useDelimiter("\\s+").asSequence().toList()
    }
    scanner.close()
    return inputData

}

/*

}*/

fun writeDataToFile(fileName: String, inputData: List<String>, dataType: DataType, sortingType: SortingType) {
    val file = File(fileName)
    val writer = file.bufferedWriter()
    val processedData = processData(inputData, dataType)
    val count = processedData.count
    writer.write("Total ${dataType.name.lowercase(Locale.getDefault())}s: $count.\n")

    when (sortingType) {

        SortingType.NATURAL -> {
            val sortedData = when (dataType) {
                DataType.LONG -> inputData.map { it.toLong() }.sorted().map { it.toString() }
                DataType.LINE, DataType.WORD -> inputData.sorted()
            }
            writer.write("Sorted data: ${sortedData.joinToString(" ")}\n")
        }

        SortingType.BYCOUNT -> {
            val dataMap = inputData.groupingBy { it }.eachCount().toList()

            if (dataType == DataType.LONG) {
                writer.write(sortAndPrintLong(dataMap, count).toString())
            } else if (dataType == DataType.WORD || dataType == DataType.LINE) {
                writer.write(sortAndPrintWordAndLine(dataMap, count).toString())
            }
        }

    }
    writer.close()

}

fun printData(inputData: List<String>, dataType: DataType, sortingType: SortingType) {
    val processedData = processData(inputData, dataType)
    val count = processedData.count
    println("Total ${dataType.name.lowercase(Locale.getDefault())}s: $count.")

    when (sortingType) {

        SortingType.NATURAL -> {
            sortAndPrintNaturalSortingType(dataType, inputData)
        }

        SortingType.BYCOUNT -> {
            val dataMap = inputData.groupingBy { it }.eachCount().toList()

            if (dataType == DataType.LONG) {
                sortAndPrintLong(dataMap, count)
            } else if (dataType == DataType.WORD || dataType == DataType.LINE) {
                sortAndPrintWordAndLine(dataMap, count)
            }
        }

    }
}

fun sortAndPrintLong(dataMap: List<Pair<String, Int>>, count: Int) {
    val sortedData = dataMap.sortedWith(compareBy<Pair<String, Int>> { it.second }.thenBy { it.first.toLongOrNull() })
    printingGeneral(sortedData, count)
}


fun sortAndPrintWordAndLine(dataMap: List<Pair<String, Int>>, count: Int) {
    val sortedData = dataMap.sortedWith(compareBy<Pair<String, Int>> { it.second }.thenBy { it.first })
    printingGeneral(sortedData, count)
}

fun printingGeneral(sortedData: List<Pair<String, Int>>, count: Int) {
    for ((element, entryCount) in sortedData) {
        val percentage = (entryCount.toDouble() / count) * 100
        println("$element: $entryCount time(s), ${percentage.toInt()}%")
    }
}

fun sortAndPrintNaturalSortingType(dataType: DataType, inputData: List<String>) {
    val sortedData = when (dataType) {
        DataType.LONG -> inputData.map { it.toLong() }.sorted().map { it.toString() }
        DataType.LINE, DataType.WORD -> inputData.sorted()

    }
    println("Sorted data: ${sortedData.joinToString(" ")}")
}


fun parseDataTypeArgument(args: Array<String>): DataType {
    val dataTypeIndex = args.indexOf("-dataType")
    if (dataTypeIndex == -1 || dataTypeIndex == args.lastIndex) {
        println("No data type defined!")
        return DataType.WORD
    }
    try {
        return when (val dataTypeArg = args[dataTypeIndex + 1].uppercase(Locale.getDefault())) {
            "LONG" -> DataType.LONG

            "LINE" -> DataType.LINE

            "WORD" -> DataType.WORD

            else -> {
                throw Exception("$dataTypeArg is not a valid parameter. It will be skipped")
            }
        }
    } catch (e: Exception) {
        println(e.message)
        exitProcess(1)
    }
}

fun parseSortingTypeArgument(args: Array<String>): SortingType {
    val sortingTypeIndex = args.indexOf("-sortingType")
    if (sortingTypeIndex == -1 || sortingTypeIndex == args.lastIndex) {
        return SortingType.NATURAL
    }
    try {
        return when (val sortingTypeArg = args[sortingTypeIndex + 1].uppercase(Locale.getDefault())) {
            "NATURAL" -> SortingType.NATURAL
            "BYCOUNT" -> SortingType.BYCOUNT

            else -> {
                throw Exception("$sortingTypeArg is not a valid parameter. It will be skipped")
            }
        }
    } catch (e: Exception) {
        println(e.message)
        exitProcess(1)
    }
}

fun parseInputFileArgument(args: Array<String>): String? {
    val inputFileIndex = args.indexOf("-inputFile")
    return if (inputFileIndex != -1 && inputFileIndex < args.lastIndex) {
        args[inputFileIndex + 1]
    } else {
        null
    }
}

fun parseOutputFileArgument(args: Array<String>): String? {
    val outputFileIndex = args.indexOf("-outputFile")
    return if (outputFileIndex != -1 && outputFileIndex < args.lastIndex) {
        args[outputFileIndex + 1]
    } else {
        null
    }
}


fun processData(inputData: List<String>, dataType: DataType): ProcessedData {
    val count = inputData.size
    val maxElement = when (dataType) {
        DataType.LONG -> inputData.maxByOrNull { it.toLongOrNull() ?: Long.MIN_VALUE } ?: Long.MIN_VALUE.toString()
        DataType.LINE, DataType.WORD -> inputData.maxByOrNull { it.length } ?: ""
    }
    val maxOccurrences = inputData.count { it == maxElement }
    val maxPercentage = (maxOccurrences.toDouble() / count) * 100


    return ProcessedData(count, maxElement, maxOccurrences, maxPercentage.toInt())

}

