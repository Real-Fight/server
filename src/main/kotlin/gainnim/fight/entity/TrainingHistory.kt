package gainnim.fight.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

@Entity
data class TrainingHistory(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID = UUID.randomUUID(),
        @ManyToOne
        val user: User,
        val trainingType: TrainingType,
        val count: Long,
        val createdAt: Date = Date()
)
enum class TrainingType {
    SQUAT, PUSHUP, SITUP
}