package gainnim.fight.entity

import gainnim.fight.handler.MatchType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.Date
import java.util.UUID

@Entity
data class GameHistory(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID = UUID.randomUUID(),
        @ManyToOne
        val user: User,
        val matchType: MatchType,
        val result: Result,
        val score: Int,
        val createdAt: Date = Date()
)
enum class Result() {
    WIN, LOSE, DRAW
}