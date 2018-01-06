package io.github.remen.graphql

import com.coxautodev.graphql.tools.SchemaParser
import com.fasterxml.jackson.databind.SerializationFeature
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import graphql.ExecutionInput
import graphql.GraphQL
import io.github.remen.graphql.fairhair.DocumentService
import io.github.remen.graphql.fairhair.SearchService
import io.github.remen.graphql.graphql.Query
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.experimental.future.await
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.adapter.java8.Java8CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

val LOGGER: Logger = LoggerFactory.getLogger("AppKt")

fun main(args: Array<String>) {
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            LOGGER.info("{} {}", chain.request().method(), chain.request().url())
            chain.proceed(chain.request())
        }
        .build()

    val retrofit = Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(client)
        .build()

    val documentService = retrofit.newBuilder()
        .baseUrl("https://document-service-nrstaging.meltwater.net/")
        .build()
        .create(DocumentService::class.java)

    val searchService = retrofit.newBuilder()
        .baseUrl("https://search-service-nrstaging.meltwater.net/")
        .client(client)
        .build()
        .create(SearchService::class.java)

    val graphQLSchema = SchemaParser.newParser()
        .file("graphql/schema.graphqls")
        .resolvers(Query(documentService, searchService))
        .build()
        .makeExecutableSchema()

    val graphQL = GraphQL
        .newGraphQL(graphQLSchema)
        .build()

    val server = embeddedServer(Netty, 8080) {
        install(Compression)
        install(ContentNegotiation) {
            jackson {
                configure(SerializationFeature.INDENT_OUTPUT, true)
            }
        }
        routing {
            get("/") {
                call.respondRedirect("/graphiql/index.html", permanent = true)
            }
            static("/graphiql") {
                resources("graphiql")
            }
            post("/graphql") {
                val request = call.receive<GraphQLRequest>()
                val response = graphQL.executeAsync(
                    ExecutionInput.newExecutionInput()
                        .query(request.query)
                        .operationName(request.operationName)
                        .variables(request.variables)
                ).await().toSpecification()

                call.respond(response)
            }
        }
    }
    server.start(wait = true)
}

data class GraphQLRequest(val query: String, val variables: Map<String, Any>?, val operationName: String?)
