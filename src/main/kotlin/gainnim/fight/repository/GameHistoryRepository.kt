package gainnim.fight.repository

import gainnim.fight.entity.GameHistory
import gainnim.fight.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GameHistoryRepository: JpaRepository<GameHistory, UUID> {
    fun findGameHistoriesByUserOrderByCreatedAtDesc(user: User): List<GameHistory>
}