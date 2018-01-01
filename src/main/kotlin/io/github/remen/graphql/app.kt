package io.github.remen.graphql

import com.fasterxml.jackson.databind.SerializationFeature
import graphql.GraphQL
import io.github.remen.graphqlkotlin.createGraphQLSchema
import io.github.remen.graphqlkotlin.executeSuspend
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


data class Person(
    val name: String,
    val age: Int,
    private val bestFriend: Person?
)

fun main(args: Array<String>) {
    val schema = createGraphQLSchema(Person::class)
    val graphQL = GraphQL.newGraphQL(schema).build()

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
                val person = Person("John Doe", 32, Person("Jane Doe", 33, null))
                val response = graphQL.executeSuspend(
                    query = request.query,
                    operationName = request.operationName,
                    context = null,
                    root = person,
                    variables = request.variables
                ).toSpecification()
                call.respond(response)
            }
        }
    }
    server.start(wait = true)
}

data class GraphQLRequest(val query: String, val variables: Map<String, Any>?, val operationName: String?)
