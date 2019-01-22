package com.fkw.rpc.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FaiCliLineMarkerProvider extends RelatedItemLineMarkerProvider {

    String FAI_CLI_PREFIX  = "fai.cli";

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        //判断元素是不是文本文件
        if (!(element instanceof PsiField)) return;

        super.collectNavigationMarkers(element, result);
    }


    private boolean isTargetField(@NotNull PsiField field) {

        String name = field.getName();
        if (StringUtil.isEmpty(name) || !name.startsWith(FAI_CLI_PREFIX)) {
            return false;
        }

        return false;
    }
}
