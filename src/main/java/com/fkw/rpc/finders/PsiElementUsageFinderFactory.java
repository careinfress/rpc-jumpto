package com.fkw.rpc.finders;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;

public class PsiElementUsageFinderFactory {
    public static PsiElementUsageFinder getUsageFinder(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod)
            return new MethodUsageFinder(psiElement);

        if (psiElement instanceof PsiNamedElement)
            return new DefaultUsageFinder(psiElement);

        return AbstractUsageFinder.NULL;
    }
}
