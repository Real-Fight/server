package gainnim.fight.dto.response

import java.util.UUID

data class RankingResponse(
        val id: UUID,
        val name: String,
        val profileImgUrl: String,
        val ranking: Long,
        val statusMessage: String,
        val totalPower: Long
)
