package com.fkw.rpc.Annotation;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Annotation implements Cloneable {

    public static final Annotation JNETKIT_CMD = new Annotation("@Cmd", "fai.comm.jnetkit.server.fai.annotation.Cmd");

    public static final Annotation NETKIT_CMD = new Annotation("@Cmd", "fai.comm.netkit.Cmd");

    private final String label;

    private final String qualifiedName;

    private Map<String, AnnotationValue> attributePairs;

    @NotNull
    public String getLabel() {
        return label;
    }
    @NotNull
    public String getQualifiedName() {
        return qualifiedName;
    }

    public interface AnnotationValue {

    }

    public static class StringValue implements AnnotationValue {

        private String value;

        public StringValue(@NotNull String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "\"" + value + "\"";
        }

    }

    public Annotation(@NotNull String label, @NotNull String qualifiedName) {
        this.label = label;
        this.qualifiedName = qualifiedName;
        attributePairs = Maps.newHashMap();
    }

    private Annotation addAttribute(String key, AnnotationValue value) {
        this.attributePairs.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(label);
        if (!Iterables.isEmpty(attributePairs.entrySet())) {
            builder.append(setupAttributeText());
        }
        return builder.toString();
    }

    private String setupAttributeText() {
        Optional<String> singleValue = getSingleValue();
        return singleValue.isPresent() ? singleValue.get() : getComplexValue();
    }

    private Optional<String> getSingleValue() {
        try {
            String value = Iterables.getOnlyElement(attributePairs.keySet());
            StringBuilder builder = new StringBuilder("(");
            builder.append(attributePairs.get(value).toString());
            builder.append(")");
            return Optional.of(builder.toString());
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    private String getComplexValue() {
        StringBuilder builder = new StringBuilder("(");
        for (String key : attributePairs.keySet()) {
            builder.append(key);
            builder.append(" = ");
            builder.append(attributePairs.get(key).toString());
            builder.append(", ");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        return builder.toString();
    }

    @NotNull
    public Optional<PsiClass> toPsiClass(@NotNull Project project) {
        return Optional.fromNullable(JavaPsiFacade.getInstance(project).findClass(getQualifiedName(), GlobalSearchScope.allScope(project)));
    }


    @Override
    protected Annotation clone() {
        try {
            return (Annotation) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }
}
