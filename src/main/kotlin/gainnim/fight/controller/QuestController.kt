package gainnim.fight.controller

import gainnim.fight.dto.response.QuestResponse
import gainnim.fight.service.QuestService
import gainnim.fight.util.ResponseFormat
import gainnim.fight.util.ResponseFormatBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/quest")
class QuestController(val questService: QuestService) {
    @GetMapping
    fun getQuest(principal: Principal): ResponseEntity<ResponseFormat<QuestResponse>> {
        val userId = UUID.fromString(principal.name)
        val result = questService.getQuest(userId)
        return ResponseEntity.ok(ResponseFormatBuilder { message = "success" }.build(result))
    }
}