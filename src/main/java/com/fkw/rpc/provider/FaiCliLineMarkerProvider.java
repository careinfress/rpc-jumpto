package com.fkw.rpc.provider;

import com.fkw.rpc.utils.Icons;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FaiCliLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private final String FAI_CLI_PREFIX = "fai.cli";
    private final String FAI_CLI_EXPRESSION = "sendProtocol.setCmd";

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        //判断是否为class元素
        if (!(element instanceof PsiClass)) return;
        //判断包名是否符合规定
        if (!isTargetField(element)) return;

        PsiClass psiClass = (PsiClass) element;
        //拿到该类的所有的所有方法
        PsiMethod[] allMethods = psiClass.getAllMethods();
        for (PsiMethod method : allMethods) {
            //获取最每个方法最后的代码块
            PsiElement lastChild = method.getLastChild();
            if (!(lastChild instanceof PsiCodeBlock)) return;
            PsiCodeBlock psiCodeBlock = (PsiCodeBlock) lastChild;
            PsiElement[] children = psiCodeBlock.getChildren();
            for (PsiElement child : children) {
                //如果包含try代码块
                if ((child instanceof PsiTryStatement)) {
                    PsiTryStatement PsiTryStatement = (PsiTryStatement)child;
                    PsiCodeBlock tryBlock = PsiTryStatement.getTryBlock();
                    if (tryBlock == null) {
                        continue;
                    }
                    //拿到try代码块中指定变量
                    PsiStatement[] tryBlockStatements = tryBlock.getStatements();
                    for (PsiStatement tryBlockStatement : tryBlockStatements) {
                        if (tryBlockStatement.getText().equals(FAI_CLI_EXPRESSION)) {
                            //可以注上标记
                            NavigationGutterIconBuilder<PsiElement> builder =
                                    NavigationGutterIconBuilder.create(Icons.SPRING_INJECTION_ICON)
                                            .setAlignment(GutterIconRenderer.Alignment.CENTER)
                                            .setTarget(psiClass)
                                            .setTooltipTitle("Data access object found - " + psiClass.getQualifiedName());
                            result.add(builder.createLineMarkerInfo(((PsiNameIdentifierOwner) element).getNameIdentifier()));
                        }
                    }
                }
            }
        }

    }



    private boolean isTargetField(@NotNull PsiElement element) {
        //cli类的包名是固定的,都是在fai.cli下
        PsiPackageStatement psiPackageStatement =  (PsiPackageStatement) element;
        return psiPackageStatement.getPackageName().equals(FAI_CLI_PREFIX);
    }


}
