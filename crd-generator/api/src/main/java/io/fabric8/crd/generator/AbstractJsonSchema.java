/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.crd.generator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import io.fabric8.crd.generator.InternalSchemaSwaps.SwapResult;
import io.fabric8.crd.generator.annotation.SchemaSwap;
import io.fabric8.crd.generator.utils.Types;
import io.fabric8.generator.annotation.ValidationRule;
import io.fabric8.kubernetes.api.model.Duration;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import io.sundr.builder.internal.functions.TypeAs;
import io.sundr.model.AnnotationRef;
import io.sundr.model.ClassRef;
import io.sundr.model.Method;
import io.sundr.model.PrimitiveRefBuilder;
import io.sundr.model.Property;
import io.sundr.model.TypeDef;
import io.sundr.model.TypeRef;
import io.sundr.model.functions.GetDefinition;
import io.sundr.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.sundr.model.utils.Types.BOOLEAN_REF;
import static io.sundr.model.utils.Types.DOUBLE_REF;
import static io.sundr.model.utils.Types.FLOAT_REF;
import static io.sundr.model.utils.Types.INT_REF;
import static io.sundr.model.utils.Types.LONG_REF;
import static io.sundr.model.utils.Types.STRING_REF;
import static io.sundr.model.utils.Types.VOID;

/**
 * Encapsulates the common logic supporting OpenAPI schema generation for CRD generation.
 *
 * @param <T> the concrete type of the generated JSON Schema
 * @param <B> the concrete type of the JSON Schema builder
 */
