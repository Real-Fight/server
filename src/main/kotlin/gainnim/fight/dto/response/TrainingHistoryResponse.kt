package gainnim.fight.dto.response

import gainnim.fight.entity.TrainingType
import java.util.*

data class TrainingHistoryResponse(
        val trainingType: TrainingType,
        val count: Long,
        val createdAt: Date
)
