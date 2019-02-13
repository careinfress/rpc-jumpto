package com.fkw.rpc.provider;

import com.fkw.rpc.Annotation.Annotation;
import com.fkw.rpc.finders.PsiElementUsageFinderFactory;
import com.fkw.rpc.utils.JavaUtils;
import com.fkw.rpc.wrapper.Reference;
import com.fkw.rpc.wrapper.ReferenceCollection;
import com.google.common.base.Optional;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AnnotationJNetkitLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        if (!(element instanceof PsiModifierListOwner)) return;
        if (!isTargetField(element)) return;

        PsiModifierListOwner psiModifierListOwner = (PsiModifierListOwner) element;
        Optional<String> valueText = JavaUtils.getAnnotationValueText(psiModifierListOwner, Annotation.JNETKIT_CMD);
        if (!valueText.isPresent()) return;
        System.out.println(valueText.get());//DnsDef.Protocol.Cmd.GET_ADDR_LIST
        String cliKey = valueText.get();
        String classQualifiedName = cliKey.substring(0, cliKey.lastIndexOf("\\."));
        String fieldName = valueText.get().substring(cliKey.lastIndexOf("\\.") + 1);
        Optional<PsiField> javaField = JavaUtils.findJavaField(element.getProject(), classQualifiedName, fieldName);
        if (!javaField.isPresent()) return;

        ReferenceCollection references = ReferenceCollection.EMPTY;
        references.addAll(PsiElementUsageFinderFactory.getUsageFinder(javaField.get()).findUsages());
        for (Reference reference : references) {
            System.out.println(reference.containingMethod().getName());
            System.out.println(reference.containingClass().getQualifiedName());
        }
        System.out.println("===============");



        /*if (!(element instanceof PsiField)) return;
        PsiField field = (PsiField) element;
        if (!JavaUtils.isAnnotationPresent(field, Annotation.JNETKIT_CMD)) return;

        Optional<String> valueText = JavaUtils.getAnnotationValueText(field, Annotation.JNETKIT_CMD);
        if (!valueText.isPresent()) return;
        System.out.println(valueText.get());*/
        super.collectNavigationMarkers(element, result);
    }

    private boolean isTargetField(PsiElement psiElement) {
        boolean flag = false;
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
        if (psiClass == null) return flag;
        PsiClass superClass = psiClass.getSuperClass();
        if (superClass == null) return flag;
        if (StringUtils.isEmpty(superClass.getName()) || !superClass.getName().equals("FaiHandler")) return flag;
        flag = true;
        return flag;
    }



}
