package com.harmony.gestureview.slice;

import com.harmony.gestureview.ResourceTable;
import io.gitee.chordwang.gesture.GestureView;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;

public class MainAbilitySlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
    }

    @Override
    public void onActive() {
        super.onActive();


        GestureView gestureView = (GestureView) findComponentById(ResourceTable.Id_gesture_view);
        gestureView.setMinGestureCount(3);//设置最少链接手势点个数。默认值为1。
        gestureView.setMaxGestureCount(9);//设置最大链接手势点个数。默认值为行*列。
        gestureView.setGestureStyle(GestureView.GestureStyle.IMAGE);//设置手势点样式。默认为线条格式，可设置为自定义图片。
        gestureView.setGestureDisappear(GestureView.GestureDisappearMode.APPEAR_ONLY_ERROR_ON_FINISH);//设置手势路径显示消失模式。默认为手势结束时不显示已绘制路径。
        gestureView.setGestureDrawOrder(GestureView.GestureDrawOrder.SECTION_TOP);//设置手势点与路径线条的绘制顺序。默认为手势点格子在上。
        gestureView.setGestureRepeatMode(GestureView.GestureRepeatMode.DEFAULT);//设置手势路径重复模式。默认为除相邻两个点不可重复外，其余每个点可重复多次。
        gestureView.setColumn(3);//设置列数。
        gestureView.setRow(3);//设置行数。
        gestureView.setImageNormal(ResourceTable.Media_icon_zao_gesture_normal);//设置手势点样式为自定义图片时的 未选中 状态图片。
        gestureView.setImageChecked(ResourceTable.Media_icon_zao_gesture_checked);//设置手势点样式为自定义图片时的 选中 状态图片。
        gestureView.setImageError(ResourceTable.Media_icon_zao_gesture_error);//设置手势点样式为自定义图片时的为 错误 状态图片。
        gestureView.setLineColor(new Color(Color.getIntColor("#ff0081ff")));//设置手势路径的线段的颜色。
        //设置手势监听。
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
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
