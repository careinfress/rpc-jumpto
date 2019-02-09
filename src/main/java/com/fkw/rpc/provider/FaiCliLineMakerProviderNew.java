package com.fkw.rpc.provider;

import com.fkw.rpc.finders.PsiElementUsageFinderFactory;
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
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FaiCliLineMakerProviderNew extends RelatedItemLineMarkerProvider {

    private final String FAI_CLI_PREFIX = "fai.cli";
    private final String FAI_APP_PREFIX = "fai.app.";
    private final String FAI_CLI_EXPRESSION = "sendProtocol.setCmd";


    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        if (!(element instanceof PsiMethodCallExpression)) return;
        PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;

//        System.out.println(psiMethodCallExpression.getMethodExpression().getQualifiedName());//sendProtocol.setCmd
//        System.out.println(psiMethodCallExpression.getText());//sendProtocol.setCmd(CdnDef.Protocol.Cmd.REFRESH_OBJECT_CACHES)


        if (psiMethodCallExpression.getMethodExpression().getQualifiedName().equals(FAI_CLI_EXPRESSION)) {

            //判断包名是否为fai.cli
            PsiJavaFile parentOfType = PsiTreeUtil.getParentOfType(element, PsiJavaFile.class);
            if (!parentOfType.getPackageName().equals(FAI_CLI_PREFIX)) return;

            PsiElement nextSibling = psiMethodCallExpression.getFirstChild().getNextSibling();
            PsiElement tagPsi = nextSibling.getFirstChild().getNextSibling();

            //如果缓存不存在
            if (!JavaUtils.cliToSvrCache.containsKey(tagPsi.getText())) {
                String clazzName = FAI_APP_PREFIX + tagPsi.getFirstChild().getText();
                Optional<PsiClass> clazz = JavaUtils.findClazz(element.getProject(), clazzName);
                if (!clazz.isPresent()) return;
                PsiField[] allFields = clazz.get().getAllFields();
                for (PsiField field : allFields) {
                    if (!(field instanceof PsiNamedElement)) return;
                    PsiNamedElement psiNamedElement = (PsiNamedElement) field;
                    //System.out.println(psiNamedElement.getName());
                    /**
                     * ADD_DOMAIN_BPS_DATA
                     * UPD_DOMAIN_BPS_DATA
                     * GET_DOMAIN_BPS_DATA
                     */

                    final ReferenceCollection references = ReferenceCollection.EMPTY;
                    references.addAll(PsiElementUsageFinderFactory.getUsageFinder(field).findUsages());
                    JavaUtils.cliThreadAnalysis(tagPsi.getFirstChild().getText() + "."  + field.getName(), references);
                    /*if (references.size() > 0) {
                        JavaUtils.cliThreadAnalysis(tagPsi.getText(), references);
                        break;
                    }*/

                }
            }

            //缓存存在
            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(Icons.SPRING_INJECTION_ICON)
                            .setAlignment(GutterIconRenderer.Alignment.CENTER)
                            .setTarget(JavaUtils.cliToSvrCache.get(tagPsi.getText()))
                            .setTooltipTitle("Data access object found - ");
            result.add(builder.createLineMarkerInfo(element));
        }

    }
}
