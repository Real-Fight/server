package gainnim.fight.repository

import gainnim.fight.entity.TrainingHistory
import gainnim.fight.entity.TrainingType
import gainnim.fight.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TrainingHistoryRepository: JpaRepository<TrainingHistory, UUID> {
    fun findTrainingHistoriesByUserAndTrainingTypeOrderByCreatedAtDesc(user: User, trainingType: TrainingType): List<TrainingHistory>
}