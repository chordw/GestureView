# GestureView

![platform](https://img.shields.io/badge/platform-harmoyOS-black) ![](https://img.shields.io/badge/author-chordwang-brightgreen) ![](https://img.shields.io/badge/license-Apache--2.0-blue)

**github地址**：<https://github.com/chordw/GestureView>

**gitee地址**：<https://gitee.com/chordwang/gesture-view>

#### 介绍

HamonyOS 下的手势控件。

![Image text](https://gitee.com/chordwang/gesture-view/raw/master/image/WX20210622-164921@2x.png)


#### 功能说明

- 支持设置列数。
- 支持设置行数。
- 支持设置最少链接手势点个数。默认值为1。
- 支持设置最大链接手势点个数。默认值为行*列。
- 支持设置手势点样式。默认为线条格式，可设置为自定义图片。
- 支持设置手势路径的线段的颜色。
- 支持设置手势点样式为自定义图片时的 未选中 状态图片。
- 支持设置手势点样式为自定义图片时的 选中 状态图片。
- 支持设置手势点样式为自定义图片时的为 错误 状态图片。
- 支持设置手势路径显示消失模式。默认为手势结束时不显示已绘制路径。
- 支持设置手势点与路径线条的绘制顺序。默认为手势点格子在上。
- 支持设置手势路径重复模式。默认为除相邻两个点不可重复外，其余每个点可重复多次。

#### 使用说明

[详情参考demo](https://gitee.com/chordwang/gesture-view/blob/master/entry/src/main/java/com/harmony/gestureview/slice/MainAbilitySlice.java)

#### Gradle
**本项目已经上传mavenCentral。** ![](https://img.shields.io/badge/mavenCentral-GestureView-green)
```
implementation 'io.gitee.chordwang:gestureview:1.0.0'
```
工程根目录的build.gradle中添加如下：
```
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```


#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


