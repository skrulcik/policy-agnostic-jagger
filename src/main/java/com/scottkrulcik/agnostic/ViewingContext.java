package com.scottkrulcik.agnostic;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * The context under which data is being exposed.
 */
public final class ViewingContext {

    private final ImmutableMap<Class<@Viewer ?>,Object> container;

    public ViewingContext() {
        this(ImmutableMap.of());
    }

    public ViewingContext(Map<Class<?>, Object> container) {
        this.container = ImmutableMap.copyOf(container);
    }

    public <@Viewer T> T get(Class<T> classKey) {
        return classKey.cast(container.get(classKey));
    }

    public <@Viewer T> ViewingContext deriveContext(Class<T> classKey, T instance) {
        ImmutableMap<Class<@Viewer ?>, Object> newContainer =
            ImmutableMap.<Class<@Viewer ?>, Object>builder()
            .putAll(container)
            .put(classKey, instance)
            .build();
        return new ViewingContext();

    }
}
