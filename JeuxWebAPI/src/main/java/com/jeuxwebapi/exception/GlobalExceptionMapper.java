package com.jeuxwebapi.exception;

import com.jeuxwebapi.models.ApiErrorResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Inject
    UriInfo uriInfo;

    @ConfigProperty(name = "jeuxwebapi.api.errors.include-stack-trace", defaultValue = "true")
    boolean includeStackTrace;

    @Override
    public Response toResponse(Throwable exception) {
        return buildResponse(exception);
    }

    /** Обрабатывает сбои в reactive-цепочках ({@code Uni} и т.д.). */
    @ServerExceptionMapper
    public Response mapReactiveFailure(Throwable exception) {
        return buildResponse(exception);
    }

    private Response buildResponse(Throwable raw) {
        Throwable exception = unwrap(raw);

        int status = resolveStatus(exception);
        String reasonPhrase = resolveReasonPhrase(status);

        LOG.errorf(exception, "Ошибка обработки запроса%s: %s",
                uriInfo != null && uriInfo.getPath() != null ? " (" + uriInfo.getPath() + ")" : "",
                exception.getMessage());

        ApiErrorResponse body = new ApiErrorResponse();
        body.setStatus(status);
        body.setError(reasonPhrase);
        body.setMessage(exception.getMessage() != null ? exception.getMessage() : exception.toString());
        body.setExceptionType(exception.getClass().getName());
        if (uriInfo != null) {
            body.setPath(uriInfo.getPath());
        }
        if (includeStackTrace) {
            body.setStackTrace(formatStackTrace(exception));
        }

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private static int resolveStatus(Throwable exception) {
        if (exception instanceof WebApplicationException wae && wae.getResponse() != null) {
            int s = wae.getResponse().getStatus();
            if (s > 0) {
                return s;
            }
        }
        if (exception instanceof IllegalArgumentException) {
            return Response.Status.BAD_REQUEST.getStatusCode();
        }
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    private static String resolveReasonPhrase(int status) {
        Response.Status st = Response.Status.fromStatusCode(status);
        return st != null ? st.getReasonPhrase() : "Error";
    }

    private static Throwable unwrap(Throwable t) {
        Throwable c = t;
        int depth = 0;
        while (c.getCause() != null
                && depth < 20
                && (c instanceof java.util.concurrent.CompletionException
                        || c instanceof java.util.concurrent.ExecutionException)) {
            c = c.getCause();
            depth++;
        }
        return c;
    }

    private static String formatStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
