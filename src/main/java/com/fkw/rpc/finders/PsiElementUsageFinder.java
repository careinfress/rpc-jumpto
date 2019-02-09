package com.fkw.rpc.finders;


import com.fkw.rpc.wrapper.ReferenceCollection;

public interface PsiElementUsageFinder {
    ReferenceCollection findUsages();

    PsiElementUsageFinder setNext(PsiElementUsageFinder nextFinder);
}
