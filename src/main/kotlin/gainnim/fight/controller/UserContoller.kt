package gainnim.fight.controller

import gainnim.fight.dto.request.UserStatusMessageRequest
import gainnim.fight.dto.response.UserInfoResponse
import gainnim.fight.service.UserService
import gainnim.fight.util.ResponseFormat
import gainnim.fight.util.ResponseFormatBuilder
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/user")
class UserContoller(val userService: UserService) {
    @GetMapping()
    fun getMyInfo(principal: Principal): ResponseEntity<ResponseFormat<UserInfoResponse>> {
        val userId = UUID.fromString(principal.name)
        val result = userService.getMyInfo(userId)
        return ResponseEntity.ok(ResponseFormatBuilder { message = "success" }.build(result))
    }
    @PatchMapping("/status")
    fun patchUserStatusMessage(principal: Principal, request: UserStatusMessageRequest): ResponseEntity<ResponseFormat<Any>> {
        val userId = UUID.fromString(principal.name)
        userService.patchUserStatusMessage(userId, request)
        return ResponseEntity.ok(ResponseFormatBuilder { message = "success" }.noData())
    }
    @PatchMapping("/img", consumes = ["multipart/form-data"])
    fun patchUserImg(principal: Principal, @RequestPart("image") image: MultipartFile): ResponseEntity<ResponseFormat<Any>> {
        val userId = UUID.fromString(principal.name)
        userService.patchUserImg(userId, image)
        return ResponseEntity.ok(ResponseFormatBuilder { message = "success" }.noData())
    }
}