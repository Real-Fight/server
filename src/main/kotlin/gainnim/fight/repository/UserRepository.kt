package gainnim.fight.repository

import gainnim.fight.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findUserById(id: String): User?
    fun findUserByName(name: String): User?
    fun findUserByLoginId(loginId: String): User?
}