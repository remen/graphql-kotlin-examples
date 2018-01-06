package io.github.remen.graphql.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetchingEnvironment
import io.github.remen.graphql.fairhair.DocumentService
import io.github.remen.graphql.fairhair.SearchService
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.util.*
import java.util.concurrent.CompletableFuture

class Query(private val documentService: DocumentService, private val searchService: SearchService) : GraphQLQueryResolver {
    private val fieldTranslationTable = HashMap<String, String>()

    init {
        fieldTranslationTable.put("id", "id")
        fieldTranslationTable.put("url", "metaData.url")
    }

    fun document(id: String, environment: DataFetchingEnvironment): CompletableFuture<Document> {
        val fields = environment.selectionSet.get().keys.map { fieldTranslationTable[it]!! }

        return future {
            Document(documentService.documentById(id, fields).await())
        }
    }

    fun search(searchQuery: SearchQuery): CompletableFuture<List<Document>> {
        val query = "{\n" +
            "    \"query\": {\n" +
            "        \"type\": \"all\",\n" +
            "        \"allQueries\": [\n" +
            "            {\n" +
            "                \"field\": \"body.title.text\",\n" +
            "                \"value\": \"obama\",\n" +
            "                \"type\": \"word\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"field\": \"metaData.source.informationType\",\n" +
            "                \"value\": \"news\",\n" +
            "                \"type\": \"term\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"field\": \"body.publishDate.date\",\n" +
            "                \"from\": 1457967799000,\n" +
            "                \"to\": 1458140599000,\n" +
            "                \"type\": \"range\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"viewRequests\": {\n" +
            "        \"count\": {\n" +
            "            \"type\": \"count\"\n" +
            "        },\n" +
            "        \"list\": {\n" +
            "            \"type\": \"resultList\",\n" +
            "            \"fields\": [\n" +
            "                \"body.title.text\",\n" +
            "                \"metaData.url\",\n" +
            "                \"id\"\n" +
            "            ],\n" +
            "            \"size\": 10,\n" +
            "            \"start\": 0,\n" +
            "            \"highlightOptions\": {\n" +
            "                \"body.title.text\": {\n" +
            "                    \"numberOfFragments\": 10,\n" +
            "                    \"preTag\": \"<em>\",\n" +
            "                    \"fragmentSize\": 140,\n" +
            "                    \"strictFragmentSize\": true,\n" +
            "                    \"postTag\": \"</em>\",\n" +
            "                    \"keywords\": true\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"

        val queryAsJson = ObjectMapper().readTree(query)

        return future {
            val searchResult = searchService.search(queryAsJson).await()
            searchResult["views"]["list"]["results"].map { result ->
                Document(result["quiddity"])
            }
        }
    }
}
