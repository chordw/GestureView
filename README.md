# GestureView

![platform](https://img.shields.io/badge/platform-harmoyOS-black) ![](https://img.shields.io/badge/author-chordwang-brightgreen) ![](https://img.shields.io/badge/license-Apache--2.0-blue)

**github地址**：<https://github.com/chordw/GestureView>

**gitee地址**：<https://gitee.com/chordwang/gesture-view>

#### 介绍

HamonyOS 下的手势控件。

![image](https://gitee.com/chordwang/gesture-view/raw/master/image/WX20210622-164921@2x.png)

![gif](https://gitee.com/chordwang/gesture-view/raw/master/image/introduce.gif)

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

**主要接口**
```
    //手势路径点重复模式
    public interface GestureRepeatMode {
        int DEFAULT = 0;//除相邻两个点不可重复外，其余每个点可重复多次。默认值。
        int NO_REPEAT = 1;//每个点单独不可重复。
    }

    //手势点样式
    public interface GestureStyle {
        int LINE = 0;//线条
        int IMAGE = 1;//图片
    }
    
    //当前状态
    public interface GestureState {
        int NORMAL = 0;//普通状态
        int ERROR = 1;//错误状态
    }

    //结束时手势显示模式
    public interface GestureDisappearMode {
        int APPEAR_ON_FINISH = 11;//手势结束时显示已绘制路径
        int DISAPPEAR_ON_FINISH = 12;//手势结束时不显示已绘制路径
        int APPEAR_ONLY_ERROR_ON_FINISH = 13;//仅当错误状态时，手势结束时显示已绘制路径
    }
    
    //手势点与路径绘制优先级
    public interface GestureDrawOrder {
        int SECTION_TOP = 0;//格子在上
        int LINE_TOP = 1;//线条在上
    }
    
    //结果监听
    public interface DetectedListener {
        int DETECT_SUCCESS = 0;//识别成功，手势点数正常
        int DETECT_LIMITED_MIN = 10;//识别失败，未达到手势点最小个数限制
        int DETECT_LIMITED_MAX = 11;//识别失败，超到手势点最大个数限制
        int DETECT_CANCEL = 20;//识别失败，意外取消

        /**
         * @param code            结果码
         * @param detectedGesture code为DETECT_SUCCESS时不为null,其他为null
         */
        void onDetectedGesture(int code, List<Section> detectedGesture);
    }
```

**主要方法**

- 设置手势点图片样式时手势未选中时图片资源ID。
```
gestureView.setImageNormal(ResourceTable.Media_icon_zao_gesture_normal);
```

- 设置手势点图片样式时手势未选中时图片。
```
gestureView.setImageNormal(PixelMapHolder imageNormal);
```

- 设置手势点图片样式时手势选中时图片资源ID。
```
gestureView.setImageChecked(int resId);
```

- 设置手势点图片样式时手势选中时图片。
```
gestureView.setImageChecked(PixelMapHolder imageChecked);
```

- 设置手势点图片样式时手势错误时图片资源ID。
```
gestureView.setImageError(int resId);
```

- 设置手势点图片样式时手势错误时图片。
```
gestureView.setImageError(PixelMapHolder imageError);
```

- 设置手势点线条样式时未选中状态线条颜色。默认是Color.GREEN。
```
gestureView.setLineNormalColor(Color color);
```

- 设置手势点线条样式时线条颜色。默认是Color.GREEN。
```
gestureView.setLineCheckedColor(Color color);
```

- 设置手势控件错误状态时手势路径线条颜色。默认是Color.RED。
```
gestureView.setLineErrorColor(Color lineErrorColor)
```

- 设置手势路径线条默认颜色。默认是Color.GREEN。
```
gestureView.setLineColor(Color color);
```

- 设置手势点线条样式的线宽度。默认是4px。
```
gestureView.setLineWidthSection(float lineWidthSection);
```

- 设置手势路径线的宽度。默认是4px。
```
gestureView.setLineWidth(float lineWidth);
```

- 设置手势控件点阵行数
```
gestureView.setRow(int row);
```
- 设置手势控件点阵列数
```
gestureView.setColumn(int column);
```

- 设置连接点最少个数
```
gestureView.setMinGestureCount(int minGestureCount);
```

- 设置连接点最大个数
```
gestureView.setMaxGestureCount(int maxCount);
```

- 清空已选中手势数据
```
gestureView.clearGesture();
```

- 根据手势点阵生成序列字符串。横向排列，起始0，顺序+1拼接成字符序列。
```
String string = GestureView.section2String(detectedGesture);
```

- 判断是否是简单手势：转折点个数是否小于传入值。实际转折点比预期转折点小则为简单手势。反之，则为复杂手势。
```
boolean simpleGesture = GestureView.isSimpleGesture(detectedGesture, 1);
```

- 设置手势绘制监听器
```
gestureView.setOnDetectedListener((code, detectedGesture) -> {
            //手势结束回调。
            if (code == GestureView.DetectedListener.DETECT_SUCCESS) {//绘制成功。
                String string = GestureView.section2String(detectedGesture);
                boolean simpleGesture = GestureView.isSimpleGesture(detectedGesture, 1);
                string = (simpleGesture ? "密码过于简单：" : "") + string;
                new ToastDialog(getAbility())
                        .setText(string)
                        .setAlignment(LayoutAlignment.CENTER)
                        .show();
            } else if (code == GestureView.DetectedListener.DETECT_LIMITED_MIN) {//手势点个数未达到设置的最小值。
                gestureView.setGestureState(GestureView.GestureState.ERROR);
                new ToastDialog(getAbility())
                        .setText("请连接最少" + gestureView.getMinGestureCount() + "个点")
                        .setAlignment(LayoutAlignment.CENTER)
                        .show();
            } else if (code == GestureView.DetectedListener.DETECT_LIMITED_MAX) {//手势点个数超到设置的最大值。
                gestureView.setGestureState(GestureView.GestureState.ERROR);
                new ToastDialog(getAbility())
                        .setText("请连接最多" + gestureView.getMaxGestureCount() + "个点")
                        .setAlignment(LayoutAlignment.CENTER)
                        .show();
            }
        });
```

#### Gradle
**本项目已经上传mavenCentral。** ![](https://img.shields.io/maven-central/v/io.gitee.chordwang/gestureview)
```
implementation 'io.gitee.chordwang:gestureview:1.0.0'//最新版本见上面
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

#### Licenses

```
   Copyright [2021] [chordwang]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```


#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


