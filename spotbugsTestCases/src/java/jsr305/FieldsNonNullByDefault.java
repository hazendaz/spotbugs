package jsr305;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jspecify.annotations.NonNull;

import javax.annotation.meta.TypeQualifierDefault;
import javax.annotation.meta.When;

@NonNull
@TypeQualifierDefault(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsNonNullByDefault {

}
