package com.koray.politicalspeech

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CsvService {
    companion object {
        private val LOG = LoggerFactory.getLogger(CsvService::class.java)
        private val dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    fun readCSVFromUrl(url: String): List<List<String>>? {
        return try {
            val lines = mutableListOf<List<String>>()
            URL(url).openStream().bufferedReader().use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    val fields = line.split(";").map { it.trim() }
                    lines.add(fields)
                }
            }
            lines
        } catch (ex: Exception) {
            LOG.error("Could not read file from url $url", ex)
            null
        }
    }

    fun parseData(csv: List<List<String>>): List<Speech>? {
        return try {
            csv.drop(1)
                .map { line ->
                Speech(
                    line[0],
                    line[1],
                    LocalDate.parse(line[2], dtFormatter),
                    Integer.parseInt(line[3])
                )
            }
        } catch (ex: Exception) {
            LOG.error("Could not parse the csv file", ex)
            null
        }
    }
}
