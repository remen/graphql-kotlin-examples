package io.github.remen.graphql.graphql

import com.fasterxml.jackson.databind.JsonNode

class Document(private val jsonNode: JsonNode) {
    val id get() = jsonNode.path("id").asText()
    val url get() = jsonNode.path("metaData").path("url").asText()
}
