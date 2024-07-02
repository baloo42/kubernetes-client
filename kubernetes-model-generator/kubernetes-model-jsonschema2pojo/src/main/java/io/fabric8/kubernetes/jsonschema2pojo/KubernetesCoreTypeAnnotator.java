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
package io.fabric8.kubernetes.jsonschema2pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import io.fabric8.kubernetes.api.builder.Editable;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.fabric8.kubernetes.model.jackson.JsonUnwrappedDeserializer;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.transform.annotations.TemplateTransformation;
import io.sundr.transform.annotations.TemplateTransformations;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KubernetesCoreTypeAnnotator extends Jackson2Annotator {

  private static final String BUILDER_PACKAGE = "io.fabric8.kubernetes.api.builder";

  public static final String BUILDABLE_REFERENCE_VALUE = "value";

  protected static final String ANNOTATION_VALUE = "value";
  protected static final String API_VERSION = "apiVersion";
  protected static final String METADATA = "metadata";
  protected static final String KIND = "kind";
  protected static final String DEFAULT = "default";
  protected static final String INTERFACE_TYPE_PROPERTY = "interfaceType";
  private static final Set<String> IGNORED_CLASSES = ConcurrentHashMap.newKeySet();
  static {
    IGNORED_CLASSES.add("io.fabric8.kubernetes.api.model.KubeSchema");
    IGNORED_CLASSES.add("io.fabric8.kubernetes.api.model.ValidationSchema");
  }
  protected final Map<String, JDefinedClass> pendingResources = new HashMap<>();
  protected final Map<String, JDefinedClass> pendingLists = new HashMap<>();

  private final Set<String> handledClasses = new HashSet<>();

  public KubernetesCoreTypeAnnotator(GenerationConfig generationConfig) {
    super(generationConfig);
  }

  @Override
  public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
    // ensure every class is only processed once
    if (handledClasses.contains(clazz.fullName()) || IGNORED_CLASSES.contains(clazz.fullName())) {
      return;
    }
    handledClasses.add(clazz.fullName());

    final JAnnotationArrayMember jsonPropertyOrder = clazz.annotate(JsonPropertyOrder.class).paramArray(ANNOTATION_VALUE);
    final List<String> fieldNames = StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(propertiesNode.fieldNames(), Spliterator.ORDERED), false)
        .collect(Collectors.toList());
    final List<String> topFields = Arrays.asList(API_VERSION, KIND, METADATA);
    for (String topField : topFields) {
      if (fieldNames.contains(topField)) {
        jsonPropertyOrder.param(topField);
      }
    }
    fieldNames.stream().filter(f -> !topFields.contains(f)).forEach(jsonPropertyOrder::param);

    clazz.annotate(ToString.class);
    clazz.annotate(EqualsAndHashCode.class);
    clazz.annotate(Setter.class);
    clazz.annotate(Accessors.class).paramArray("prefix").param("_").param("");
    makeEditable(clazz);
    processBuildable(clazz);

    final Map<String, JFieldVar> fields = clazz.fields();
    if (fields.containsKey(KIND) && propertiesNode.has(API_VERSION) && propertiesNode.get(API_VERSION).has(DEFAULT)) {
      String apiVersion = propertiesNode.get(API_VERSION).get(DEFAULT).toString().replace('"', ' ').trim();
      String apiGroup = "";
      final int lastSlash = apiVersion.lastIndexOf('/');
      if (lastSlash > 0) {
        apiGroup = apiVersion.substring(0, lastSlash);
        apiVersion = apiVersion.substring(apiGroup.length() + 1);
      }

      JAnnotationArrayMember arrayMember = clazz.annotate(TemplateTransformations.class)
          .paramArray(ANNOTATION_VALUE);
      arrayMember.annotate(TemplateTransformation.class).param(ANNOTATION_VALUE, "/manifest.vm")
          .param("outputPath", "META-INF/services/io.fabric8.kubernetes.api.model.KubernetesResource").param("gather", true);

      String resourceName = clazz.fullName();
      if (resourceName.endsWith("List")) {
        resourceName = resourceName.substring(0, resourceName.length() - 4);
        final JDefinedClass resourceClass = pendingResources.remove(resourceName);
        if (resourceClass != null) {
          annotate(clazz, apiVersion, apiGroup);
        } else {
          pendingLists.put(resourceName, clazz);
        }
      } else {
        final JDefinedClass resourceListClass = pendingLists.remove(resourceName);
        if (resourceListClass != null) {
          annotate(resourceListClass, apiVersion, apiGroup);
        } else {
          annotate(clazz, apiVersion, apiGroup);
          pendingResources.put(resourceName, clazz);
        }
      }
    }
  }

  private void makeEditable(JDefinedClass clazz) {
    JClass builderType = clazz.owner().ref(clazz.fullName() + "Builder");
    JClass editableType = clazz.owner().ref(Editable.class).narrow(builderType);
    clazz._implements(editableType);
    JMethod editMethod = clazz.method(JMod.PUBLIC, builderType, "edit");
    editMethod.annotate(JsonIgnore.class);
    JInvocation newBuilder = JExpr._new(builderType).arg(JExpr._this());
    editMethod.body()._return(newBuilder);
    JMethod toBuilderMethod = clazz.method(JMod.PUBLIC, builderType, "toBuilder");
    toBuilderMethod.annotate(JsonIgnore.class);
    toBuilderMethod.body()._return(JExpr.invoke("edit"));
  }

  private void annotate(JDefinedClass clazz, String apiVersion, String apiGroup) {
    clazz.annotate(Version.class).param(ANNOTATION_VALUE, apiVersion);
    clazz.annotate(Group.class).param(ANNOTATION_VALUE, apiGroup);
  }

  @Override
  public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
    if (IGNORED_CLASSES.contains(clazz.fullName())) {
      return;
    }
    if (schema.has("serializer")) {
      annotateSerde(clazz, JsonSerialize.class, schema.get("serializer").asText());
    }

    String deserializer = null;
    if (schema.has("deserializer")) {
      deserializer = schema.get("deserializer").asText();
    }

    if (schema.has("properties") && hasInterfaceFields(schema.get("properties"))) {
      clazz.annotate(JsonDeserialize.class)
          .param("using", JsonUnwrappedDeserializer.class);
    } else {
      annotateSerde(clazz, JsonDeserialize.class,
          deserializer == null ? JsonDeserializer.None.class.getCanonicalName() : deserializer);
    }

    super.propertyInclusion(clazz, schema);
  }

  private void annotateSerde(JDefinedClass clazz, Class<? extends Annotation> annotation, String usingClassName) {
    if (!usingClassName.endsWith(".class")) {
      usingClassName = usingClassName + ".class";
    }

    clazz.annotate(annotation).param("using", literalExpression(usingClassName));
  }

  private JExpressionImpl literalExpression(String literal) {
    return new JExpressionImpl() {
      @Override
      public void generate(JFormatter f) {
        f.p(literal);
      }
    };
  }

  @Override
  public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
    if (IGNORED_CLASSES.contains(clazz.fullName())) {
      return;
    }
    super.propertyField(field, clazz, propertyName, propertyNode);

    // Include NON_EMPTY for jsonschema2pojo configured fields
    if (propertyNode.has("javaOmitEmpty") && propertyNode.get("javaOmitEmpty").asBoolean(false)) {
      field.annotate(JsonInclude.class).param(ANNOTATION_VALUE, JsonInclude.Include.NON_EMPTY);
    }
    // Include NON_EMPTY for Maps and Arrays always
    else if (field.type().fullName().startsWith(Map.class.getName())
        || field.type().fullName().startsWith(List.class.getName())) {
      field.annotate(JsonInclude.class).param(ANNOTATION_VALUE, JsonInclude.Include.NON_EMPTY);
    }

    // Annotate JsonUnwrapped for interfaces as they cannot be created when no implementations
    if (propertyNode.hasNonNull(INTERFACE_TYPE_PROPERTY)) {
      field.annotate(JsonUnwrapped.class);
    }
  }

  @Override
  public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
    super.propertyGetter(getter, clazz, propertyName);
    // https://github.com/fabric8io/kubernetes-client/issues/6085
    // https://github.com/quarkusio/quarkus/issues/39934
    final JFieldVar field = clazz.fields().get(propertyName);
    if (field != null) {
      for (JAnnotationUse annotation : field.annotations()) {
        if (annotation.getAnnotationClass().fullName().equals(JsonInclude.class.getName())) {
          final JAnnotationUse methodAnnotation = getter.annotate(JsonInclude.class);
          annotation.getAnnotationMembers()
              .forEach((key, value) -> {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final PrintWriter pw = new PrintWriter(new BufferedOutputStream(baos));
                value.generate(new JFormatter(pw));
                pw.flush();
                methodAnnotation.param(key, Enum.valueOf(JsonInclude.Include.class,
                    baos.toString().replace(JsonInclude.Include.class.getCanonicalName() + ".", "")));
              });
        }
        if (annotation.getAnnotationClass().fullName().equals(JsonUnwrapped.class.getName())) {
          getter.annotate(JsonUnwrapped.class);
        }
      }
    }
  }

  protected void processBuildable(JDefinedClass clazz) {
    JAnnotationUse buildable = clazz.annotate(Buildable.class)
        .param("editableEnabled", false)
        .param("validationEnabled", false)
        .param("generateBuilderPackage", generateBuilderPackage())
        .param("lazyCollectionInitEnabled", false)
        .param("builderPackage", BUILDER_PACKAGE);

    List<String> types = new ArrayList<>();
    addBuildableTypes(clazz, types);
    if (!types.isEmpty()) {
      JAnnotationArrayMember arrayMember = buildable.paramArray("refs");
      types.forEach(s -> {
        try {
          arrayMember.annotate(BuildableReference.class).param(BUILDABLE_REFERENCE_VALUE,
              new JCodeModel()._class(s));
        } catch (JClassAlreadyExistsException e) {
          e.printStackTrace();
        }
      });
    }
  }

  protected boolean generateBuilderPackage() {
    return false;
  }

  protected void addBuildableTypes(JDefinedClass clazz, List<String> types) {

  }

  private boolean hasInterfaceFields(JsonNode propertiesNode) {
    for (Iterator<JsonNode> field = propertiesNode.elements(); field.hasNext();) {
      JsonNode propertyNode = field.next();
      if (propertyNode.hasNonNull(INTERFACE_TYPE_PROPERTY)) {
        return true;
      }
    }

    return false;
  }

}
