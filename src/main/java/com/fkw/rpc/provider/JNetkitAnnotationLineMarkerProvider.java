package com.fkw.rpc.provider;

import com.fkw.rpc.Annotation.Annotation;
import com.fkw.rpc.finders.PsiElementUsageFinderFactory;
import com.fkw.rpc.helper.PsiHelper;
import com.fkw.rpc.utils.Constant;
import com.fkw.rpc.utils.FaiUtils;
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
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class JNetkitAnnotationLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        if (!(element instanceof PsiModifierListOwner)) return;
        if (!isTargetField(element)) return;

        PsiModifierListOwner psiModifierListOwner = (PsiModifierListOwner) element;
        Optional<String> annotationValueText = JavaUtils.getAnnotationValueText(psiModifierListOwner, Annotation.JNETKIT_CMD);
        if (!annotationValueText.isPresent()) return;

        //缓存不存在
        if (!Constant.svrToCliCache.containsKey(element)) {
            String cliKey = annotationValueText.get();//eg:DnsDef.Protocol.Cmd.GET_ADDR_LIST
            if (StringUtils.isEmpty(cliKey)) return;
            String classQualifiedName = FaiUtils.getAppDefClassQualifiedName(cliKey);
            String fieldName = FaiUtils.getAppDefFieldName(cliKey);
            if (StringUtils.isEmpty(classQualifiedName) || StringUtils.isEmpty(fieldName)) return;
            Optional<PsiField> javaField = JavaUtils.findJavaField(element.getProject(), classQualifiedName, fieldName);
            if (!javaField.isPresent()) return;

            ReferenceCollection references = ReferenceCollection.EMPTY;
            references.addAll(PsiElementUsageFinderFactory.getUsageFinder(javaField.get()).findUsages());
            for (Reference reference : references) {
                if (reference.containingPackage().equals(Constant.FAI_CLI_PREFIX)) {
                    PsiHelper psiHelper = new PsiHelper(annotationValueText.get(), reference.getPsiElement(), element);
                    Constant.svrToCliCache.put(element, psiHelper);
                    Constant.cliToSvrCache.put(annotationValueText.get(), psiHelper);
                }
            }
            references.clear();
        }

        if (Constant.svrToCliCache.get(element) == null) return;
        //缓存存在
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(Icons.FAI_SVR_PLANE_ICON)
                        .setAlignment(GutterIconRenderer.Alignment.CENTER)
                        .setTarget(Constant.svrToCliCache.get(element).getCliPsiElement())
                        .setTooltipTitle("");
        result.add(builder.createLineMarkerInfo(element));
    }

    /**
     * 功能 : 判断元素的父类是否为FaiHandler
     *
     * @param psiElement 待校验的psiElement
     * @return  true/false
     */
    private boolean isTargetField(PsiElement psiElement) {
        boolean flag = false;
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
        if (psiClass == null) return flag;
        PsiClass superClass = psiClass.getSuperClass();
        if (superClass == null) return flag;
        if (StringUtils.isEmpty(superClass.getName()) || !superClass.getName().equals("FaiHandler")) return flag;
        flag = true;
        return flag;
    }



}
