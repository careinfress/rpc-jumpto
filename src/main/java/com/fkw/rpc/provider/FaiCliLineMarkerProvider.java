package com.fkw.rpc.provider;



import com.fkw.rpc.utils.Icons;
import com.fkw.rpc.utils.JavaUtils;
import com.google.common.base.Optional;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.collections.map.LRUMap;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FaiCliLineMarkerProvider extends RelatedItemLineMarkerProvider {


    private static Map<String, PsiElement> cliToSvrCache = Collections.synchronizedMap(new LRUMap(200));

    private final String FAI_CLI_PREFIX = "fai.cli";
    private final String FAI_CLI_EXPRESSION = "sendProtocol.setCmd";
    private final String FAI_SVR_PACKAGE_NAME_PREFIX = "fai.svr.";


    /**
     * 该方法的实现是从目标对象反向向上校验
     * 不能跟我们的逻辑上下手从范围大的扫描到小的
     *
     * @param element
     * @param result
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {


        if (!(element instanceof PsiMethodCallExpression)) return;
        PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;

//        System.out.println(psiMethodCallExpression.getMethodExpression().getQualifiedName());//sendProtocol.setCmd
//        System.out.println(psiMethodCallExpression.getText());//sendProtocol.setCmd(CdnDef.Protocol.Cmd.REFRESH_OBJECT_CACHES)


        if (psiMethodCallExpression.getMethodExpression().getQualifiedName().equals(FAI_CLI_EXPRESSION)) {

            PsiMethod psiMethod = psiMethodCallExpression.resolveMethod();
            if (psiMethod != null) {
                System.out.println(psiMethod.getName());
            }
            //判断包名是否为fai.cli
            PsiJavaFile parentOfType = PsiTreeUtil.getParentOfType(element, PsiJavaFile.class);
            if (!parentOfType.getPackageName().equals(FAI_CLI_PREFIX)) return;

            PsiElement nextSibling = psiMethodCallExpression.getFirstChild().getNextSibling();
            PsiElement tagPsi = nextSibling.getFirstChild().getNextSibling();
//            System.out.println(nextSibling.getText());//(CdnDef.Protocol.Cmd.REFRESH_OBJECT_CACHES)
//            System.out.println(tagPsi.getText());//CdnDef.Protocol.Cmd.REFRESH_OBJECT_CACHES

            //包含了跳转信息
            if (cliToSvrCache.containsKey(tagPsi.getText())) {

            } else {
                //缓存中没有包含,需要重新解析
                //格式校验
//                if (tagPsi.getFirstChild() == null) return;

                int nameIndex = tagPsi.getText().split("\\.")[0].indexOf("Def");//CdnDef.Protocol.Cmd.REFRESH_OBJECT_CACHES
                String faiSvrSimpleName = tagPsi.getFirstChild().getText().substring(0, nameIndex);//Cdn
                String faiSvrQualifiedName = FAI_SVR_PACKAGE_NAME_PREFIX + faiSvrSimpleName + "Svr." + faiSvrSimpleName + "Svr";
                //System.out.println("faiSvrQualifiedName="+faiSvrQualifiedName);//faiSvrQualifiedName=fai.svr.CdnSvr.CdnSvr

                Optional<PsiClass> clazz = JavaUtils.findClazz(element.getProject(), faiSvrQualifiedName);

                //缓存变量的别名以及PsiField的映射
                HashMap<String, PsiField> fieldNameMap = new HashMap<>();
                //获取全局变量
                PsiField[] fields = clazz.get().getAllFields();
                for (PsiField field : fields) {
                    String aliasName = field.getName();
                    fieldNameMap.put(aliasName, field);
                    Optional<PsiClass> referenceClazzOfPsiField = JavaUtils.getReferenceClazzOfPsiField(field);
                    System.out.println(referenceClazzOfPsiField.get().getQualifiedName());
                    /**
                     * fai.svr.CdnSvr.CdnProc
                     * java.util.concurrent.LinkedBlockingQueue
                     * fai.comm.util.CacheManager
                     * java.lang.Thread
                     * java.lang.String
                     * java.util.List
                     */
                }

                Optional<PsiMethod> method = JavaUtils.findMethod(element.getProject(), faiSvrQualifiedName, "processThread");
                if (!method.isPresent()) return;
                //解析processThread方法
                PsiElement lastChild = method.get().getLastChild();//方法中的最后一个代码块

                PsiElement[] psiElements = lastChild.getChildren();
                for (PsiElement psiElement : psiElements) {
                    if (!(psiElement instanceof PsiSwitchStatement)) continue; //走进swich代码块

                    PsiElement swichElement = psiElement.getLastChild();
                    String svrAliasName = "";
                    PsiElement[] elements = swichElement.getChildren();
                    for (PsiElement tagElement : elements) {
                        String caseValue = "";
                        if (tagElement instanceof PsiSwitchLabelStatement) { //case代码块
                            PsiSwitchLabelStatement psiSwitchLabelStatement = (PsiSwitchLabelStatement) tagElement;
                            if (psiSwitchLabelStatement.getCaseValue() != null) {
                                System.out.println(psiSwitchLabelStatement.getCaseValue().getText());
                                caseValue = psiSwitchLabelStatement.getCaseValue().getText();
                            }
                            //System.out.println(psiSwitchLabelStatement.getCaseValue().getText());//CdnDef.Protocol.Cmd.ADD_DOMAIN_BPS_DATA
                        } else if (tagElement instanceof PsiReturnStatement) { //return 代码块
                            PsiReturnStatement psiReturnStatement = (PsiReturnStatement) tagElement;
                            PsiExpression returnValue = psiReturnStatement.getReturnValue();
                            //System.out.println(returnValue.getText());//m_cdnProc.processAddDomainBpsData(recvProtocol, sendProtocol)

                            PsiElement[] psiElements1 = returnValue.getFirstChild().getChildren();
                            for (PsiElement valueChild : psiElements1) {
                                if ((valueChild instanceof PsiReferenceExpression)) {
                                    PsiReferenceExpression expression = (PsiReferenceExpression) valueChild;
                                    svrAliasName = expression.getQualifiedName();
                                } else if (valueChild instanceof PsiIdentifier) {
                                    PsiIdentifier psiIdentifier = (PsiIdentifier) valueChild;
                                    //System.out.println(psiIdentifier.getText());//processGetDomainBpsData
                                    PsiField psiField = fieldNameMap.get(svrAliasName);//拿到类
                                    String psiType = psiField.getType().toString();
                                    PsiClass[] classes = PsiShortNamesCache.getInstance(element.getProject()).getClassesByName(psiType.split("\\:")[1],
                                            GlobalSearchScope.allScope(element.getProject()));
                                    for (PsiClass aClass : classes) {
                                        //System.out.println(aClass.getQualifiedName());
                                        Optional<PsiMethod> method1 = JavaUtils.findMethod(element.getProject(), aClass.getQualifiedName(), psiIdentifier.getText());
                                        cliToSvrCache.put(caseValue, method1.get());
                                    }
                                }
                            }
                        }
                    }
                }
            }


            //切出svr用来识别路由表
            System.out.println(tagPsi.getText());
            System.out.println(cliToSvrCache.get(tagPsi.getText()));
            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(Icons.SPRING_INJECTION_ICON)
                            .setAlignment(GutterIconRenderer.Alignment.CENTER)
                            .setTarget(cliToSvrCache.get(tagPsi.getText()))
                            .setTooltipTitle("Data access object found - ");
            result.add(builder.createLineMarkerInfo(element));

        }

    }
}
