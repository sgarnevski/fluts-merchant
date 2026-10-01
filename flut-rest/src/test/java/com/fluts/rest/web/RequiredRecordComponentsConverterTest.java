package com.fluts.rest.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContextImpl;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class RequiredRecordComponentsConverterTest {

    private final RequiredRecordComponentsConverter converter = new RequiredRecordComponentsConverter();

    record Sample(String name, @Nullable String nickname, String hidden) {
    }

    @Test
    void marksNonNullableRecordComponentsRequired() {
        final Schema<?> schema = new ObjectSchema()
                .addProperty("name", new StringSchema())
                .addProperty("nickname", new StringSchema());

        final Schema<?> resolved = resolve(Sample.class, schema);

        assertThat(resolved.getRequired()).containsExactly("name");
    }

    @Test
    void leavesSchemasWithoutPropertiesAlone() {
        assertThat(resolve(Sample.class, new ObjectSchema()).getRequired()).isNull();
    }

    @Test
    void leavesNonRecordsAlone() {
        final Schema<?> schema = new ObjectSchema().addProperty("value", new StringSchema());

        assertThat(resolve(String.class, schema).getRequired()).isNull();
    }

    @Test
    void marksNestedModelsReturnedAsReference() {
        final ModelConverterContextImpl context = new ModelConverterContextImpl(List.of());
        final Schema<?> model = new ObjectSchema().addProperty("name", new StringSchema());
        context.defineModel("Sample", model);

        resolve(Sample.class, new Schema<>().$ref("#/components/schemas/Sample"), context);

        assertThat(model.getRequired()).containsExactly("name");
    }

    @Test
    void ignoresReferencesToUnknownModels() {
        final Schema<?> reference = new Schema<>().$ref("#/components/schemas/Unknown");

        assertThat(resolve(Sample.class, reference).getRequired()).isNull();
    }

    @Test
    void doesNotDuplicateRequiredEntries() {
        final Schema<?> schema = new ObjectSchema().addProperty("name", new StringSchema()).addRequiredItem("name");

        assertThat(resolve(Sample.class, schema).getRequired()).containsExactly("name");
    }

    @Test
    void passesThroughMissingSchemas() {
        assertThat(resolve(Sample.class, null)).isNull();
    }

    @SuppressWarnings("rawtypes")
    private Schema resolve(final Class<?> type, final @Nullable Schema<?> next) {
        return resolve(type, next, new ModelConverterContextImpl(List.of()));
    }

    @SuppressWarnings("rawtypes")
    private Schema resolve(final Class<?> type, final @Nullable Schema<?> next, final ModelConverterContextImpl context) {
        final ModelConverter nextConverter = (annotatedType, ignoredContext, chain) -> next;
        return converter.resolve(new AnnotatedType(type), context, List.of(nextConverter).iterator());
    }
}
