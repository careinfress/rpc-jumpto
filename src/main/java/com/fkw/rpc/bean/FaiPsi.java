package com.fkw.rpc.bean;

public class FaiPsi {

    private String cliKey;
    private String cliClassName;
    private String proClassName;
    private String cliMethodName;
    private String proMethodName;


    public String getCliKey() {
        return cliKey;
    }

    public void setCliKey(String cliKey) {
        this.cliKey = cliKey;
    }

    public String getCliClassName() {
        return cliClassName;
    }

    public void setCliClassName(String cliClassName) {
        this.cliClassName = cliClassName;
    }

    public String getProClassName() {
        return proClassName;
    }

    public void setProClassName(String proClassName) {
        this.proClassName = proClassName;
    }

    public String getCliMethodName() {
        return cliMethodName;
    }

    public void setCliMethodName(String cliMethodName) {
        this.cliMethodName = cliMethodName;
    }

    public String getProMethodName() {
        return proMethodName;
    }

    public void setProMethodName(String proMethodName) {
        this.proMethodName = proMethodName;
    }

    @Override
    public String toString() {
        return "FaiPsi{" +
                "cliKey='" + cliKey + '\'' +
                ", cliClassName='" + cliClassName + '\'' +
                ", proClassName='" + proClassName + '\'' +
                ", cliMethodName='" + cliMethodName + '\'' +
                ", proMethodName='" + proMethodName + '\'' +
                '}';
    }
}
