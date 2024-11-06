package gainnim.fight.controller

import gainnim.fight.dto.response.HistoryResponse
import gainnim.fight.service.GameHistoryService
import gainnim.fight.util.ResponseFormat
import gainnim.fight.util.ResponseFormatBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/history")
class GameHistoryController(val gameHistoryService: GameHistoryService) {
    @GetMapping()
    fun getHistory(principal: Principal): ResponseEntity<ResponseFormat<List<HistoryResponse>>> {
        val userId = UUID.fromString(principal.name)
        val result = gameHistoryService.getHistory(userId)
        return ResponseEntity.ok(ResponseFormatBuilder { message = "success"}.build(result))
    }
}