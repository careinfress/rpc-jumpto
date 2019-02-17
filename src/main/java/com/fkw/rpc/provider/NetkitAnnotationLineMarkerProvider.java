package com.fkw.rpc.provider;

import com.fkw.rpc.Annotation.Annotation;
import com.fkw.rpc.finders.PsiElementUsageFinderFactory;
import com.fkw.rpc.helper.PsiHelper;
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
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class NetkitAnnotationLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        if (!(element instanceof PsiModifierListOwner)) return;
        if (!isTargetField(element)) return;

        PsiModifierListOwner psiModifierListOwner = (PsiModifierListOwner) element;
        Optional<String> annotationValueText = JavaUtils.getAnnotationValueText(psiModifierListOwner, Annotation.NETKIT_CMD);
        if (!annotationValueText.isPresent()) return;
//      System.out.println(annotationValueText.get());//DnsDef.Protocol.Cmd.GET_ADDR_LIST

        if (!Constant.svrToCliCache.containsKey(element)) {
            String cliKey = annotationValueText.get();
            if (StringUtils.isEmpty(cliKey)) return;
            int index = cliKey.lastIndexOf(".");
            if (index <= 0) return;
            String classQualifiedName = Constant.FAI_APP_PACKAGE_NAME_PREFIX + cliKey.substring(0, index);
            String fieldName = annotationValueText.get().substring(index + 1);
            Optional<PsiField> javaField = JavaUtils.findJavaField(element.getProject(), classQualifiedName, fieldName);
            if (!javaField.isPresent()) return;

            ReferenceCollection references = ReferenceCollection.EMPTY;
            references.addAll(PsiElementUsageFinderFactory.getUsageFinder(javaField.get()).findUsages());
            for (Reference reference : references) {
                if (reference.containingPackage().equals(Constant.FAI_CLI_PREFIX)) {
                    PsiHelper psiHelper = new PsiHelper();
                    psiHelper.setCliKey(annotationValueText.get());
                    psiHelper.setSvrPsiElement(element);
                    psiHelper.setCliPsiElement(reference.getPsiElement());
                    Constant.svrToCliCache.put(element, psiHelper);
                    Constant.cliToSvrCache.put(annotationValueText.get(), psiHelper);
                }
            }
            references.clear();
        }

        //缓存存在
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(Icons.FAI_SVR_bird_ICON)
                        .setAlignment(GutterIconRenderer.Alignment.CENTER)
                        .setTarget(Constant.svrToCliCache.get(element).getCliPsiElement())
                        .setTooltipTitle("");
        result.add(builder.createLineMarkerInfo(element));
    }

    /**
     * 功能 : 判断元素类是否继承GenericProc
     *
     * @param psiElement 待校验的psiElement
     * @return  true/false
     */
    private boolean isTargetField(PsiElement psiElement) {
        boolean flag = false;
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
        if (psiClass == null) return flag;
        PsiReferenceList implementsList = psiClass.getImplementsList();
        if (implementsList == null) return flag;
        PsiClassType[] referencedTypes = implementsList.getReferencedTypes();
        for (PsiClassType referencedType : referencedTypes) {
            if (referencedType.getClassName().equals("GenericProc")) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

}
