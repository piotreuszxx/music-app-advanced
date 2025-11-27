package music.configuration.interceptor;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.SecurityContext;
import music.configuration.interceptor.binding.LogAccess;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
@LogAccess
@Priority(1000)
public class LogAccessInterceptor {

    private static final Logger LOG = Logger.getLogger(LogAccessInterceptor.class.getName());

    private final SecurityContext securityContext;

    @Inject
    public LogAccessInterceptor(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    @AroundInvoke
    public Object invoke(InvocationContext context) throws Exception {
        var principal = securityContext == null ? null : securityContext.getCallerPrincipal();
        if (principal == null) {
            return context.proceed();
        }

        Method method = context.getMethod();
        LogAccess ann = method.getAnnotation(LogAccess.class);
        String op = null;
        if (ann != null && ann.value() != null && ann.value().length > 0) {
            op = ann.value()[0];
        }
        if (op == null || op.isBlank()) {
            op = method.getName();
        }

        UUID elementId = extractElementId(context.getParameters());

        // log: username, operation, id
        String user = principal.getName();
        String idStr = elementId == null ? "-" : elementId.toString();
        String msg = String.format("[SECURITY] user=%s operation=%s resourceId=%s target=%s#%s",
                user, op, idStr, method.getDeclaringClass().getSimpleName(), method.getName());
        LOG.log(Level.INFO, msg);

        return context.proceed();
    }

    private UUID extractElementId(Object[] parameters) {
        if (parameters == null) return null;
        for (Object param : parameters) {
            if (param == null) continue;
            if (param instanceof UUID uuid) {
                return uuid;
            }
            // try to reflectively call getId() if present
            try {
                Method m = param.getClass().getMethod("getId");
                Object id = m.invoke(param);
                if (id instanceof UUID) return (UUID) id;
            } catch (Exception e)
            {
            }
        }
        return null;
    }
}
