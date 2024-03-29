package club.dagomys.siteparser.src.exception;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class SiteParserAsyncException extends SearchEngineException implements AsyncUncaughtExceptionHandler {
    public SiteParserAsyncException(String message) {
        super(message);
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        System.out.println("Throwable Exception message : " + ex.getMessage());
        System.out.println("Method name                 : " + method.getName());
        for (Object param : params) {
            System.out.println("Parameter value             : " + param);
        }
        System.out.println("stack Trace ");
        ex.printStackTrace();
    }
}
