package com.fkw.rpc.provider;


import com.fkw.rpc.utils.Icons;
import com.fkw.rpc.wrapper.Reference;
import com.fkw.rpc.wrapper.ReferenceCollection;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public class FaiCliLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private final String FAI_CLI_PREFIX = "fai.cli";
    private final String FAI_CLI_EXPRESSION = "sendProtocol.setCmd";


    /**
     * 该方法的实现是从目标对象反向向上校验
     * 不能跟我们的逻辑上下手从范围大的扫描到小的
     * @param element
     * @param result
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {


        //判断有无try代码块
//        if (!(element instanceof PsiTryStatement)) return;
//        PsiTryStatement psiTryStatement = (PsiTryStatement) element;
//        PsiCodeBlock tryBlock = psiTryStatement.getTryBlock();
//
//
//        if (tryBlock == null) return;
//        if (tryBlock.getText().contains(FAI_CLI_EXPRESSION)) {
//            System.out.println("这里进来的都是方法中的代码块");
//            NavigationGutterIconBuilder<PsiElement> builder =
//                    NavigationGutterIconBuilder.create(Icons.SPRING_INJECTION_ICON)
//                            .setAlignment(GutterIconRenderer.Alignment.CENTER)
//                            .setTarget(element)
//                            .setTooltipTitle("Data access object found - ");
//            result.add(builder.createLineMarkerInfo(element));
//        }



        //判断是否为Psi表达式代码块
        if (!(element instanceof PsiReferenceExpression)) return;
        PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) element;
        if (psiReferenceExpression.getCanonicalText().equals(FAI_CLI_EXPRESSION)) {
            System.out.println(psiReferenceExpression.getContainingFile().getName());
            //如果不是XXXCli.java的文件剔除
            if (!psiReferenceExpression.getContainingFile().getName().endsWith("Cli.java")) return;

            PsiElement[] children = psiReferenceExpression.getNextSibling().getChildren();
            //children[1].getText();              //AcctOptDef.Protocol.Cmd.GET_ACCTOPT_DATA
            //psiReferenceExpression.getContext();//sendProtocol.setCmd(AcctOptDef.Protocol.Cmd.GET_ACCTOPT_DATA);

            PsiTreeUtil.findChildrenOfType(children[1],PsiLiteralExpression.class);


            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(Icons.SPRING_INJECTION_ICON)
                            .setAlignment(GutterIconRenderer.Alignment.CENTER)
                            .setTarget(element)
                            .setTooltipTitle("Data access object found - ");
            result.add(builder.createLineMarkerInfo(element));

        }


    }

}
