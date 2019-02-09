package com.fkw.rpc.finders;

import com.fkw.rpc.wrapper.ReferenceCollection;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.searches.MethodReferencesSearch;


public class MethodUsageFinder extends AbstractUsageFinder {

    public MethodUsageFinder(PsiElement psiElement) { super(psiElement); }

    private boolean isInterfaceMethod(PsiMethod psiMethod) {
        return psiMethod.getContainingClass().isInterface();
    }

    protected ReferenceCollection findCurrentElementUsages() {
        MethodReferencesSearch.SearchParameters searchParameters = new MethodReferencesSearch.SearchParameters((PsiMethod) this.psiElement, searchScope, false);
        return new ReferenceCollection(MethodReferencesSearch.INSTANCE.createQuery(searchParameters).findAll());
    }
}
