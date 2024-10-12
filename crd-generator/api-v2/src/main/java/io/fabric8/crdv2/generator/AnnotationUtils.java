package io.fabric8.crdv2.generator;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AnnotationUtils {
  private AnnotationUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Walks up the class hierarchy to consume the repeating annotation
   */
  public static <A extends Annotation> void consumeRepeatingAnnotation(
      Class<?> beanClass,
      Class<A> annotation,
      Consumer<A> consumer) {

    while (beanClass != null && beanClass != Object.class) {
      Stream.of(beanClass.getAnnotationsByType(annotation)).forEach(consumer);
      beanClass = beanClass.getSuperclass();
    }
  }

  public static <A extends Annotation> List<A> findRepeatingAnnotations(Class<?> clazz, Class<A> annotation) {
    List<A> list = new LinkedList<>();
    consumeRepeatingAnnotation(clazz, annotation, list::add);
    return list;
  }
}
