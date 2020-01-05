package com.sohosai.sos.infrastructure

import com.sohosai.sos.infrastructure.graphql.GraphQLHandler
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import org.koin.ktor.ext.get

internal fun Routing.routes() {
    val graphQLHandler: GraphQLHandler = get()
    authenticate(optional = true) {
        get("/graphql") { graphQLHandler.handleCall(call) }
        post("/graphql") { graphQLHandler.handleCall(call) }
        get("/") { call.respond(HttpStatusCode.OK) }
        get("/health-check") { call.respond(HttpStatusCode.OK) }
    }
}