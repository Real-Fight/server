package gainnim.fight.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@io.swagger.v3.oas.annotations.security.SecurityScheme(
        type = SecuritySchemeType.APIKEY, `in` = SecuritySchemeIn.HEADER,
        name = "Authorization", description = "."
)
@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
                .info(
                        Info().title("API Documentation")
                                .description("Swagger UI with JWT Authorization")
                                .version("1.0")
                )
                .addSecurityItem(SecurityRequirement().addList("Authorization"))
    }
}
