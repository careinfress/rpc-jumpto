package com.fkw.rpc.finders;

import com.fkw.rpc.wrapper.ReferenceCollection;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;


public abstract class AbstractUsageFinder implements PsiElementUsageFinder {
    PsiElement psiElement;
    SearchScope searchScope;
    private PsiElementUsageFinder nextFinder = NULL;

    static final PsiElementUsageFinder NULL = new PsiElementUsageFinder() {
        public ReferenceCollection findUsages() {
            return new ReferenceCollection(ReferenceCollection.EMPTY);
        }

        public PsiElementUsageFinder setNext(PsiElementUsageFinder nextFinder) {
            return this;
        }
    };

    public AbstractUsageFinder(PsiElement psiElement) {
        this.psiElement = psiElement;
        this.searchScope = psiElement.getUseScope();
    }

    public PsiElementUsageFinder setNext(PsiElementUsageFinder nextFinder) {
        return this.nextFinder = nextFinder;
    }

    protected abstract ReferenceCollection findCurrentElementUsages();

    public final ReferenceCollection findUsages() {
        ReferenceCollection usages = findCurrentElementUsages();
        usages.addAll(nextFinder.findUsages());
        return usages;
    }
}
