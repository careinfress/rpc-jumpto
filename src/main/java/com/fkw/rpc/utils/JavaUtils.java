package com.fkw.rpc.utils;

import com.fkw.rpc.Annotation.Annotation;
import com.fkw.rpc.wrapper.Reference;
import com.fkw.rpc.wrapper.ReferenceCollection;
import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JavaUtils {

    public final static Map<String, PsiElement> cliToSvrCache = Collections.synchronizedMap(new LRUMap(200));
    public final static Map<PsiElement, PsiElement> svrToCliCache = Collections.synchronizedMap(new LRUMap(200));

    private JavaUtils() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static Optional<PsiClass> findClazz(@NotNull Project project, @NotNull String clazzName) {
        return Optional.fromNullable(JavaPsiFacade.getInstance(project).findClass(clazzName, GlobalSearchScope.allScope(project)));
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



    //====================下面的方法将引入到FaiUtils.java===========================

    public static void cliThreadAnalysis(String cliKey, ReferenceCollection references) {
        if (references == null || references.isEmpty()) {return ;}
        Iterator<Reference> iterator = references.iterator();
        while (iterator.hasNext()) {
            Reference reference = iterator.next();
            PsiMethod psiMethod = reference.containingMethod();
            PsiClass psiClass = reference.containingClass();
            if (psiClass == null || psiClass.getSuperClass() == null) continue;
            if (psiMethod != null && psiMethod.getName().equals("processThread")) {
                //老svr
                if (psiClass.getSuperClass().getName().equals("FaiServer")) {
                    processThreadMethodAnalysis(cliKey, reference);
                }
            } else {

                //新svr
                if (psiClass.getSuperClass().getName().equals("FaiHandler") || psiClass.getSuperClass().getName().equals("GenericProc")) {
                    cliToSvrCache.put(cliKey, reference.getPsiElement());
                }
            }
        }
        return ;
    }

    public static void processThreadMethodAnalysis(String cliKey, Reference reference) {
        if (cliToSvrCache.containsKey(cliKey)) {return;}
        if (reference == null) return;
        PsiMethod psiMethod = reference.containingMethod();
        if (psiMethod == null) return;
        String classQualifiedName = getProcClassQualifiedName(reference.containingClass());
        if (StringUtils.isEmpty(classQualifiedName)) return;
        PsiElement methodLastChild = psiMethod.getLastChild();
        PsiElement[] children = methodLastChild.getChildren();
        for (PsiElement child : children) {
            if (!(child instanceof PsiSwitchStatement)) continue; //走进swich代码块
            PsiElement swichElement = child.getLastChild();
            PsiElement[] elements = swichElement.getChildren();
            String caseValue = "";
            for (PsiElement element : elements) {
                if (element instanceof PsiSwitchLabelStatement) { //case代码块
                    PsiSwitchLabelStatement psiSwitchLabelStatement = (PsiSwitchLabelStatement) element;
                    if (psiSwitchLabelStatement.getCaseValue() != null) {
                        System.out.println(psiSwitchLabelStatement.getCaseValue().getText());
                        caseValue = psiSwitchLabelStatement.getCaseValue().getText();
                    }
                } else if (element instanceof PsiReturnStatement) {
                    PsiReturnStatement psiReturnStatement = (PsiReturnStatement) element;
                    PsiExpression returnValue = psiReturnStatement.getReturnValue();
                    String methodName = getOldProcMethodName(returnValue.getText());
                    Optional<PsiMethod> method = JavaUtils.findMethod(reference.getPsiElement().getProject(), classQualifiedName, methodName);
                    if (!method.isPresent()) continue;
                    cliToSvrCache.put(caseValue, method.get());
                }
            }
        }
    }

    public static String getProcClassQualifiedName(PsiClass psiClass) {
        String procClassQualifiedName = "";
        if (psiClass == null) return procClassQualifiedName;
        PsiField[] allFields = psiClass.getAllFields();
        for (PsiField psiField : allFields) {
            Optional<PsiClass> referenceClazzOfPsiField = JavaUtils.getReferenceClazzOfPsiField(psiField);
            if (!referenceClazzOfPsiField.isPresent()) continue;
            String qualifiedName = referenceClazzOfPsiField.get().getQualifiedName();
            if (!qualifiedName.startsWith("fai.svr.")) continue;
            procClassQualifiedName = qualifiedName;
        }
        return procClassQualifiedName;
    }

    public static String getOldProcMethodName(String returnVaule) {
        String methodName = "";
        if (StringUtils.isEmpty(returnVaule)) return methodName;
        String[] splits = returnVaule.split("\\.");
        if (splits.length <= 1) return methodName;
        String substring = splits[1].substring(0, splits[1].indexOf("("));
        if (StringUtils.isEmpty(substring)) return methodName;
        methodName = substring;
        return methodName;
    }




}
