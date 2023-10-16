package club.dagomys.siteparcer.src.exception;

//@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class SearchEngineException extends Throwable {
    public SearchEngineException(String message) {
        super(message);

    }
}
