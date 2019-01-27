package com.fkw.rpc.wrapper;

import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class DataHolder {
    public Project PROJECT;
    public Editor EDITOR;
    public Module MODULE;
    public PsiElement PSI_ELEMENT;
    public Navigatable NAVIGATABLE;
    public VirtualFile VIRTUAL_FILE;
    public PsiFile PSI_FILE;

    private DataHolder() {
    }

    private static DataHolder _instance = null;

    public static DataHolder getInstance() {
        return _instance = (_instance == null ? new DataHolder() : _instance);
    }

    public void initDataHolder(DataContext dataContext) {
        PROJECT = (Project) dataContext.getData(DataConstants.PROJECT);
        EDITOR = (Editor) dataContext.getData(DataConstants.EDITOR);
        MODULE = (Module) dataContext.getData(DataConstants.MODULE);
        PSI_ELEMENT = (PsiElement) dataContext.getData(DataConstants.PSI_ELEMENT);
        NAVIGATABLE = (Navigatable) dataContext.getData(DataConstants.NAVIGATABLE);
        VIRTUAL_FILE = (VirtualFile) dataContext.getData(DataConstants.VIRTUAL_FILE);
        PSI_FILE = (PsiFile) dataContext.getData(DataConstants.PSI_FILE);
    }
}
