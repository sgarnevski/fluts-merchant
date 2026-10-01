package com.fluts.rest.web;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;

/**
 * Marks every record component as {@code required} in the OpenAPI schema unless it is annotated
 * {@link Nullable}. Without this, springdoc marks all properties optional, and the generated
 * TypeScript types would make every field {@code x?: ...}.
 */
public final class RequiredRecordComponentsConverter implements ModelConverter {

    @Override
    @SuppressWarnings("rawtypes")
    public @Nullable Schema resolve(final AnnotatedType type, final ModelConverterContext context,
            final Iterator<ModelConverter> chain) {
        final Schema schema = chain.next().resolve(type, context, chain);
        final Class<?> rawClass = Json.mapper().constructType(type.getType()).getRawClass();
        if (schema != null && rawClass.isRecord()) {
            markRequired(rawClass, modelOf(schema, context));
        }
        return schema;
    }

    /** Nested models are returned as a {@code $ref}; the model itself is in the context. */
    @SuppressWarnings("rawtypes")
    private static Schema modelOf(final Schema schema, final ModelConverterContext context) {
        final String ref = schema.get$ref();
        return ref == null ? schema : context.getDefinedModels().get(ref.substring(ref.lastIndexOf('/') + 1));
    }

    @SuppressWarnings("rawtypes")
    private static void markRequired(final Class<?> recordClass, final @Nullable Schema model) {
        if (model == null || model.getProperties() == null) {
            return;
        }
        Arrays.stream(recordClass.getRecordComponents())
                .filter(component -> !component.getAnnotatedType().isAnnotationPresent(Nullable.class))
                .map(RecordComponent::getName)
                .filter(model.getProperties()::containsKey)
                .filter(name -> model.getRequired() == null || !model.getRequired().contains(name))
                .forEach(model::addRequiredItem);
    }
}
