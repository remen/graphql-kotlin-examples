package io.github.remen.graphql.fairhair

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DocumentService {
    @GET("documents/{id}")
    fun documentById(
        @Path("id") id: String
    ): Deferred<JsonNode>

    @GET("documents/{id}")
    fun documentById(
        @Path("id") id: String,
        @Query("fields") fields: Collection<String>
    ): Deferred<JsonNode>
}
