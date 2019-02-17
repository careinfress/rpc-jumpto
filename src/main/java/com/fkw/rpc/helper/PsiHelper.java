package com.fkw.rpc.helper;

import com.intellij.psi.PsiElement;

public class PsiHelper {

    private String cliKey;
    private PsiElement cliPsiElement;
    private PsiElement svrPsiElement;


    public PsiHelper(String cliKey, PsiElement cliPsiElement, PsiElement svrPsiElement) {
        this.cliKey =cliKey;
        this.cliPsiElement = cliPsiElement;
        this.svrPsiElement =svrPsiElement;
    }

    public PsiHelper() {}


    public void setCliPsiElement(PsiElement cliPsiElement) {
        this.cliPsiElement = cliPsiElement;
    }

    public void  setSvrPsiElement(PsiElement svrPsiElement) {
        this.svrPsiElement = svrPsiElement;
    }

    public PsiElement getCliPsiElement() {
        return cliPsiElement;
    }

    public PsiElement getSvrPsiElement() {
        return svrPsiElement;
    }

    public void setCliKey(String cliKey) {this.cliKey = cliKey;}

    public String getCliKey() {return cliKey;}


}
