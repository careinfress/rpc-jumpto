package com.fkw.rpc.provider;

import com.fkw.rpc.bean.FaiPsi;
import com.fkw.rpc.finders.PsiElementUsageFinderFactory;
import com.fkw.rpc.utils.Constant;
import com.fkw.rpc.utils.FaiUtils;
import com.fkw.rpc.utils.Icons;
import com.fkw.rpc.utils.JavaUtils;
import com.fkw.rpc.wrapper.ReferenceCollection;
import com.google.common.base.Optional;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
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

        PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        FaiPsi faiPsi = new FaiPsi();
        faiPsi.setCliKey(tagPsi.getText());
        faiPsi.setCliClassName(psiClass.getQualifiedName());
        faiPsi.setCliMethodName(psiMethod.getName());

        //如果缓存不存在
        //eg: tagPsi.getText() == CdnDef.Protocol.Cmd.REFRESH_OBJECT_CACHES
        if (!Constant.faiCache.containsKey(tagPsi.getText())) {
            String clazzName = FaiUtils.getAppDefClassQualifiedName(tagPsi.getText());
            String fieldName = FaiUtils.getAppDefFieldName(tagPsi.getText());
            Optional<PsiField> javaField = JavaUtils.findJavaField(element.getProject(), clazzName, fieldName);
            if (!javaField.isPresent()) return;
            ReferenceCollection references = ReferenceCollection.EMPTY;
            references.addAll(PsiElementUsageFinderFactory.getUsageFinder(javaField.get()).findUsages());
            FaiUtils.cliElementAnalysis(faiPsi, references);
            references.clear();
        }


        FaiPsi psi = Constant.faiCache.get(tagPsi.getText());
        if (psi == null) return;
        Optional<PsiMethod> adress = JavaUtils.getProcAdressByFaiPsi(element, psi);
        if (!adress.isPresent()) return;

        //缓存存在
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(Icons.FAI_SVR_FAISCO_ICON)
                        .setAlignment(GutterIconRenderer.Alignment.CENTER)
                        .setTarget(adress.get().getNameIdentifier())
                        .setTooltipTitle("");
                        //.setTooltipTitle("Data access object found - " + adress.get().getName());
        result.add(builder.createLineMarkerInfo(adress.get().getNameIdentifier()));
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
