package com.fkw.rpc.provider;

import com.fkw.rpc.Annotation.Annotation;
import com.fkw.rpc.utils.JavaUtils;
import com.google.common.base.Optional;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FaiSvrLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        if (!(element instanceof PsiNamedElement)) return;
        PsiNamedElement psiNamedElement = (PsiNamedElement)element;
        System.out.println(psiNamedElement.getName());
        Collection<PsiReference> references = ReferencesSearch.search(element).findAll();
        System.out.println(psiNamedElement.getName() + "：引用的数量为：" + references.size());
        for (PsiReference reference : references) {
            PsiElement psiElement = reference.getElement();
            PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
            PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
            if (psiMethod != null) {System.out.println(psiNamedElement.getName() + ":引用的方法名为:" + psiMethod.getName());}
            if  (psiClass != null) {System.out.println(psiNamedElement.getName() + ":引用的类名为:" + psiClass.getQualifiedName());}
        }
        super.collectNavigationMarkers(element, result);
    }
}
