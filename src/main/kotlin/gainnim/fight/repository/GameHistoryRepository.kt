package gainnim.fight.repository

import gainnim.fight.entity.GameHistory
import gainnim.fight.entity.User
import gainnim.fight.handler.MatchType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GameHistoryRepository: JpaRepository<GameHistory, UUID> {
    fun findGameHistoriesByUserOrderByCreatedAtDesc(user: User): List<GameHistory>
    fun countGameHistoriesByUserAndCreatedAtBetween(user: User, startOfDay: Date, endOfDay: Date): Int
    fun findGameHistoriesByUserAndMatchTypeInAndCreatedAtBetween(user: User ,matchTypes: List<MatchType>, startOfDay: Date, endOfDay: Date): List<GameHistory>
}