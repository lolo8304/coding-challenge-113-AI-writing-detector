package ch.lolo.common.versioning;

import ch.lolo.common.exception.BadRequestException;
import ch.lolo.common.versioning.transform.JsonVersionTransformer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VersionTransformerFactory<T extends JsonVersionTransformer> {

    private final Map<VersionTransition, T> transformerByTransition;
    private final String errorCode;
    private final Function<VersionTransition, String> errorMessageFactory;

    public VersionTransformerFactory(
            List<T> transformers,
            String errorCode,
            Function<VersionTransition, String> errorMessageFactory
    ) {
        this.transformerByTransition = transformers.stream()
                .collect(Collectors.toMap(JsonVersionTransformer::transition, Function.identity()));
        this.errorCode = errorCode;
        this.errorMessageFactory = errorMessageFactory;
    }

    public T getRequired(VersionTransition transition) {
        T transformer = transformerByTransition.get(transition);
        if (transformer != null) {
            return transformer;
        }

        throw new BadRequestException(errorCode, errorMessageFactory.apply(transition));
    }
}