public abstract class AbstractJsonSchema<T, B> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJsonSchema.class);

  protected static final TypeDef OBJECT = TypeDef.forName(Object.class.getName());
  protected static final TypeDef QUANTITY = TypeDef.forName(Quantity.class.getName());
  protected static final TypeDef DURATION = TypeDef.forName(Duration.class.getName());
  protected static final TypeDef INT_OR_STRING = TypeDef.forName(IntOrString.class.getName());

  protected static final TypeRef OBJECT_REF = OBJECT.toReference();
  protected static final TypeRef QUANTITY_REF = QUANTITY.toReference();
  protected static final TypeRef DURATION_REF = DURATION.toReference();
  protected static final TypeRef INT_OR_STRING_REF = INT_OR_STRING.toReference();

  protected static final TypeDef DATE = TypeDef.forName(Date.class.getName());
  protected static final TypeRef DATE_REF = DATE.toReference();

  private static final String JSON_FORMAT_SHAPE = "shape";
  private static final Map<JsonFormat.Shape, TypeRef> JSON_FORMAT_SHAPE_MAPPING = new HashMap<>();
  private static final String VALUE = "value";

  private static final String INT_OR_STRING_MARKER = "int_or_string";
  private static final String STRING_MARKER = "string";
  private static final String INTEGER_MARKER = "integer";
  private static final String NUMBER_MARKER = "number";
  private static final String BOOLEAN_MARKER = "boolean";

  protected static final TypeRef P_INT_REF = new PrimitiveRefBuilder().withName("int").build();
  protected static final TypeRef P_LONG_REF = new PrimitiveRefBuilder().withName("long").build();
  protected static final TypeRef P_FLOAT_REF = new PrimitiveRefBuilder().withName("float").build();
  protected static final TypeRef P_DOUBLE_REF = new PrimitiveRefBuilder().withName("double").build();
  protected static final TypeRef P_BOOLEAN_REF = new PrimitiveRefBuilder().withName(BOOLEAN_MARKER)
      .build();

  private static final Map<TypeRef, String> COMMON_MAPPINGS = new HashMap<>();
  public static final String ANNOTATION_JSON_FORMAT = "com.fasterxml.jackson.annotation.JsonFormat";
  public static final String ANNOTATION_JSON_PROPERTY = "com.fasterxml.jackson.annotation.JsonProperty";
  public static final String ANNOTATION_JSON_PROPERTY_DESCRIPTION = "com.fasterxml.jackson.annotation.JsonPropertyDescription";
  public static final String ANNOTATION_JSON_IGNORE = "com.fasterxml.jackson.annotation.JsonIgnore";
  public static final String ANNOTATION_JSON_ANY_GETTER = "com.fasterxml.jackson.annotation.JsonAnyGetter";
  public static final String ANNOTATION_JSON_ANY_SETTER = "com.fasterxml.jackson.annotation.JsonAnySetter";
  public static final String ANNOTATION_DEFAULT = "io.fabric8.generator.annotation.Default";
  public static final String ANNOTATION_MIN = "io.fabric8.generator.annotation.Min";
  public static final String ANNOTATION_MAX = "io.fabric8.generator.annotation.Max";
  public static final String ANNOTATION_PATTERN = "io.fabric8.generator.annotation.Pattern";
  public static final String ANNOTATION_NULLABLE = "io.fabric8.generator.annotation.Nullable";
  public static final String ANNOTATION_REQUIRED = "io.fabric8.generator.annotation.Required";
  public static final String ANNOTATION_SCHEMA_FROM = "io.fabric8.crd.generator.annotation.SchemaFrom";
  public static final String ANNOTATION_PERSERVE_UNKNOWN_FIELDS = "io.fabric8.crd.generator.annotation.PreserveUnknownFields";
  public static final String ANNOTATION_SCHEMA_SWAP = "io.fabric8.crd.generator.annotation.SchemaSwap";
  public static final String ANNOTATION_SCHEMA_SWAPS = "io.fabric8.crd.generator.annotation.SchemaSwaps";
  public static final String ANNOTATION_VALIDATION_RULE = "io.fabric8.generator.annotation.ValidationRule";
  public static final String ANNOTATION_VALIDATION_RULES = "io.fabric8.generator.annotation.ValidationRules";

  public static final String JSON_NODE_TYPE = "com.fasterxml.jackson.databind.JsonNode";
  public static final String ANY_TYPE = "io.fabric8.kubernetes.api.model.AnyType";

  private static final JsonSchemaGenerator GENERATOR;
  private static final Set<String> COMPLEX_JAVA_TYPES = new HashSet<>();

  static {
    COMMON_MAPPINGS.put(STRING_REF, STRING_MARKER);
    COMMON_MAPPINGS.put(DATE_REF, STRING_MARKER);
    COMMON_MAPPINGS.put(INT_REF, INTEGER_MARKER);
    COMMON_MAPPINGS.put(P_INT_REF, INTEGER_MARKER);
    COMMON_MAPPINGS.put(LONG_REF, INTEGER_MARKER);
    COMMON_MAPPINGS.put(P_LONG_REF, INTEGER_MARKER);
    COMMON_MAPPINGS.put(FLOAT_REF, NUMBER_MARKER);
    COMMON_MAPPINGS.put(P_FLOAT_REF, NUMBER_MARKER);
    COMMON_MAPPINGS.put(DOUBLE_REF, NUMBER_MARKER);
    COMMON_MAPPINGS.put(P_DOUBLE_REF, NUMBER_MARKER);
    COMMON_MAPPINGS.put(BOOLEAN_REF, BOOLEAN_MARKER);
    COMMON_MAPPINGS.put(P_BOOLEAN_REF, BOOLEAN_MARKER);
    COMMON_MAPPINGS.put(QUANTITY_REF, INT_OR_STRING_MARKER);
    COMMON_MAPPINGS.put(INT_OR_STRING_REF, INT_OR_STRING_MARKER);
    COMMON_MAPPINGS.put(DURATION_REF, STRING_MARKER);
    ObjectMapper mapper = new ObjectMapper();
    // initialize with client defaults
    new KubernetesSerialization(mapper, false);
    GENERATOR = new JsonSchemaGenerator(mapper);

    JSON_FORMAT_SHAPE_MAPPING.put(JsonFormat.Shape.BOOLEAN, Types.typeDefFrom(Boolean.class).toReference());
    JSON_FORMAT_SHAPE_MAPPING.put(JsonFormat.Shape.NUMBER, Types.typeDefFrom(Double.class).toReference());
    JSON_FORMAT_SHAPE_MAPPING.put(JsonFormat.Shape.NUMBER_FLOAT, Types.typeDefFrom(Double.class).toReference());
    JSON_FORMAT_SHAPE_MAPPING.put(JsonFormat.Shape.NUMBER_INT, Types.typeDefFrom(Long.class).toReference());
    JSON_FORMAT_SHAPE_MAPPING.put(JsonFormat.Shape.STRING, Types.typeDefFrom(String.class).toReference());
  }

  public static String getSchemaTypeFor(TypeRef typeRef) {
    String type = COMMON_MAPPINGS.get(typeRef);
    if (type == null && typeRef instanceof ClassRef) { // Handle complex types
      ClassRef classRef = (ClassRef) typeRef;
      TypeDef def = Types.typeDefFrom(classRef);
      type = def.isEnum() ? STRING_MARKER : "object";
    }
    return type;
  }

  protected static class SchemaPropsOptions {
    final String defaultValue;
    final Double min;
    final Double max;
    final String pattern;
    final boolean nullable;
    final boolean required;
    final boolean preserveUnknownFields;
    final List<KubernetesValidationRule> validationRules;

    SchemaPropsOptions() {
      defaultValue = null;
      min = null;
      max = null;
      pattern = null;
      nullable = false;
      required = false;
      preserveUnknownFields = false;
      validationRules = null;
    }

    public SchemaPropsOptions(String defaultValue, Double min, Double max, String pattern,
        List<KubernetesValidationRule> validationRules,
        boolean nullable, boolean required, boolean preserveUnknownFields) {
      this.defaultValue = defaultValue;
      this.min = min;
      this.max = max;
      this.pattern = pattern;
      this.nullable = nullable;
      this.required = required;
      this.preserveUnknownFields = preserveUnknownFields;
      this.validationRules = validationRules;
    }

    public Optional<String> getDefault() {
      return Optional.ofNullable(defaultValue);
    }

    public Optional<Double> getMin() {
      return Optional.ofNullable(min);
    }

    public Optional<Double> getMax() {
      return Optional.ofNullable(max);
    }

    public Optional<String> getPattern() {
      return Optional.ofNullable(pattern);
    }

    public boolean isNullable() {
      return nullable;
    }

    public boolean getRequired() {
      return required;
    }

    public boolean isPreserveUnknownFields() {
      return preserveUnknownFields;
    }

    public List<KubernetesValidationRule> getValidationRules() {
      return Optional.ofNullable(validationRules)
          .orElseGet(Collections::emptyList);
    }
  }

  /**
   * Creates the JSON schema for the particular {@link TypeDef}. This is template method where
   * sub-classes are supposed to provide specific implementations of abstract methods.
   *
   * @param definition The definition.
   * @param ignore a potentially empty list of property names to ignore while generating the schema
   * @return The schema.
   */
  protected T internalFrom(TypeDef definition, String... ignore) {
    InternalSchemaSwaps schemaSwaps = new InternalSchemaSwaps();
    return internalFromImpl(definition, new LinkedHashMap<>(), schemaSwaps, ignore);
  }

  private static ClassRef extractClassRef(Object type) {
    if (type != null) {
      if (type instanceof ClassRef) {
        return (ClassRef) type;
      } else if (type instanceof Class) {
        return Types.typeDefFrom((Class<?>) type).toReference();
      } else {
        throw new IllegalArgumentException("Unmanaged type passed to the annotation " + type);
      }
    } else {
      return null;
    }
  }

  private void extractSchemaSwaps(ClassRef definitionType, AnnotationRef annotation, InternalSchemaSwaps schemaSwaps) {
    String fullyQualifiedName = annotation.getClassRef().getFullyQualifiedName();
    switch (fullyQualifiedName) {
      case ANNOTATION_SCHEMA_SWAP:
        extractSchemaSwap(definitionType, annotation, schemaSwaps);
        break;
      case ANNOTATION_SCHEMA_SWAPS:
        Map<String, Object> params = annotation.getParameters();
        Object[] values = (Object[]) params.get("value");
        for (Object value : values) {
          extractSchemaSwap(definitionType, value, schemaSwaps);
        }
        break;
    }
  }

  private void extractSchemaSwap(ClassRef definitionType, Object annotation, InternalSchemaSwaps schemaSwaps) {
    if (annotation instanceof SchemaSwap) {
      SchemaSwap schemaSwap = (SchemaSwap) annotation;
      schemaSwaps.registerSwap(definitionType,
          extractClassRef(schemaSwap.originalType()),
          schemaSwap.fieldName(),
          extractClassRef(schemaSwap.targetType()), schemaSwap.depth());

    } else if (annotation instanceof AnnotationRef
        && ((AnnotationRef) annotation).getClassRef().getFullyQualifiedName().equals(ANNOTATION_SCHEMA_SWAP)) {
      Map<String, Object> params = ((AnnotationRef) annotation).getParameters();
      schemaSwaps.registerSwap(definitionType,
          extractClassRef(params.get("originalType")),
          (String) params.get("fieldName"),
          extractClassRef(params.getOrDefault("targetType", void.class)), (Integer) params.getOrDefault("depth", 0));

    } else {
      throw new IllegalArgumentException("Unmanaged annotation type passed to the SchemaSwaps: " + annotation);
    }
  }

  private static Stream<KubernetesValidationRule> extractKubernetesValidationRules(AnnotationRef annotationRef) {
    switch (annotationRef.getClassRef().getFullyQualifiedName()) {
      case ANNOTATION_VALIDATION_RULE:
        return Stream.of(KubernetesValidationRule.from(annotationRef));
      case ANNOTATION_VALIDATION_RULES:
        return Arrays.stream(((ValidationRule[]) annotationRef.getParameters().get(VALUE)))
            .map(KubernetesValidationRule::from);
      default:
        return Stream.empty();
    }
  }

  private T internalFromImpl(TypeDef definition, LinkedHashMap<String, String> visited, InternalSchemaSwaps schemaSwaps,
      String... ignore) {
    Set<String> ignores = ignore.length > 0 ? new LinkedHashSet<>(Arrays.asList(ignore))
        : Collections
            .emptySet();
    List<String> required = new ArrayList<>();

    final boolean isJsonNode = (definition.getFullyQualifiedName() != null &&
        (definition.getFullyQualifiedName().equals(JSON_NODE_TYPE)
            || definition.getFullyQualifiedName().equals(ANY_TYPE)));

    final B builder = (isJsonNode) ? newBuilder(null) : newBuilder();

    boolean preserveUnknownFields = isJsonNode;

    schemaSwaps = schemaSwaps.branchAnnotations();
    final InternalSchemaSwaps swaps = schemaSwaps;
    definition.getAnnotations().forEach(annotation -> extractSchemaSwaps(definition.toReference(), annotation, swaps));

    // index potential accessors by name for faster lookup
    final Map<String, Method> accessors = indexPotentialAccessors(definition);

    for (Property property : definition.getProperties()) {
      if (isJsonNode) {
        break;
      }
      String name = property.getName();
      if (property.isStatic() || ignores.contains(name)) {
        LOGGER.debug("Ignoring property {}", name);
        continue;
      }

      schemaSwaps = schemaSwaps.branchDepths();
      SwapResult swapResult = schemaSwaps.lookupAndMark(definition.toReference(), name);
      LinkedHashMap<String, String> savedVisited = visited;
      if (swapResult.onGoing) {
        visited = new LinkedHashMap<>();
      }
      final PropertyFacade facade = new PropertyFacade(property, accessors, swapResult.classRef);
      final Property possiblyRenamedProperty = facade.process();
      name = possiblyRenamedProperty.getName();

      if (facade.required) {
        required.add(name);
      } else if (facade.ignored) {
        continue;
      }
      final T schema = internalFromImpl(name, possiblyRenamedProperty.getTypeRef(), visited, schemaSwaps);
      visited = savedVisited;
      if (facade.preserveUnknownFields) {
        preserveUnknownFields = true;
      }

      // if we got a description from the field or an accessor, use it
      final String description = facade.description;
      final T possiblyUpdatedSchema;
      if (description == null) {
        possiblyUpdatedSchema = schema;
      } else {
        possiblyUpdatedSchema = addDescription(schema, description);
      }

      SchemaPropsOptions options = new SchemaPropsOptions(
          facade.defaultValue,
          facade.min,
          facade.max,
          facade.pattern,
          facade.validationRules,
          facade.nullable,
          facade.required,
          facade.preserveUnknownFields);

      addProperty(possiblyRenamedProperty, builder, possiblyUpdatedSchema, options);
    }

    List<KubernetesValidationRule> validationRules = Stream
        .concat(definition.getAnnotations().stream(), definition.getExtendsList().stream()
            .flatMap(classRef -> GetDefinition.of(classRef).getAnnotations().stream()))
        .flatMap(AbstractJsonSchema::extractKubernetesValidationRules)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    swaps.throwIfUnmatchedSwaps();

    List<String> sortedRequiredProperties = required.stream().sorted()
        .collect(Collectors.toList());

    return build(builder, sortedRequiredProperties, validationRules, preserveUnknownFields);
  }

  private Map<String, Method> indexPotentialAccessors(TypeDef definition) {
    final List<Method> methods = definition.getMethods();
    final Map<String, Method> accessors = new HashMap<>(methods.size());
    methods.stream()
        .filter(this::isPotentialAccessor)
        .forEach(m -> accessors.put(m.getName(), m));
    return accessors;
  }

  private static class PropertyOrAccessor {
    private final Collection<AnnotationRef> annotations;
    private final String name;
    private final String propertyName;
    private final String type;
    private String renamedTo;
    private String defaultValue;
    private Double min;
    private Double max;
    private String pattern;
    private List<KubernetesValidationRule> validationRules;
    private boolean nullable;
    private boolean required;
    private boolean ignored;
    private boolean preserveUnknownFields;
    private String description;
    private TypeRef schemaFrom;

    private PropertyOrAccessor(Collection<AnnotationRef> annotations, String name, String propertyName, boolean isMethod) {
      this.annotations = annotations;
      this.name = name;
      this.propertyName = propertyName;
      type = isMethod ? "accessor" : "field";
    }

    static PropertyOrAccessor fromProperty(Property property) {
      return new PropertyOrAccessor(property.getAnnotations(), property.getName(), property.getName(), false);
    }

    static PropertyOrAccessor fromMethod(Method method, String propertyName) {
      return new PropertyOrAccessor(method.getAnnotations(), method.getName(), propertyName, true);
    }

    public void process() {
      annotations.forEach(a -> {
        switch (a.getClassRef().getFullyQualifiedName()) {
          case ANNOTATION_DEFAULT:
            defaultValue = (String) a.getParameters().get(VALUE);
            break;
          case ANNOTATION_NULLABLE:
            nullable = true;
            break;
          case ANNOTATION_MAX:
            max = (Double) a.getParameters().get(VALUE);
            break;
          case ANNOTATION_MIN:
            min = (Double) a.getParameters().get(VALUE);
            break;
          case ANNOTATION_PATTERN:
            pattern = (String) a.getParameters().get(VALUE);
            break;
          case ANNOTATION_REQUIRED:
            required = true;
            break;
          case ANNOTATION_JSON_FORMAT:
            if (schemaFrom == null) {
              schemaFrom = JSON_FORMAT_SHAPE_MAPPING.get((JsonFormat.Shape) a.getParameters().get(JSON_FORMAT_SHAPE));
            }
            break;
          case ANNOTATION_JSON_PROPERTY:
            final String nameFromAnnotation = (String) a.getParameters().get(VALUE);
            if (!Strings.isNullOrEmpty(nameFromAnnotation) && !propertyName.equals(nameFromAnnotation)) {
              renamedTo = nameFromAnnotation;
            }
            break;
          case ANNOTATION_JSON_PROPERTY_DESCRIPTION:
            final String descriptionFromAnnotation = (String) a.getParameters().get(VALUE);
            if (!Strings.isNullOrEmpty(descriptionFromAnnotation)) {
              description = descriptionFromAnnotation;
            }
            break;
          case ANNOTATION_JSON_IGNORE:
            ignored = true;
            break;
          case ANNOTATION_JSON_ANY_GETTER:
          case ANNOTATION_JSON_ANY_SETTER:
          case ANNOTATION_PERSERVE_UNKNOWN_FIELDS:
            preserveUnknownFields = true;
            break;
          case ANNOTATION_SCHEMA_FROM:
            schemaFrom = extractClassRef(a.getParameters().get("type"));
            break;
          case ANNOTATION_VALIDATION_RULE:
          case ANNOTATION_VALIDATION_RULES:
            validationRules = extractKubernetesValidationRules(a).collect(Collectors.toList());
            break;
        }
      });
    }

    public String getRenamedTo() {
      return renamedTo;
    }

    public boolean isNullable() {
      return nullable;
    }

    public Optional<String> getDefault() {
      return Optional.ofNullable(defaultValue);
    }

    public Optional<Double> getMax() {
      return Optional.ofNullable(max);
    }

    public Optional<Double> getMin() {
      return Optional.ofNullable(min);
    }

    public Optional<String> getPattern() {
      return Optional.ofNullable(pattern);
    }

    public Optional<List<KubernetesValidationRule>> getValidationRules() {
      return Optional.ofNullable(validationRules);
    }

    public boolean isRequired() {
      return required;
    }

    public boolean isIgnored() {
      return ignored;
    }

    public boolean isPreserveUnknownFields() {
      return preserveUnknownFields;
    }

    public String getDescription() {
      return description;
    }

    public boolean contributeName() {
      return renamedTo != null;
    }

    public boolean contributeDescription() {
      return description != null;
    }

    public TypeRef getSchemaFrom() {
      return schemaFrom;
    }

    public boolean contributeSchemaFrom() {
      return schemaFrom != null;
    }

    @Override
    public String toString() {
      return "'" + name + "' " + type;
    }
  }

  private static class PropertyFacade {
    private final List<PropertyOrAccessor> propertyOrAccessors = new ArrayList<>(4);
    private String renamedTo;
    private String description;
    private String defaultValue;
    private Double min;
    private Double max;
    private String pattern;
    private boolean nullable;
    private boolean required;
    private boolean ignored;
    private boolean preserveUnknownFields;
    private final Property original;
    private String nameContributedBy;
    private String descriptionContributedBy;
    private TypeRef schemaFrom;
    private List<KubernetesValidationRule> validationRules;

    public PropertyFacade(Property property, Map<String, Method> potentialAccessors, ClassRef schemaSwap) {
      original = property;
      final String capitalized = property.getNameCapitalized();
      final String name = property.getName();
      propertyOrAccessors.add(PropertyOrAccessor.fromProperty(property));
      Method method = potentialAccessors.get("is" + capitalized);
      if (method != null) {
        propertyOrAccessors.add(PropertyOrAccessor.fromMethod(method, name));
      }
      method = potentialAccessors.get("get" + capitalized);
      if (method != null) {
        propertyOrAccessors.add(PropertyOrAccessor.fromMethod(method, name));
      }
      method = potentialAccessors.get("set" + capitalized);
      if (method != null) {
        propertyOrAccessors.add(PropertyOrAccessor.fromMethod(method, name));
      }
      schemaFrom = schemaSwap;
      defaultValue = null;
      min = null;
      max = null;
      pattern = null;
      validationRules = new LinkedList<>();
    }

    public Property process() {
      final String name = original.getName();

      propertyOrAccessors.forEach(p -> {
        p.process();
        final String contributorName = p.toString();
        if (p.contributeName()) {
          if (renamedTo == null) {
            renamedTo = p.getRenamedTo();
            this.nameContributedBy = contributorName;
          } else {
            LOGGER.debug("Property {} has already been renamed to {} by {}", name, renamedTo, nameContributedBy);
          }
        }

        if (p.contributeDescription()) {
          if (description == null) {
            description = p.getDescription();
            descriptionContributedBy = contributorName;
          } else {
            LOGGER.debug("Description for property {} has already been contributed by: {}", name, descriptionContributedBy);
          }
        }
        defaultValue = p.getDefault().orElse(defaultValue);
        min = p.getMin().orElse(min);
        max = p.getMax().orElse(max);
        pattern = p.getPattern().orElse(pattern);
        p.getValidationRules().ifPresent(rules -> validationRules.addAll(rules));

        if (p.isNullable()) {
          nullable = true;
        }

        if (p.isRequired()) {
          required = true;
        } else if (p.isIgnored()) {
          ignored = true;
        }

        preserveUnknownFields = p.isPreserveUnknownFields() || preserveUnknownFields;

        if (p.contributeSchemaFrom()) {
          schemaFrom = p.getSchemaFrom();
        }
      });

      TypeRef typeRef = schemaFrom != null ? schemaFrom : original.getTypeRef();
      String finalName = renamedTo != null ? renamedTo : original.getName();

      return new Property(original.getAnnotations(), typeRef, finalName,
          original.getComments(), false, false, original.getModifiers(), original.getAttributes());
    }
  }

  /**
   * Version independent DTO for a ValidationRule
   */
  protected static class KubernetesValidationRule {
    private String fieldPath;
    private String message;
    private String messageExpression;
    private Boolean optionalOldSelf;
    private String reason;
    private String rule;

    public String getFieldPath() {
      return fieldPath;
    }

    public String getMessage() {
      return message;
    }

    public String getMessageExpression() {
      return messageExpression;
    }

    public Boolean getOptionalOldSelf() {
      return optionalOldSelf;
    }

    public String getReason() {
      return reason;
    }

    public String getRule() {
      return rule;
    }

    static KubernetesValidationRule from(AnnotationRef annotationRef) {
      KubernetesValidationRule result = new KubernetesValidationRule();
      result.rule = (String) annotationRef.getParameters().get(VALUE);
      result.reason = mapNotEmpty((String) annotationRef.getParameters().get("reason"));
      result.message = mapNotEmpty((String) annotationRef.getParameters().get("message"));
      result.messageExpression = mapNotEmpty((String) annotationRef.getParameters().get("messageExpression"));
      result.fieldPath = mapNotEmpty((String) annotationRef.getParameters().get("fieldPath"));
      result.optionalOldSelf = Boolean.TRUE.equals(annotationRef.getParameters().get("optionalOldSelf")) ? Boolean.TRUE : null;
      return result;
    }

    static KubernetesValidationRule from(ValidationRule validationRule) {
      KubernetesValidationRule result = new KubernetesValidationRule();
      result.rule = validationRule.value();
      result.reason = mapNotEmpty(validationRule.reason());
      result.message = mapNotEmpty(validationRule.message());
      result.messageExpression = mapNotEmpty(validationRule.messageExpression());
      result.fieldPath = mapNotEmpty(validationRule.fieldPath());
      result.optionalOldSelf = validationRule.optionalOldSelf() ? true : null;
      return result;
    }

    private static String mapNotEmpty(String s) {
      if (s == null)
        return null;
      if (s.isEmpty())
        return null;
      return s;
    }
  }

  private boolean isPotentialAccessor(Method method) {
    final String name = method.getName();
    return name.startsWith("is") || name.startsWith("get") || name.startsWith("set");
  }

  /**
   * Retrieves the updated property name for the specified property if its annotations warrant it
   *
   * @param property the Property which name might need to be updated
   * @return the updated property name or its original one if it didn't need to be changed
   */
  private String extractUpdatedNameFromJacksonPropertyIfPresent(Property property) {
    final String name = property.getName();
    final boolean ignored = property.getAnnotations().stream()
        .anyMatch(a -> a.getClassRef().getFullyQualifiedName().equals(ANNOTATION_JSON_IGNORE));

    if (ignored) {
      return null;
    } else {
      return property.getAnnotations().stream()
          // only consider JsonProperty annotation
          .filter(a -> a.getClassRef().getFullyQualifiedName().equals(ANNOTATION_JSON_PROPERTY))
          .findAny()
          // if we found an annotated accessor, override the property's name if needed
          .map(a -> {
            final String fromAnnotation = (String) a.getParameters().get(VALUE);
            if (!Strings.isNullOrEmpty(fromAnnotation) && !name.equals(fromAnnotation)) {
              return fromAnnotation;
            } else {
              return name;
            }
          }).orElse(property.getName());
    }
  }

  /**
   * Creates a new specific builder object.
   *
   * @return a new builder object specific to the CRD generation version
   */
  public abstract B newBuilder();

  /**
   * Creates a new specific builder object.
   *
   * @param type the type to be used
   * @return a new builder object specific to the CRD generation version
   */
  public abstract B newBuilder(String type);

  /**
   * Adds the specified property to the specified builder, calling {@link #internalFrom(String, TypeRef)}
   * to create the property schema.
   *
   * @param property the property to add to the currently being built schema
   * @param builder the builder representing the schema being built
   * @param schema the built schema for the property being added
   */
  public abstract void addProperty(Property property, B builder, T schema, SchemaPropsOptions options);

  /**
   * Finishes up the process by actually building the final JSON schema based on the provided
   * builder and a potentially empty list of names of fields which should be marked as required
   *
   * @param builder the builder used to build the final schema
   * @param required the list of names of required fields
   * @param validationRules the list of validation rules
   * @param preserveUnknownFields whether preserveUnknownFields is enabled
   * @return the built JSON schema
   */
  public abstract T build(B builder,
      List<String> required,
      List<KubernetesValidationRule> validationRules,
      boolean preserveUnknownFields);

  /**
   * Builds the specific JSON schema representing the structural schema for the specified property
   *
   * @param name the name of the property which schema we want to build
   * @param typeRef the type of the property which schema we want to build
   * @return the structural schema associated with the specified property
   */
  public T internalFrom(String name, TypeRef typeRef) {
    return internalFromImpl(name, typeRef, new LinkedHashMap<>(), new InternalSchemaSwaps());
  }

  private T internalFromImpl(String name, TypeRef typeRef, LinkedHashMap<String, String> visited,
      InternalSchemaSwaps schemaSwaps) {
    // Note that ordering of the checks here is meaningful: we need to check for complex types last
    // in case some "complex" types are handled specifically
    if (typeRef.getDimensions() > 0 || io.sundr.model.utils.Collections.isCollection(typeRef)) { // Handle Collections & Arrays
      //noinspection unchecked
      final TypeRef collectionType = TypeAs.combine(TypeAs.UNWRAP_ARRAY_OF, TypeAs.UNWRAP_COLLECTION_OF)
          .apply(typeRef);
      final T schema = internalFromImpl(name, collectionType, visited, schemaSwaps);
      return arrayLikeProperty(schema);
    } else if (io.sundr.model.utils.Collections.IS_MAP.apply(typeRef)) { // Handle Maps
      final TypeRef keyType = TypeAs.UNWRAP_MAP_KEY_OF.apply(typeRef);

      if (!(keyType instanceof ClassRef && ((ClassRef) keyType).getFullyQualifiedName().equals("java.lang.String"))) {
        LOGGER.warn("Property '{}' with '{}' key type is mapped to 'string' because of CRD schemas limitations", name, typeRef);
      }

      final TypeRef valueType = TypeAs.UNWRAP_MAP_VALUE_OF.apply(typeRef);
      T schema = internalFromImpl(name, valueType, visited, schemaSwaps);
      if (schema == null) {
        LOGGER.warn(
            "Property '{}' with '{}' value type is mapped to 'object' because its CRD representation cannot be extracted.",
            name, typeRef);
        schema = internalFromImpl(name, OBJECT_REF, visited, schemaSwaps);
      }

      return mapLikeProperty(schema);
    } else if (io.sundr.model.utils.Optionals.isOptional(typeRef)) { // Handle Optionals
      return internalFromImpl(name, TypeAs.UNWRAP_OPTIONAL_OF.apply(typeRef), visited, schemaSwaps);
    } else {
      final String typeName = COMMON_MAPPINGS.get(typeRef);
      if (typeName != null) { // we have a type that we handle specifically
        if (INT_OR_STRING_MARKER.equals(typeName)) { // Handle int or string mapped types
          return mappedProperty(typeRef);
        } else {
          return singleProperty(typeName); // Handle Standard Types
        }
      } else {
        if (typeRef instanceof ClassRef) { // Handle complex types
          ClassRef classRef = (ClassRef) typeRef;
          TypeDef def = Types.typeDefFrom(classRef);

          // check if we're dealing with an enum
          if (def.isEnum()) {
            final JsonNode[] enumValues = def.getProperties().stream()
                .filter(Property::isEnumConstant)
                .map(this::extractUpdatedNameFromJacksonPropertyIfPresent)
                .filter(Objects::nonNull)
                .sorted()
                .map(JsonNodeFactory.instance::textNode)
                .toArray(JsonNode[]::new);
            return enumProperty(enumValues);
          } else if (!classRef.getFullyQualifiedName().equals(VOID.getName())) {
            return resolveNestedClass(name, def, visited, schemaSwaps);
          }

        }
        return null;
      }
    }
  }

  private T resolveNestedClass(String name, TypeDef def, LinkedHashMap<String, String> visited,
      InternalSchemaSwaps schemaSwaps) {
    String fullyQualifiedName = def.getFullyQualifiedName();
    T res = resolveJavaClass(fullyQualifiedName);
    if (res != null) {
      return res;
    }
    if (visited.put(fullyQualifiedName, name) != null) {
      throw new IllegalArgumentException(
          "Found a cyclic reference involving the field of type " + fullyQualifiedName + " starting a field "
              + visited.entrySet().stream().map(e -> e.getValue() + " >>\n" + e.getKey()).collect(Collectors.joining(".")) + "."
              + name);
    }

    res = internalFromImpl(def, visited, schemaSwaps);
    visited.remove(fullyQualifiedName);
    return res;
  }

  private T resolveJavaClass(String fullyQualifiedName) {
    if ((!fullyQualifiedName.startsWith("java.") && !fullyQualifiedName.startsWith("javax."))
        || COMPLEX_JAVA_TYPES.contains(fullyQualifiedName)) {
      return null;
    }
    String mapping = null;
    boolean array = false;
    try {
      Class<?> clazz = Class.forName(fullyQualifiedName);
      JsonSchema schema = GENERATOR.generateSchema(clazz);
      if (schema.isArraySchema()) {
        Items items = schema.asArraySchema().getItems();
        if (items.isSingleItems()) {
          array = true;
          schema = items.asSingleItems().getSchema();
        }
      }
      if (schema.isIntegerSchema()) {
        mapping = INTEGER_MARKER;
      } else if (schema.isNumberSchema()) {
        mapping = NUMBER_MARKER;
      } else if (schema.isBooleanSchema()) {
        mapping = BOOLEAN_MARKER;
      } else if (schema.isStringSchema()) {
        mapping = STRING_MARKER;
      }
    } catch (Exception e) {
      LOGGER.debug(
          "Something went wrong with detecting java type schema for {}, will use full introspection instead",
          fullyQualifiedName, e);
    }
    // cache the result for subsequent calls
    if (mapping != null) {
      if (array) {
        return arrayLikeProperty(singleProperty(mapping));
      }
      COMMON_MAPPINGS.put(TypeDef.forName(fullyQualifiedName).toReference(), mapping);
      return singleProperty(mapping);
    }

    COMPLEX_JAVA_TYPES.add(fullyQualifiedName);
    return null;
  }

  /**
   * Builds the schema for specifically handled property types (e.g. intOrString properties)
   *
   * @param ref the type of the specifically handled property
   * @return the property schema
   */
  protected abstract T mappedProperty(TypeRef ref);

  /**
   * Builds the schema for array-like properties
   *
   * @param schema the schema for the extracted element type for this array-like property
   * @return the schema for the array-like property
   */
  protected abstract T arrayLikeProperty(T schema);

  /**
   * Builds the schema for map-like properties
   *
   * @param schema the schema for the extracted element type for the values of this map-like property
   * @return the schema for the map-like property
   */
  protected abstract T mapLikeProperty(T schema);

  /**
   * Builds the schema for standard, simple (e.g. string) property types
   *
   * @param typeName the mapped name of the property type
   * @return the schema for the property
   */
  protected abstract T singleProperty(String typeName);

  protected abstract T enumProperty(JsonNode... enumValues);

  protected abstract T addDescription(T schema, String description);
}
