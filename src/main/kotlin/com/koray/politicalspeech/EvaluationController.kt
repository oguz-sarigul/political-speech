package com.koray.politicalspeech

import org.slf4j.LoggerFactory
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EvaluationController(
    private val csvService: CsvService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(EvaluationController::class.java)
    }

    @GetMapping("/evaluation")
    fun evaluate(@RequestParam urls: MultiValueMap<String, String>): Response {
        val speeches = mutableListOf<Speech>()
        for ((paramName, paramValue) in urls) {
            val url = paramValue[0]
            LOG.info("will parse the csv from the given url: {}", url)
            csvService.readCSVFromUrl(url)?.let {csv ->
                csvService.parseData(csv)?.let {
                    speeches.addAll(it)
                }
            }
        }

        return prepareResponse(speeches)
    }

    private fun prepareResponse(speeches: List<Speech>): Response {
        return if (speeches.isNotEmpty()) {

            val mostSpeechesIn2013 = speeches
                .filter { it.date.year == 2013  }
                .groupBy { it.speaker }
                .mapValues { (_, value) -> value.sumOf { it.words } }
                .maxByOrNull { (_, value) -> value }

            val mostSecurity = speeches
                .filter { it.topic == "homeland security" }
                .groupBy { it.speaker }
                .mapValues { (_, value) -> value.sumOf { it.words } }
                .maxByOrNull { (_, value) -> value }

            val leastWordy = speeches
                .groupBy { it.speaker }
                .mapValues { (_, value) -> value.sumOf { it.words } }
                .minByOrNull { (_, value) -> value }

            Response(
                mostSpeechesIn2013?.key,
                mostSecurity?.key,
                leastWordy?.key
            )
        }
         else Response()
    }
}
