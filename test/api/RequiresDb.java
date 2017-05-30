package api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;



@org.scalatest.TagAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresDb {}