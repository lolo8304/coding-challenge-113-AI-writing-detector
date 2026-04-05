package ch.lolo.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InboundRequestCachingFilter extends OncePerRequestFilter {

    private static final int REQUEST_CACHE_LIMIT_BYTES = 1024 * 1024;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (request instanceof ContentCachingRequestWrapper) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT_BYTES);
        filterChain.doFilter(wrappedRequest, response);
    }
}
