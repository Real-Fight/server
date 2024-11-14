package gainnim.fight.dto.response

import gainnim.fight.entity.User
import java.util.UUID

data class UserInfoResponse(
        val id: UUID,
        val name: String,
        val profileImgUrl: String,
        val statusMessage: String,
        val strength: Long,
        val endurance: Long,
        val agility: Long,
        val willpower: Long,
        val totalPower: Long,
        val ranking: Long
) {
    companion object {
        fun fromUser(user: User, ranking: Long) = UserInfoResponse(
                id = user.id,
                name = user.name,
                profileImgUrl = user.profileImgUrl,
                statusMessage = user.statusMessage,
                strength = user.strength,
                endurance = user.endurance,
                agility = user.agility,
                willpower = user.willpower,
                totalPower = user.totalPower,
                ranking = ranking
        )
    }
}
