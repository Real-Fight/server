package gainnim.fight.dto.response

import gainnim.fight.entity.Result
import gainnim.fight.handler.MatchType

data class HistoryResponse(
        val matchType: MatchType,
        val result: Result,
        val score: Int
)
