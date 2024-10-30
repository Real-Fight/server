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
)