package exceptions

/**
 * @author kurochenko
 */
class CommunicationException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
