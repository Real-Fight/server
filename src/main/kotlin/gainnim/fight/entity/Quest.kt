package gainnim.fight.entity

import gainnim.fight.handler.MatchType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import java.util.*

@Entity
data class Quest(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID = UUID.randomUUID(),
        @OneToOne
        val user: User,
        val type: QuestType,
        val level: Long,
        val count: Long = 0,
        val completed: Boolean = false
)
enum class QuestType(val length: Int, val message: String) {
    SQUAT(20, "총 스쿼트"),
    PUSHUP(20, "총 푸쉬업"),
    MATCH(1, "운동한판")
}
