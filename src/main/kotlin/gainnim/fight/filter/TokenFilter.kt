package gainnim.fight.filter

import gainnim.fight.util.jwt.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
class TokenFilter(val tokenProvider: JwtProvider, @Value("\${jwt.haeder}") val header: String): GenericFilterBean() {
    val log = LoggerFactory.getLogger(javaClass)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest

        log.info(request.requestURI)

        request.getHeader(header)?.let {
            log.info(it)
            tokenProvider.validateToken(it)
            val auth = tokenProvider.getAuthenticationByToken(it)
            SecurityContextHolder.getContext().authentication = auth
        }

        chain.doFilter(request, response)
    }
}