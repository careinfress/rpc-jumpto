package com.fkw.rpc.utils;

import com.fkw.rpc.Annotation.Annotation;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaUtils {

//    public final static Map<String, PsiElement> cliToSvrCache = new ConcurrentHashMap<String, PsiElement>();

    private JavaUtils() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static Optional<PsiClass> findClazz(@NotNull Project project, @NotNull String clazzName) {
        return Optional.fromNullable(JavaPsiFacade.getInstance(project).findClass(clazzName, GlobalSearchScope.allScope(project)));
    }

    @NotNull
    public static Optional<PsiField> findJavaField(@NotNull Project project, @Nullable String clazzName, @Nullable String fieldName) {
        if (StringUtils.isBlank(clazzName) && StringUtils.isBlank(fieldName)) {
            return Optional.absent();
        }
        Optional<PsiClass> clazz = findClazz(project, clazzName);
        if (clazz.isPresent()) {
            PsiField[] fields = clazz.get().getFields();
            for (PsiField field : fields) {
                if (!(field instanceof PsiNamedElement)) continue;
                PsiNamedElement psiNamedElement = (PsiNamedElement) field;
                if (psiNamedElement.getName().equals(fieldName)) return Optional.of(field);
            }
        }
        return Optional.absent();
    }

    @NotNull
    public static Optional<PsiMethod> findMethod(@NotNull Project project, @Nullable String clazzName, @Nullable String methodName) {
        if (StringUtils.isBlank(clazzName) && StringUtils.isBlank(methodName)) {
            return Optional.absent();
        }
        Optional<PsiClass> clazz = findClazz(project, clazzName);
        if (clazz.isPresent()) {
            PsiMethod[] methods = clazz.get().findMethodsByName(methodName, true);
            return ArrayUtils.isEmpty(methods) ? Optional.<PsiMethod>absent() : Optional.of(methods[0]);
        }
        return Optional.absent();
    }

    public static boolean isAnnotationPresent(@NotNull PsiModifierListOwner target, @NotNull Annotation annotation) {
        PsiModifierList modifierList = target.getModifierList();
        return null != modifierList && null != modifierList.findAnnotation(annotation.getQualifiedName());
    }

    @NotNull
    public static Optional<PsiAnnotation> getPsiAnnotation(@NotNull PsiModifierListOwner target, @NotNull Annotation annotation) {
        PsiModifierList modifierList = target.getModifierList();
        return null == modifierList ? Optional.<PsiAnnotation>absent() : Optional.fromNullable(modifierList.findAnnotation(annotation.getQualifiedName()));
    }

    @NotNull
    public static Optional<PsiAnnotationMemberValue> getAnnotationAttributeValue(@NotNull PsiModifierListOwner target,
                                                                                 @NotNull Annotation annotation,
                                                                                 @NotNull String attrName) {
        if (!isAnnotationPresent(target, annotation)) {
            return Optional.absent();
        }
        Optional<PsiAnnotation> psiAnnotation = getPsiAnnotation(target, annotation);
        return psiAnnotation.isPresent() ? Optional.fromNullable(psiAnnotation.get().findAttributeValue(attrName)) : Optional.<PsiAnnotationMemberValue>absent();
    }
    @NotNull
    public static Optional<PsiAnnotationMemberValue> getAnnotationValue(@NotNull PsiModifierListOwner target, @NotNull Annotation annotation) {
        return getAnnotationAttributeValue(target, annotation, "value");
    }

    public static Optional<String> getAnnotationValueText(@NotNull PsiModifierListOwner target, @NotNull Annotation annotation) {
        Optional<PsiAnnotationMemberValue> annotationValue = getAnnotationValue(target, annotation);
        return annotationValue.isPresent() ? Optional.of(annotationValue.get().getText().replaceAll("\"", "")) : Optional.<String>absent();
    }

    public static Optional<PsiClass> getReferenceClazzOfPsiField(@NotNull PsiElement field) {
        if (!(field instanceof PsiField)) {
            return Optional.absent();
        }
        PsiType type = ((PsiField) field).getType();
        return type instanceof PsiClassReferenceType ? Optional.fromNullable(((PsiClassReferenceType) type).resolve()) : Optional.<PsiClass>absent();
    }

}
