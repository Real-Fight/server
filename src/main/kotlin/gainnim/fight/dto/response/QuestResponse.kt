package gainnim.fight.dto.response

import gainnim.fight.entity.QuestType

data class QuestResponse(
        val message: String,
        val questType: QuestType,
        val completed: Boolean
)
