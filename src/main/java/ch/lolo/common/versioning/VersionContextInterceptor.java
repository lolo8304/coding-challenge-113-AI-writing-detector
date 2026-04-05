package ch.lolo.common.versioning;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class VersionContextInterceptor implements HandlerInterceptor {

    private final VersionChainPlanner versionChainPlanner;

    public VersionContextInterceptor(VersionChainPlanner versionChainPlanner) {
        this.versionChainPlanner = versionChainPlanner;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        ApiVersion requestedVersion = ApiVersion.resolve(request.getHeader(ApiVersion.VERSION_HEADER));
        ApiVersion latestVersion = ApiVersion.latest();

        List<VersionTransition> upgradeTransitions =
                versionChainPlanner.planUpgradeChain(requestedVersion, latestVersion);
        List<VersionTransition> downgradeTransitions =
                versionChainPlanner.planDowngradeChain(latestVersion, requestedVersion);

        VersionContextHolder.set(
                request,
                new VersionContext(requestedVersion, upgradeTransitions, downgradeTransitions)
        );

        return true;
    }
}

