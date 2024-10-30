package gainnim.fight.util.error

class CustomError(val reason: ErrorState): RuntimeException(reason.message)