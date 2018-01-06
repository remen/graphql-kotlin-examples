package io.github.remen.graphql.fairhair

import com.fasterxml.jackson.databind.JsonNode
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.CompletableFuture

interface SearchService {
    @POST("/")
    fun search(@Body body: JsonNode): CompletableFuture<JsonNode>
}
