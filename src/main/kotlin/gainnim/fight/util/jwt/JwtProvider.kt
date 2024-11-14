package gainnim.fight.util.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import kotlin.io.encoding.Base64

@Component
class JwtProvider(
        @Value("\${jwt.secret}") val secret: String,
        @Value("\${jwt.token-expires-time}") val accessTokenExpiresTime: Long
) : InitializingBean {
    lateinit var key: Key
    val log = LoggerFactory.getLogger(javaClass)

    @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
    override fun afterPropertiesSet() {
        val keyByte = Base64.decode(secret)
        this.key = Keys.hmacShaKeyFor(keyByte)
    }

    fun createToken(uuid: UUID): String {
        val date = Date(System.currentTimeMillis() + accessTokenExpiresTime)
        return Jwts.builder()
                .setSubject(uuid.toString())
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(date)
                .compact()
    }

    fun getAuthenticationByToken(token: String): Authentication {
        val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        val principal = User(claims.subject, "", setOf(SimpleGrantedAuthority("user")))
        return UsernamePasswordAuthenticationToken(principal, token, setOf(SimpleGrantedAuthority("user")))
    }

    fun getUuidByToken(token: String): UUID {
        val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        return UUID.fromString(claims.subject)
    }

    fun validateToken(token: String): Jws<Claims> {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
    }
}