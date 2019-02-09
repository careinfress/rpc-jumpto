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

        /*if (!(element instanceof PsiField)) return;
        PsiClass parentOfType = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (parentOfType.getName().endsWith("Cmd"))  {
            PsiField psiField = (PsiField)element;
            PsiType type = psiField.getInitializer().getType();
            System.out.println(type.getCanonicalText());
        }*/

        if (!(element instanceof PsiNamedElement)) return;
        PsiNamedElement psiNamedElement = (PsiNamedElement)element;
        System.out.println(psiNamedElement.getName());

        Optional<PsiClass> clazz = JavaUtils.findClazz(element.getProject(), "");



        ReferencesSearch.SearchParameters searchParameters = new ReferencesSearch.SearchParameters(element, element.getUseScope(), true);
        Collection<PsiReference> all = ReferencesSearch.search(searchParameters).findAll();
        for (PsiReference psiReference : all) {
            System.out.println(psiReference.getCanonicalText());
        }


        System.out.println("====================================");


        super.collectNavigationMarkers(element, result);
    }


    private boolean isTargetField(PsiField field) {
        if (JavaUtils.isAnnotationPresent(field, Annotation.JNETKIT_CMD) || JavaUtils.isAnnotationPresent(field, Annotation.NETKIT_CMD)) {
            return true;
        }
        return false;
    }



}
