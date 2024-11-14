package gainnim.fight.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory =
            LettuceConnectionFactory()

    @Bean
    fun redisTemplate() = RedisTemplate<String, Any>().apply {
        connectionFactory = redisConnectionFactory()
        keySerializer = StringRedisSerializer()
        valueSerializer = StringRedisSerializer()
    }
}
