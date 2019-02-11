package com.fkw.rpc.finders;

import com.fkw.rpc.wrapper.ReferenceCollection;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ReferencesSearch;

public class DefaultUsageFinder extends AbstractUsageFinder {

    public DefaultUsageFinder(PsiElement psiElement) {
        super(psiElement);
    }

    protected ReferenceCollection findCurrentElementUsages() {
        return new ReferenceCollection(ReferencesSearch.search(this.psiElement).findAll());
    }

}
