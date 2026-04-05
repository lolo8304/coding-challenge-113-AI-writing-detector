package ch.lolo.common.versioning;

import jakarta.servlet.http.HttpServletRequest;

public final class VersionContextHolder {

    public static final String ATTR_VERSION_CONTEXT = VersionContextHolder.class.getName() + ".context";

    private VersionContextHolder() {
    }

    public static void set(HttpServletRequest request, VersionContext context) {
        request.setAttribute(ATTR_VERSION_CONTEXT, context);
    }

    public static VersionContext getRequired(HttpServletRequest request) {
        Object attribute = request.getAttribute(ATTR_VERSION_CONTEXT);
        if (attribute instanceof VersionContext context) {
            return context;
        }

        ApiVersion latest = ApiVersion.latest();
        return new VersionContext(latest, java.util.List.of(), java.util.List.of());
    }
}

