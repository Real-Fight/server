package gainnim.fight.dto.request

import gainnim.fight.entity.TrainingType

data class TrainingHistoryRequest(
        val trainingType: TrainingType,
        val count: Long
)
