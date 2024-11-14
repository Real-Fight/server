package gainnim.fight.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID = UUID.randomUUID(),
        val loginId: String,
        val password: String,
        val name: String,
        val profileImgUrl: String = "https://nonabili-bucket.s3.ap-northeast-2.amazonaws.com/profile_image/8b03de68-0fc5-41a0-b367-859d58c7af07", // todo split profile
        val statusMessage: String = "",
        val strength: Long = 0,  // todo extend table
        val endurance: Long = 0,
        val agility: Long = 0,
        val willpower: Long = 0,
        val totalPower: Long = 0
)