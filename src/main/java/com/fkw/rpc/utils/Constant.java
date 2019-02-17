package com.fkw.rpc.utils;

import com.fkw.rpc.helper.PsiHelper;
import com.intellij.psi.PsiElement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constant {

    public final static Map<PsiElement, PsiHelper> svrToCliCache = new ConcurrentHashMap<PsiElement, PsiHelper>();
    public final static Map<String, PsiHelper> cliToSvrCache = new ConcurrentHashMap<String, PsiHelper>();


    public Constant() { throw new UnsupportedOperationException(); }

    public static final String FAI_CLI_PREFIX = "fai.cli";

    public static final String FAI_CLI_TAGR_METHOD_EXPRESSION = "sendProtocol.setCmd";

    public static final String FAI_SVR_PACKAGE_NAME_PREFIX = "fai.svr.";

    public static final String FAI_APP_PACKAGE_NAME_PREFIX = "fai.app.";


}
