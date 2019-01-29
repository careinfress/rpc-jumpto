package com.fkw.rpc.provider;


import com.fkw.rpc.helper.SvrMapperHelper;
import com.fkw.rpc.utils.Icons;
import com.fkw.rpc.utils.JavaUtils;
import com.fkw.rpc.wrapper.Reference;
import com.fkw.rpc.wrapper.ReferenceCollection;
import com.google.common.base.Optional;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.apache.commons.collections.map.LRUMap;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FaiCliLineMarkerProvider extends RelatedItemLineMarkerProvider {


    private static Map<String, SvrMapperHelper> cliToSvrCache = Collections.synchronizedMap(new LRUMap(200));

    private final String FAI_CLI_PREFIX = "fai.cli";
    private final String FAI_CLI_EXPRESSION = "sendProtocol.setCmd";
    private final String FAI_CLI_JAVA_SUFFIX = "Cli.java";


    private final String FAI_SVR_PACKAGE_NAME_PREFIX = "fai.svr.";




    /**
     * 该方法的实现是从目标对象反向向上校验
     * 不能跟我们的逻辑上下手从范围大的扫描到小的
     * @param element
     * @param result
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {


        //判断是否为Psi表达式代码块
        if (!(element instanceof PsiReferenceExpression)) return;
        PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) element;
        if (psiReferenceExpression.getCanonicalText().equals(FAI_CLI_EXPRESSION)) {
            //System.out.println(psiReferenceExpression.getContainingFile().getName());//CdnCli.java
            //如果不是XXXCli.java的文件剔除
            if (!psiReferenceExpression.getContainingFile().getName().endsWith(FAI_CLI_JAVA_SUFFIX)) return;

            PsiElement[] children = psiReferenceExpression.getNextSibling().getChildren();
            //children[1].getText();              //AcctOptDef.Protocol.Cmd.GET_ACCTOPT_DATA
            //psiReferenceExpression.getContext();//sendProtocol.setCmd(AcctOptDef.Protocol.Cmd.GET_ACCTOPT_DATA);

            //包含了跳转信息
            if (cliToSvrCache.containsKey(children[1].getText())) {

            } else {
                //缓存中没有包含,需要重新解析
                //格式校验
                if (children[1].getFirstChild() == null) return;

                int nameIndex = children[1].getFirstChild().getText().split("\\.")[0].indexOf("Def");//CdnDef.Protocol.Cmd
                String faiSvrSimpleName = children[1].getFirstChild().getText().substring(0, nameIndex);//Cdn
                String faiSvrQualifiedName = FAI_SVR_PACKAGE_NAME_PREFIX + faiSvrSimpleName + "Svr." + faiSvrSimpleName + "Svr";
                //System.out.println("faiSvrQualifiedName="+faiSvrQualifiedName);//faiSvrQualifiedName=fai.svr.CdnSvr.CdnSvr

                Optional<PsiClass> clazz = JavaUtils.findClazz(element.getProject(), faiSvrQualifiedName);

                Optional<PsiMethod> method = JavaUtils.findMethod(element.getProject(), faiSvrQualifiedName, "processThread");
                if (!method.isPresent()) return;
                System.out.println(method.get().getBody().getText());

                System.out.println("=====================");


                HashMap<String, String> svrMap = new HashMap<>();
                //获取全局变量
                PsiField[] fields = clazz.get().getAllFields();
                for (PsiField field : fields) {
                    String psiType = field.getType().toString();
                    svrMap.put(field.getName(), psiType.split("\\:")[1]);
                }
            }


            //切出svr用来识别路由表


//            NavigationGutterIconBuilder<PsiElement> builder =
//                    NavigationGutterIconBuilder.create(Icons.SPRING_INJECTION_ICON)
//                            .setAlignment(GutterIconRenderer.Alignment.CENTER)
//                            .setTarget(element)
//                            .setTooltipTitle("Data access object found - ");
//            result.add(builder.createLineMarkerInfo(element));

        }


    }

}
