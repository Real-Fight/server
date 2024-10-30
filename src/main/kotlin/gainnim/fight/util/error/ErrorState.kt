package gainnim.fight.util.error

import org.springframework.http.HttpStatus

enum class ErrorState(val status: HttpStatus = HttpStatus.OK, val message: String) {

    ERROR_FORMAT(HttpStatus.BAD_REQUEST, "It's test"),
    ACCESSTOKEN_HAS_EXPIRED(HttpStatus.BAD_REQUEST, "AccessToken has expired"),

    ID_IS_ALREADY_USED(HttpStatus.BAD_REQUEST, "Id is already used"),
    NAME_IS_ALREADY_USED(HttpStatus.BAD_REQUEST, "Name is already used"),
    TELL_IS_ALREADY_USED(HttpStatus.BAD_REQUEST, "Tell is already used"),
    EMAIL_IS_ALREADY_USED(HttpStatus.BAD_REQUEST, "Email is already used"),

    WANT_IS_ALREADY_EXISTED(HttpStatus.BAD_REQUEST, "Want is already existed"),

    NOT_FOUND_ID(HttpStatus.NOT_FOUND, "Not found Id"),
    NOT_FOUND_REFRESHTOKEN(HttpStatus.NOT_FOUND, "Not found RefreshToken"),
    NOT_FOUND_USER(HttpStatus.FORBIDDEN, "User not found"),
    NOT_FOUND_SHARE(HttpStatus.NOT_FOUND, "Share not found"),
    NOT_FOUND_WANT(HttpStatus.NOT_FOUND, "Want not found"),
    NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, "Product not found"),

    YOUR_NOT_OWNER(HttpStatus.FORBIDDEN, "Your not owner"),
    YOU_ARE_POOL(HttpStatus.BAD_REQUEST, "You do not have enough social credit to buy a product"),
    YOU_CAN_NOT_WANT_IT(HttpStatus.BAD_REQUEST, "You can not want it"),

    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "Wrong password"),
    WRONG_ADRESS(HttpStatus.BAD_REQUEST, "Wrong adress"),
    WRONG_CATEGORY(HttpStatus.BAD_REQUEST, "Wrong category"),

    NOT_VERIFED_EMAIL(HttpStatus.BAD_REQUEST, "Email not verifed"),
    NOT_VERIFED_TELL(HttpStatus.BAD_REQUEST, "Tell not verifed")
}