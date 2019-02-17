package com.fkw.rpc.utils;

import com.fkw.rpc.helper.PsiHelper;
import com.fkw.rpc.wrapper.Reference;
import com.fkw.rpc.wrapper.ReferenceCollection;
import com.google.common.base.Optional;
import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;

public class FaiUtils {

    private FaiUtils() {
        throw new UnsupportedOperationException();
    }

    public static void cliThreadAnalysis(String cliKey, ReferenceCollection references, PsiElement psiElement) {
        if (references == null || references.isEmpty()) {return;}
        Iterator<Reference> iterator = references.iterator();
        while (iterator.hasNext()) {
            Reference reference = iterator.next();
            PsiClass psiClass = reference.containingClass();
            PsiMethod psiMethod = reference.containingMethod();
            if (psiClass == null || psiMethod == null) continue;
            PsiReferenceList implementsList = psiClass.getImplementsList();

            if (psiClass.getSuperClass() != null) {
                //老svr或者新svr用注解的方式
                if (psiMethod.getName().equals("processThread") && psiClass.getSuperClass().getName().equals("FaiServer")) {
                    processThreadMethodAnalysisNew(reference, cliKey, psiElement);
                    //processThreadMethodAnalysis(reference);
                } else if (psiClass.getSuperClass().getName().equals("FaiHandler")) {
                    PsiHelper psiHelper = new PsiHelper(cliKey, psiElement, reference.getPsiElement());
                    Constant.cliToSvrCache.put(cliKey, psiHelper);
                    Constant.svrToCliCache.put(reference.getPsiElement(), psiHelper);
//                    cliToSvrCache.put(cliKey, reference.getPsiElement());
                }
            }
            //老svr用注解的方式
            if (implementsList != null) {
                PsiClassType[] referencedTypes = implementsList.getReferencedTypes();
                for (PsiClassType referencedType : referencedTypes) {
                    if (referencedType.getClassName().equals("GenericProc")) {
                        PsiHelper psiHelper = new PsiHelper(cliKey, psiElement, reference.getPsiElement());
                        Constant.cliToSvrCache.put(cliKey, psiHelper);
                        Constant.svrToCliCache.put(reference.getPsiElement(), psiHelper);
//                        cliToSvrCache.put(cliKey, reference.getPsiElement());
                    }
                }
            }
        }
    }

    /*private static void processThreadMethodAnalysis(Reference reference) {
        if (reference == null) return;
        PsiMethod psiMethod = reference.containingMethod();
        if (psiMethod == null) return;
        PsiElement methodLastChild = psiMethod.getLastChild();
        PsiElement[] children = methodLastChild.getChildren();
        for (PsiElement child : children) {
            if (!(child instanceof PsiSwitchStatement)) continue; //走进swich代码块
            PsiElement swichElement = child.getLastChild();
            PsiElement[] elements = swichElement.getChildren();
            String caseValue = "";
            for (PsiElement element : elements) {
                if (element instanceof PsiSwitchLabelStatement) { //case代码块
                    PsiSwitchLabelStatement psiSwitchLabelStatement = (PsiSwitchLabelStatement) element;
                    if (psiSwitchLabelStatement.getCaseValue() != null) {
                        caseValue = psiSwitchLabelStatement.getCaseValue().getText();
                    }
                } else if (element instanceof PsiReturnStatement) {
                    PsiReturnStatement psiReturnStatement = (PsiReturnStatement) element;
                    PsiExpression returnValue = psiReturnStatement.getReturnValue();
                    String procAliasName = getProcAliasName(returnValue.getText());
                    String methodName = getOldProcMethodName(returnValue.getText());
                    if (StringUtils.isEmpty(procAliasName) || StringUtils.isEmpty(methodName)) continue;
                    if (!procNameMapperCache.containsKey(procAliasName)) procClassQualifiedName(reference.containingClass());
                    String classQualifiedName = procNameMapperCache.get(procAliasName);
                    Optional<PsiMethod> method = JavaUtils.findMethod(reference.getPsiElement().getProject(), classQualifiedName, methodName);
                    if (!method.isPresent()) continue;
                    cliToSvrCache.put(caseValue, method.get().getNameIdentifier());
                }
            }
        }
    }*/


