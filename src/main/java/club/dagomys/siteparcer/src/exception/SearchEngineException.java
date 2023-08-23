package club.dagomys.siteparcer.src.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public abstract class SearchEngineException extends Throwable {
    public SearchEngineException(String message) {
        super(message);

    }
}
