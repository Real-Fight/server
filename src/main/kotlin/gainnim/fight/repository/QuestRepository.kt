package gainnim.fight.repository

import gainnim.fight.entity.Quest
import gainnim.fight.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuestRepository: JpaRepository<Quest, UUID> {
    fun findQuestByUser(user: User): Quest?
}