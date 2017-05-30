package exceptions

/**
 * @author kurochenko
 */
class InvalidRequestException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
