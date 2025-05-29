package search

import java.io.File
import java.io.FileInputStream

fun main(args: Array<String>) {
    val searchEngine = SearchEngine()
    if (args.isNotEmpty() && args.contains("--data")) {
        val inputFileIndex = args.indexOf("--data") + 1
        val inputFile = args[inputFileIndex]
        searchEngine.readInputFromFile(inputFile)
    }else{
        searchEngine.readInputFromConsole()
    }
    searchEngine.displayMenu()
}

class SearchEngine{
    private var inputList: MutableList<String> = mutableListOf()
    private val invertedIndexMap = mutableMapOf<String, MutableList<Int>>()

    fun displayMenu() {
        while (true) {
            val welcomeString = """
        === Menu ===
        1. Find a person
        2. Print all people
        0. Exit
    """.trimIndent()
            println(welcomeString)
            val choice = readln().toInt()
            when (choice) {
                1 -> findPersonUsingSearchStrategy()
                2 -> printAllPersons(inputList)
                0 -> break
                else -> println("Incorrect option! Try again.")
            }
        }
    }

    fun readInputFromConsole() {
        println("Enter the number of people:")
        val count = readln().toInt()
        repeat(count) {
            inputList.run { add(readln()) }
        }
        createInvertedIndexMap()
    }

    fun readInputFromFile(fileName: String) {
        val file = File(fileName)
        inputList = FileInputStream(file).use { fis ->
            fis.bufferedReader()
                .useLines { lines -> lines.toMutableList() }
        }
        createInvertedIndexMap()
    }

    private fun createInvertedIndexMap() {
        // (STEP:1) Creating a map from each user input line to its index (index -> line)
        val map = mutableMapOf<Int, String>()
        for (index in inputList.indices) {
            map[index] = inputList[index]
        }

        // Splitting each input line by whitespace delimiter to separate words(tokens)
        val tokenList = inputList.map { it.split(" ") }.flatten().map { it.lowercase() }

        // Inverting the map created at (STEP:1) with line as key (line -> index)
        val map2 = map.entries.groupBy { entry -> entry.value }
            .mapValues { entry2 -> entry2.value.map { entry3 -> entry3.key } }

        // Creating a inverted index map from each token(word) as key ( word -> List(index))
        for (token in tokenList) {
            val list = inputList.filter { it.lowercase().contains(token) }
                .map { it2 -> map2[it2]!! }.flatten().toMutableList()
            invertedIndexMap.merge(token, list) { oldValue, newValue ->
                oldValue.addAll(newValue)
                return@merge oldValue.distinct().toMutableList()
            }
        }
    }

    private fun findPersonsUsingIndexMap(keyword: String): MutableList<String> {
        val candidatesList = mutableListOf<String>()
        val searchKeys = keyword.split(' ')
        for (key in searchKeys) {
            if (invertedIndexMap.containsKey(key)) {
                for (value in invertedIndexMap[key]!!.iterator()) {
                    candidatesList.add(inputList[value])
                }
            }
        }
        return candidatesList
    }

    private fun findPersonUsingSearchStrategy() {
        println("Select a matching strategy: ALL, ANY, NONE")
        val strategy = readln()
        println("Enter a name or email to search all matching people.")
        val keyword = readln().lowercase()
        val list = findPersonsUsingIndexMap(keyword)
        when (strategy) {
            "ALL" -> showOutput(list.filter { it.contains(keyword) }.distinct())
            "ANY" -> showOutput(list.distinct())
            "NONE" -> {
                if (list.isNotEmpty()) {
                    val inputListCopy = MutableList(inputList.size) { inputList[it] }
                    inputListCopy.removeAll(list)
                    showOutput(inputListCopy)
                }
                showOutput(inputList.distinct())
            }

            else -> println("Not a valid strategy to search people.")
        }
    }

    private fun findPerson() {
        println("Enter a name or email to search all suitable people.")
        val keyword = readln().lowercase()
        val data = inputList.filter { it.lowercase().contains(keyword) }
        showOutput(data)
    }

    private fun printAllPersons(list: List<String>) = list.forEach(::println)

    private fun showOutput(list: List<String>) {
        if (list.isNotEmpty()) {
            println("${list.size} persons found:")
            list.forEach { println(it) }
        } else {
            println("No matching people found.")
        }
    }
}