    private static void processThreadMethodAnalysisNew(Reference reference, String cliKey, PsiElement psiElement) {
        if (reference == null) return;
        PsiMethod psiMethod = reference.containingMethod();
        if (psiMethod == null) return;
        PsiElement methodLastChild = psiMethod.getLastChild();
        PsiElement[] children = methodLastChild.getChildren();
        for (PsiElement child : children) {
            if (!(child instanceof PsiSwitchStatement)) continue;
            PsiElement swichElement = child.getLastChild();
            PsiElement[] elements = swichElement.getChildren();
            //走进swich的代码块中
            for (PsiElement element : elements) {
                if (!(element instanceof PsiSwitchLabelStatement)) continue;//case代码块
                PsiSwitchLabelStatement psiSwitchLabelStatement = (PsiSwitchLabelStatement) element;
                if (psiSwitchLabelStatement.getCaseValue() == null) continue;
                String caseValue = psiSwitchLabelStatement.getCaseValue().getText();//AcctOptDef.Protocol.Cmd.ADD
                if (!caseValue.equals(cliKey)) continue;
                PsiElement nextSibling = element.getNextSibling().getNextSibling();//return m_acctOptProc.processAddInfo(recvProtocol, sendProtocol);
                if (nextSibling == null ||  (!(nextSibling instanceof PsiReturnStatement))) continue;
                PsiReturnStatement psiReturnStatement = (PsiReturnStatement) nextSibling;
                PsiExpression returnValue = psiReturnStatement.getReturnValue();
                String procAliasName = getProcAliasName(returnValue.getText());
                String methodName = getOldProcMethodName(returnValue.getText());
                if (StringUtils.isEmpty(procAliasName) || StringUtils.isEmpty(methodName)) continue;
                if (!Constant.procNameMapperCache.containsKey(procAliasName)) procClassQualifiedName(reference.containingClass());
                String classQualifiedName = Constant.procNameMapperCache.get(procAliasName);
                Optional<PsiMethod> method = JavaUtils.findMethod(reference.getPsiElement().getProject(), classQualifiedName, methodName);
                if (!method.isPresent()) continue;
                PsiHelper psiHelper = new PsiHelper(cliKey, psiElement, method.get().getNameIdentifier());
                Constant.cliToSvrCache.put(cliKey, psiHelper);
                Constant.svrToCliCache.put(method.get().getNameIdentifier(), psiHelper);
//                cliToSvrCache.put(caseValue, method.get().getNameIdentifier());
            }
        }
    }

    private static void procClassQualifiedName(PsiClass psiClass) {
        if (psiClass == null) return;
        PsiField[] allFields = psiClass.getFields();
        for (PsiField psiField : allFields) {
            Optional<PsiClass> referenceClazzOfPsiField = JavaUtils.getReferenceClazzOfPsiField(psiField);
            if (!referenceClazzOfPsiField.isPresent()) continue;
            String qualifiedName = referenceClazzOfPsiField.get().getQualifiedName();
            if (StringUtils.isEmpty(qualifiedName) || !qualifiedName.startsWith(Constant.FAI_SVR_PACKAGE_NAME_PREFIX)) continue;
            Constant.procNameMapperCache.put(psiField.getName(), qualifiedName);
        }
    }


    /**
     * 功能：获取Svr中ProcessThread方法swich代码块return的方法名
     *
     * @see <a href="https://github.com/careinfress/rpc-jumpto/issues/1">20190212提交的issue</a>
     * @param returnVaule m_cdnProc.processRefreshObjectCaches(recvProtocol, sendProtocol)
     * @return processRefreshObjectCaches
     */
    private static String getOldProcMethodName(String returnVaule) {
        String methodName = "";
        if (StringUtils.isEmpty(returnVaule)) return methodName;
        String[] splits = returnVaule.split("\\.");
        if (splits.length != 2) return methodName;
        if (!splits[1].endsWith(")")) {
            return methodName;
        }
        String substring = splits[1].substring(0, splits[1].indexOf("("));
        if (StringUtils.isEmpty(substring)) return methodName;
        methodName = substring;
        return methodName;
    }

    /**
     * 功能：获取Svr中ProcessThread方法swich代码块return的类的别名(Proc的别名)
     *
     * @see <a href="https://github.com/careinfress/rpc-jumpto/issues/1">20190212提交的issue</a>
     * @param returnVaule  m_cdnProc.processRefreshObjectCaches(recvProtocol, sendProtocol)
     * @return  m_cdnProc
     */
    private static String getProcAliasName(String returnVaule) {
        String aliasName = "";
        if (StringUtils.isEmpty(returnVaule)) return aliasName;
        String[] splits = returnVaule.split("\\.");
        if (splits.length != 2) return aliasName;
        if (!splits[1].endsWith(")")) {
            return aliasName;
        }
        aliasName = splits[0];
        return aliasName;
    }

    /**
     * 功能：获取变量声明地址的类的全限命名
     *
     * @param material DnsDef.Protocol.Cmd.GET_ADDR_LIST
     * @return  fai.app.DnsDef.Protocol.Cmd
     */
    public static String getAppDefClassQualifiedName (String material) {
        String classQualifiedName = "";
        if (StringUtils.isEmpty(material)) {return classQualifiedName;}
        int index = material.lastIndexOf(".");
        if (index <= 0) return classQualifiedName;
        classQualifiedName = Constant.FAI_APP_PACKAGE_NAME_PREFIX + material.substring(0, index);
        return classQualifiedName;
    }

    /**
     * 功能：获取变量声明命名
     *
     * @param material DnsDef.Protocol.Cmd.GET_ADDR_LIST
     * @return  GET_ADDR_LIST
     */
    public static String getAppDefFieldName(String material) {
        String fieldName = "";
        if (StringUtils.isEmpty(material)) {return fieldName;}
        int index = material.lastIndexOf(".");
        if (index <= 0) return fieldName;
        fieldName = material.substring(index + 1);
        return fieldName;
    }


}
