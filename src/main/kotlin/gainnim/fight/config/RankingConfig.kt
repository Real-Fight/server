package gainnim.fight.config

import gainnim.fight.repository.UserRepository
import gainnim.fight.service.RankingService
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RankingConfig(val userRepository: UserRepository, val rankingService: RankingService) {
    @Bean
    fun initializeRanking(): ApplicationRunner {
        return ApplicationRunner {
            val users = userRepository.findAll()
            rankingService.initializeRanking(users)
        }
    }
}