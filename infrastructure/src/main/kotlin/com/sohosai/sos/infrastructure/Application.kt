package com.sohosai.sos.infrastructure

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.sohosai.sos.domain.user.Email
import com.sohosai.sos.interfaces.AuthContext
import io.ktor.application.Application
import io.ktor.application.ApplicationEnvironment
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.auth.principal
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.gson.gson
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import org.flywaydb.core.Flyway
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import kotlin.properties.Delegates

var application: Application by Delegates.notNull()
var env: ApplicationEnvironment by Delegates.notNull()

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused") // called in application.conf
fun Application.configure() {
    application = this
    env = this.environment

    install(Koin) {
        modules(KoinModules.dev())
    }
    install(Authentication) {
        jwt {
            val jwkProvider: JwkProvider = JwkProviderBuilder(env.config.property("jwt.jwkDomain").getString())
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()
            realm = env.config.property("jwt.realm").getString()
            challenge { _, _ ->
                this.context.authentication.errors["JWTAuth"]?.let {
                    this.context.authentication.principal = AuthStatus.Unauthorized(it)
                }
            }
            // TODO: verify audience (client id)
            verifier(jwkProvider, env.config.property("jwt.issuer").getString())
            validate { credential ->
                this.principal() ?: credential.payload.getClaim("email")?.asString()?.let {
                    AuthStatus.Authorized(
                        AuthContext(
                            authId = credential.payload.subject,
                            email = Email(it),
                            isEmailVerified = credential.payload.getClaim("email_verified").asBoolean()
                        )
                    )
                }
            }
        }
    }
    install(ContentNegotiation) {
        gson()
    }
    install(DataConversion)
    migrateDatabase(get())

    routing {
        routes()
    }
}

private fun migrateDatabase(dataSource: DataSource) {
    Flyway.configure().apply {
        dataSource(dataSource)
        locations("db/migration")
    }.load().migrate()
}