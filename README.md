# 										![rpc-jumpto](http://15878290.s21i.faiusr.com/4/ABUIABAEGAAg47it4wUopp7UsgcwgAE4gAE.png)																			                                     Doodle Jump

### 1. 实现目的

**解决RPC框架中cli端跟svr端方法代码的跳转问题**

### 2. 安装方法

在**IDEA-->File-->Plugns-->Browse repositories**中搜索`Doodle Jump`，然后安装重启就可以使用了。

![](http://15878290.s21i.faiusr.com/3/ABUIABADGAAg38yt4wUo56ivrgIw1g04yQY.gif)

### 3. 实现思路

>1. 当IDEA随机打开一个文件的时候，会触发 `/resources/META-INF/plugin.xml` 中定义好的`Provider`的实现类的`collectNavigationMarkers` 方法。
>
>2. 方法将会扫描打开文件的各个`PsiElement`，`PsiElement`可以理解为Android中各个View组件的最基础元素，比如`TextView`，`ImageView`等继承于`View`，在IDEA的世界中，每个Element都继承`PsiElement`，比如`PsiJavaFile` ，`PsiMethod`等。
>
>3. 如果对上面的Element理解不是很清楚，可以在IDEA中下载一个叫做[PsiViewer](https://plugins.jetbrains.com/plugin/227-psiviewer)的插件，这个插件可以展示在IDEA中任何一个文件的PSI DOM结构，具体参考下图：
>
>   
>
>   ![](http://15878290.s21i.faiusr.com/2/ABUIABACGAAgv8it4wUom56Y6wUwowQ4sQc.jpg)
>
>   
>
>4. 当扫描到目标`PsiElement`后，接下来的操作就是找寻靶向`PsiElement`了，具体可以参考[github ](https://github.com/careinfress/rpc-jumpto)中的具体实现代码。可以从`/provider/`中的各个实现类开始



### 4. 工作列表

- [x] 第一代RPC框架Cli端跳转找到Proc
- [x] 第一代RPC框架注解方式Cli端的跳转到Proc
- [x] 第二代RPC框架Cli端跳转找到Proc
- [x] 支持第一代RPC框架多Proc的情况
- [x] 解决第一代插件跳转bug
- [ ] 支持第一代RPC框架从Proc跳转到Cli
- [x] 支持第二代RPC框架从Proc跳转到Cli
- [ ] 支持互动基于第二代RPC框架多Proc的情况



