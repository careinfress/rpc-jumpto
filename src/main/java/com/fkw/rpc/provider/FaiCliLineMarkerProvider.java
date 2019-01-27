package com.fkw.rpc.provider;


import com.fkw.rpc.wrapper.Reference;
import com.fkw.rpc.wrapper.ReferenceCollection;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
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



        /*//判断是否为方法
        if (!(element instanceof PsiMethod)) return;

        //方法的父类为class
        PsiElement psiClazz = element.getParent();//PsiClass
        if (psiClazz == null || !(element instanceof PsiClass)) return;
        PsiClass psiClass = (PsiClass) psiClazz;
        //判断是否在fai.cli包下
        if (!Objects.requireNonNull(psiClass.getQualifiedName()).startsWith(FAI_CLI_PREFIX)) return;

        PsiMethod psiMethod = (PsiMethod) element;
        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        if (psiCodeBlock == null) return;
        PsiStatement[] statements = psiCodeBlock.getStatements();
        for (PsiStatement statement : statements) {
            System.out.println(statement.getText());
            System.out.println("====================");
            if (statement.getText().contains(FAI_CLI_EXPRESSION)) {
                NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(Icons.SPRING_INJECTION_ICON)
                        .setAlignment(GutterIconRenderer.Alignment.CENTER)
                        .setTarget(element)
                        .setTooltipTitle("Data access object found - ");
                result.add(builder.createLineMarkerInfo(element));
            }
        }*/


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
            //System.out.println(psiReferenceExpression.getContext());
            PsiElement[] children = psiReferenceExpression.getNextSibling().getChildren();

            ReferencesSearch.SearchParameters searchParameters = new ReferencesSearch.SearchParameters(children[1], children[1].getUseScope(), true);
            ReferenceCollection referenceCollection = new ReferenceCollection(ReferencesSearch.search(searchParameters).findAll());

            for (Reference reference : referenceCollection) {
                System.out.println(reference.containingClass().getText());
            }


//            NavigationGutterIconBuilder<PsiElement> builder =
//                    NavigationGutterIconBuilder.create(Icons.SPRING_INJECTION_ICON)
//                            .setAlignment(GutterIconRenderer.Alignment.CENTER)
//                            .setTarget(element)
//                            .setTooltipTitle("Data access object found - ");
//            result.add(builder.createLineMarkerInfo(element));

        }


    }

}
