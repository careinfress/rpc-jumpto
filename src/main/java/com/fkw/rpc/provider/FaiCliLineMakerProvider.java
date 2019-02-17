package com.fkw.rpc.provider;

import com.fkw.rpc.finders.PsiElementUsageFinderFactory;
import com.fkw.rpc.utils.Constant;
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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FaiCliLineMakerProvider extends RelatedItemLineMarkerProvider {


    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

//        System.out.println(psiMethodCallExpression.getMethodExpression().getQualifiedName());
//        输出: sendProtocol.setCmd
//        System.out.println(psiMethodCallExpression.getText());
//        输出: sendProtocol.setCmd(CdnDef.Protocol.Cmd.REFRESH_OBJECT_CACHES)

        if (!isTargetField(element)) return;
        PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;
        PsiElement nextSibling = psiMethodCallExpression.getFirstChild().getNextSibling();
        PsiElement tagPsi = nextSibling.getFirstChild().getNextSibling();

        //如果缓存不存在
        if (!JavaUtils.cliToSvrCache.containsKey(tagPsi.getText())) {
            String clazzName = Constant.FAI_APP_PACKAGE_NAME_PREFIX + tagPsi.getFirstChild().getText();
            Optional<PsiClass> clazz = JavaUtils.findClazz(element.getProject(), clazzName);
            if (!clazz.isPresent()) return;
            PsiField[] allFields = clazz.get().getFields();
            for (PsiField field : allFields) {
                if (!(field instanceof PsiNamedElement)) return;
                PsiNamedElement psiNamedElement = (PsiNamedElement) field;
                //System.out.println(psiNamedElement.getName());
                // 输出 ：ADD_DOMAIN_BPS_DATA
                ReferenceCollection references = ReferenceCollection.EMPTY;
                if (tagPsi.getText().endsWith(psiNamedElement.getName())) {
                    references.addAll(PsiElementUsageFinderFactory.getUsageFinder(field).findUsages());
                    JavaUtils.cliThreadAnalysis(tagPsi.getText(), references);
                    references.clear();
                }
            }
        }


        //缓存存在
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(Icons.FAI_SVR_NEW_ICON)
                        .setAlignment(GutterIconRenderer.Alignment.CENTER)
                        .setTarget(JavaUtils.cliToSvrCache.get(tagPsi.getText()))
                        .setTooltipTitle("");
        result.add(builder.createLineMarkerInfo(element));
    }

    private boolean isTargetField(PsiElement psiElement) {
        boolean flag = false;
        //1. 判断格式是否符合
        if (!(psiElement instanceof PsiMethodCallExpression)) return flag;
        //2. 判断标记前缀格式是否符合
        PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) psiElement;
        if (!psiMethodCallExpression.getMethodExpression().getQualifiedName().equals(Constant.FAI_CLI_TAGR_METHOD_EXPRESSION)) return flag;
        //3. 判断类名前缀是否符合
        PsiJavaFile psiJavaFile = PsiTreeUtil.getParentOfType(psiElement, PsiJavaFile.class);
        if (psiJavaFile == null || !psiJavaFile.getPackageName().equals(Constant.FAI_CLI_PREFIX)) return flag;
        //4. 判断类的父类是否符合
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
        if (psiClass == null) return flag;
        PsiClass superClass = psiClass.getSuperClass();
        if (superClass == null) return flag;
        if (StringUtils.isEmpty(superClass.getName()) || !superClass.getName().equals("FaiClient")) return flag;
        flag = true;
        return flag;
    }
}
