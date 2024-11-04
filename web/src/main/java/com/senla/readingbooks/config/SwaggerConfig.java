package com.senla.readingbooks.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Nikita Lyashkevich",
                        email = "liroy2468@gmail.com"
                ),
                description = "Open Api documentation for the Reading Books Service",
                title = "RESTful API for a web application for reading books (SENLA Course)",
                version = "1.0"),

        security = {
                @SecurityRequirement(name = "JWT")
//                ,
//                @SecurityRequirement(name = "oauth2")
        }
)
@SecurityScheme(
        name = "JWT",
        description = """
                JWT auth with access and refresh tokens with the ability to
                revoke the token and logout of the application
                """,
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
//@SecurityScheme(
//        name = "oauth2",
//        type = SecuritySchemeType.OAUTH2,
//        flows = @OAuthFlows(
//                authorizationCode = @OAuthFlow(
//                        authorizationUrl = "http://localhost:8080/oauth2/authorization/google",
//                        tokenUrl = "https://www.googleapis.com/oauth2/v4/token"
//                )
//        )
//)
public class SwaggerConfig {
}
