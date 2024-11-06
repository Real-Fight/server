package gainnim.fight.controller

import gainnim.fight.dto.response.RankingResponse
import gainnim.fight.service.RankingService
import gainnim.fight.util.ResponseFormat
import gainnim.fight.util.ResponseFormatBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ranking")
class RankingContoller(val rankingService: RankingService) {
    @GetMapping
    fun getRanking(page: Long): ResponseEntity<ResponseFormat<List<RankingResponse>>> {
        val result = rankingService.getRanking(page)
        return ResponseEntity.ok(ResponseFormatBuilder { message = "success" }.build(result))
    }
}