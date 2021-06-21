package com.wx.harmony.gesture;

import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.Path;
import ohos.agp.render.PixelMapHolder;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.agp.utils.RectFloat;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.ImageSource;
import ohos.media.image.common.PixelFormat;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class GestureView extends Component implements Component.EstimateSizeListener, Component.TouchEventListener, Component.DrawTask {

    private final static HiLogLabel TAG = new HiLogLabel(HiLog.LOG_APP, 0x0058, "Console-Java");

    /**
     * 手势监听
     */
    private DetectedListener onDetectedListener;

    private Paint mPaint;//🖌️对象
    private PixelMapHolder imageNormal;
    private PixelMapHolder imageChecked;
    private PixelMapHolder imageError;
    private final Color LineDefaultColor = Color.GREEN;
    private Color lineNormalColor = LineDefaultColor;//格子-线条-未选中时颜色
    private Color lineCheckedColor = LineDefaultColor;//格子-线条-选中时颜色
    private Color lineColor = LineDefaultColor;//线条-选中时颜色
    private Color lineErrorColor = Color.RED;//线条-错误时颜色
    private float lineWidthSection = 4.0f;//格子-线条-宽度
    private float lineWidth = 4.0f;//线条-宽度
    private int column = 3;//格子-列数
    private int row = 3;//格子-行数
    /**
     * @noinspection ConstantConditions
     */
    private int minGestureCount = 1;//最少设置手势点个数
    private int maxGestureCount = row * column;//最多设置手势点个数
    private int gestureRepeatMode = GestureRepeatMode.DEFAULT;//手势点重复模式
    private int gestureStyle = GestureStyle.LINE;//格子样式
    private int gestureState = GestureState.NORMAL;//手势状态
    private int gestureDisappear = GestureDisappearMode.DISAPPEAR_ON_FINISH;//手势状态
    private int gestureDrawOrder = GestureDrawOrder.SECTION_TOP;//格子和连接线段绘制顺序

    public interface GestureRepeatMode {
        int DEFAULT = 0;//除相邻两个点不可重复外，其余每个点可重复多次。默认值。
        int NO_REPEAT = 1;//每个点单独不可重复。
    }

    public interface GestureStyle {
        int LINE = 0;//线条
        int IMAGE = 1;//图片
    }

    public interface GestureState {
        int NORMAL = 0;//普通状态
        int ERROR = 1;//错误状态
    }

    public interface GestureDisappearMode {
        //int DISAPPEAR_ON_MOVING = 0;//滑动过程中不显示路径及选中点

        //int APPEAR_ON_MOVING = 10;//滑动过程中显示路径及选中点
        int APPEAR_ON_FINISH = 11;//手势结束时显示已绘制路径
        int DISAPPEAR_ON_FINISH = 12;//手势结束时不显示已绘制路径
        int APPEAR_ONLY_ERROR_ON_FINISH = 13;//仅当错误状态时，手势结束时显示已绘制路径
    }

    public interface GestureDrawOrder {
        int SECTION_TOP = 0;//格子在上
        int LINE_TOP = 1;//线条在上
    }

    private List<Section> sectionArray;//原始格子数组
    private List<Section> sectionArrayChecked;//一次事件中选中的所有格子数组：有序可重复
    private boolean sliding = false;//手势是否滑动中
    private float moveX, moveY;//手势当前滑动位置

    public GestureView(Context context) {
        super(context);
        init(context);
    }

    public GestureView(Context context, AttrSet attrSet) {
        super(context, attrSet);
        init(context);
    }

    public GestureView(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        init(context);
    }

    public GestureView(Context context, AttrSet attrSet, int resId) {
        super(context, attrSet, resId);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//画笔抗锯齿
        mPaint.setStyle(Paint.Style.STROKE_STYLE);//画笔样式
        mPaint.setStrokeJoin(Paint.Join.ROUND_JOIN);
        mPaint.setStrokeCap(Paint.StrokeCap.ROUND_CAP);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(LineDefaultColor);
        setTouchFocusable(true);
        addDrawTask(this);
        setEstimateSizeListener(this);// 设置测量组件的侦听器
        setTouchEventListener(this);//设置触摸监听
    }

    /**
     * 设置手势点图片样式时手势未选中时图片资源ID。
     *
     * @param resId 手势点图片样式时手势错误时图片资源ID。
     */
    public void setImageNormal(int resId) {
        this.imageNormal = createPixelMapByRes(resId);
        updateDrawOnConfig();
    }

    /**
     * 设置手势点图片样式时手势未选中时图片。
     *
     * @param imageNormal 手势点图片样式时手势未选中时图片。
     */
    public void setImageNormal(PixelMapHolder imageNormal) {
        this.imageNormal = imageNormal;
        updateDrawOnConfig();
    }

    /**
     * 设置手势点图片样式时手势选中时图片资源ID。
     *
     * @param resId 手势点图片样式时手势选中时图片资源ID。
     */
    public void setImageChecked(int resId) {
        this.imageChecked = createPixelMapByRes(resId);
        updateDrawOnConfig();
    }

    /**
     * 设置手势点图片样式时手势选中时图片。
     *
     * @param imageChecked 手势点图片样式时手势选中时图片。
     */
    public void setImageChecked(PixelMapHolder imageChecked) {
        this.imageChecked = imageChecked;
        updateDrawOnConfig();
    }

    /**
     * 设置手势点图片样式时手势错误时图片资源ID。
     *
     * @param resId 手势点图片样式时手势错误时图片资源ID。
     */
    public void setImageError(int resId) {
        this.imageError = createPixelMapByRes(resId);
        updateDrawOnConfig();
    }

    /**
     * 设置手势点图片样式时手势错误时图片。
     *
     * @param imageError 手势点图片样式时手势错误时图片。
     */
    public void setImageError(PixelMapHolder imageError) {
        this.imageError = imageError;
        updateDrawOnConfig();
    }

    /**
     * 设置手势点线条样式时未选中状态线条颜色。默认是Color.GREEN。
     *
     * @param color 手势点线条样式时未选中状态线条颜色。
     */
    public void setLineNormalColor(Color color) {
        this.lineNormalColor = color;
        updateDrawOnConfig();
    }

    /**
     * 设置手势点线条样式时线条颜色。默认是Color.GREEN。
     *
     * @param color 手势点线条样式时选中状态线条颜色。
     */
    public void setLineCheckedColor(Color color) {
        this.lineCheckedColor = color;
        updateDrawOnConfig();
    }

    /**
     * 设置手势控件错误状态时手势路径线条颜色。默认是Color.RED。
     *
     * @param lineErrorColor 手势控件错误状态时手势路径线条颜色。
     */
    public void setLineErrorColor(Color lineErrorColor) {
        this.lineErrorColor = lineErrorColor;
        updateDrawOnConfig();
    }

    /**
     * 设置手势路径线条默认颜色。默认是Color.GREEN。
     *
     * @param color 手势路径线条默认颜色。
     */
    public void setLineColor(Color color) {
        this.lineColor = color;
        updateDrawOnConfig();
    }

    /**
     * 设置手势点线条样式的线宽度。。默认是4px。
     *
     * @param lineWidthSection 手势点线条样式的线宽度。
     */
    public void setLineWidthSection(float lineWidthSection) {
        if (lineWidthSection != this.lineWidthSection) {
            this.lineWidthSection = lineWidthSection;
            updateDrawOnConfig();
        }
    }

    /**
     * 设置手势路径线的宽度。默认是4px。
     *
     * @param lineWidth 手势路径线的宽度。单位px。
     */
    public void setLineWidth(float lineWidth) {
        if (lineWidth != this.lineWidth) {
            this.lineWidth = lineWidth;
            updateDrawOnConfig();
        }
    }

    /**
     * 设置手势控件点阵行数
     *
     * @param row 行数
     */
    public void setRow(int row) {
        if (row < 2) {
            return;
        }
        if (row != this.row) {
            this.row = row;
            updateDrawOnRowColumnConfig();
        }
    }

    /**
     * 设置手势控件点阵列数
     *
     * @param column 列数
     */
    public void setColumn(int column) {
        if (column < 2) {
            return;
        }
        if (column != this.column) {
            this.column = column;
            updateDrawOnRowColumnConfig();
        }
    }

    /**
     * 设置手势绘制监听器
     *
     * @param onDetectedListener 手势绘制监听器
     */
    public void setOnDetectedListener(DetectedListener onDetectedListener) {
        this.onDetectedListener = onDetectedListener;
    }

    /**
     * 设置连接点最少个数
     *
     * @param minGestureCount 连接点最少个数
     */
    public void setMinGestureCount(int minGestureCount) {
        if (minGestureCount != this.minGestureCount) {
            this.minGestureCount = Math.max(1, minGestureCount);
            resetCheckedSections();
            updateDrawOnConfig();
        }
    }

    public int getMinGestureCount() {
        return this.minGestureCount;
    }

    /**
     * 设置连接点最大个数
     *
     * @param maxCount 连接点最大个数
     */
    public void setMaxGestureCount(int maxCount) {
        if (maxCount != maxGestureCount) {
            this.maxGestureCount = Math.max(minGestureCount, maxCount);
            resetCheckedSections();
            updateDrawOnConfig();
        }
    }

    public int getMaxGestureCount() {
        return maxGestureCount;
    }

    /**
     * 设置连接点的重复模式。默认是除相邻两个点不可重复外，其余每个点可重复多次。{@link GestureRepeatMode}
     *
     * @param gestureRepeatMode 连接点的重复模式。
     */
    public void setGestureRepeatMode(int gestureRepeatMode) {
        if (gestureRepeatMode != this.gestureRepeatMode) {
            this.gestureRepeatMode = gestureRepeatMode;
            resetCheckedSections();
            updateDrawOnConfig();
        }
    }

    /**
     * 设置手势点的样式。默认是线条样式。可以设置线条和图片两种样式。{@link GestureStyle}
     *
     * @param gestureStyle 手势点样式
     */
    public void setGestureStyle(int gestureStyle) {
        if (gestureStyle != this.gestureStyle) {
            this.gestureStyle = gestureStyle;
            updateDrawOnConfig();
        }
    }

    /**
     * 设置手势当前状态。默认是普通状态。{@link GestureState}
     *
     * @param gestureState 手势当前状态
     */
    public void setGestureState(int gestureState) {
        if (gestureState != this.gestureState) {
            this.gestureState = gestureState;
            updateDrawOnConfig();
        }
    }

    /**
     * 设置手指离开时手势显示状态。默认是手势离开时不显示已绘制路径。{@link GestureDisappearMode}
     *
     * @param gestureDisappear 手指离开时手势显示状态
     */
    public void setGestureDisappear(int gestureDisappear) {
        if (gestureDisappear != this.gestureDisappear) {
            this.gestureDisappear = gestureDisappear;
            updateDrawOnConfig();
        }
    }

    /**
     * 设置手势点和手势路径的绘制顺序。默认是手势点在上层显示。{@link GestureDrawOrder}
     *
     * @param gestureDrawOrder 手势点和手势路径线的绘制顺序。
     */
    public void setGestureDrawOrder(int gestureDrawOrder) {
        if (gestureDrawOrder != this.gestureDrawOrder) {
            this.gestureDrawOrder = gestureDrawOrder;
            updateDrawOnConfig();
        }
    }

    /**
     * 清空已选中手势数据，重绘
     */
    public void clearGesture() {
        resetCheckedSections();
        invalidate();
    }

    public void onDestroyView() {
        if (imageNormal != null) {
            imageNormal.release();
        }
        if (imageChecked != null) {
            imageChecked.release();
        }
        if (imageError != null) {
            imageError.release();
        }
        imageNormal = null;
        imageChecked = null;
        imageError = null;
    }

    /**
     * 更新UI
     */
    private void updateDrawOnConfig() {
        if (sectionArray == null) {
            return;
        }
        invalidate();
    }

    private void updateDrawOnRowColumnConfig() {
        resetCheckedSections();
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        //重新计算所有格子
        sectionArray = calculateSection(width, height, column, row, getPaddingLeft(), getPaddingRight());
        invalidate();
    }

    private PixelMapHolder createPixelMapByRes(int resId) {
        try {
            ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
            decodingOptions.desiredPixelFormat = PixelFormat.ARGB_8888;
            ImageSource imageSource = ImageSource.create(getContext().getResourceManager().getResource(resId), new ImageSource.SourceOptions());
            return new PixelMapHolder(imageSource.createPixelmap(decodingOptions));
        } catch (IOException | NotExistException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 添加一个选中的格子
     */
    private void addCheckedSection(Section section) {
        if (sectionArrayChecked == null) {
            sectionArrayChecked = new ArrayList<>();
        }

        int lastSize = sectionArrayChecked.size();

        if (lastSize < 1) { //第一个
            sectionArrayChecked.add(section);//直接添加，结束。
            return;
        }
        int newSize = sectionArrayChecked.size();//获取最新个数

        if (gestureRepeatMode == GestureRepeatMode.DEFAULT) {//可重复
            //第2..个
            //
            //添加格子到选中的格子数组中
            Section lastSection = sectionArrayChecked.get(newSize - 1);
            if (lastSection.row != section.row || lastSection.column != section.column) {
                sectionArrayChecked.add(section);//如果已选中格子数组中最后一个格子不是当前手势选中的格子则添加进去
                //结束
            }
        } else if (gestureRepeatMode == GestureRepeatMode.NO_REPEAT) {
            //第2..个
            //
            for (int index = newSize - 1; index >= 0; index--) {
                Section lastSection = sectionArrayChecked.get(index);
                if (lastSection.row == section.row && lastSection.column == section.column) {
                    return;//如果已选中格子数组中最后一个格子是当前手势选中的格子则直接跳过判断
                }
            }
            sectionArrayChecked.add(section);//添加，结束
        }
    }

    /**
     * 重置所有已选中的格子
     */
    private void resetCheckedSections() {
        if (sectionArrayChecked != null) {
            sectionArrayChecked.clear();
        } else {
            sectionArrayChecked = new ArrayList<>();
        }
    }

    /**
     * 计算格子
     */
    private List<Section> calculateSection(int width, int height, int sectionColumn, int sectionRow, int startX, int startY) {
        int sectionWidth = width / sectionColumn; //单个宽度
        int sectionHeight = height / sectionRow; //单个高度
        ArrayList<Section> sectionArray = new ArrayList<>();
        for (int row = 0; row < sectionRow; row++) {
            for (int column = 0; column < sectionColumn; column++) {
                //分格子，计算格子起始，结束点
                Section section = new Section();
                section.totalRow = sectionRow;
                section.totalColumn = sectionColumn;
                section.row = row;//格子所在行
                section.column = column;//格子所在列
                //noinspection ConstantConditions
                section.startX = column * sectionWidth + startX; //左上X
                //noinspection ConstantConditions
                section.startY = row * sectionHeight + startY; //左上Y
                section.endX = section.startX + sectionWidth; //右下X
                section.endY = section.startY + sectionHeight; //右下Y
                section.centerX = (section.startX + section.endX) / 2; //格子中心点X
                section.centerY = (section.startY + section.endY) / 2; //格子中心点Y
                section.width = sectionWidth; //格子宽度
                section.height = sectionHeight; //格子高度

                section.contentStartX = section.startX + section.width / 4; //内容区域左上X
                section.contentStartY = section.startY + section.height / 4; //内容区域左上Y
                section.contentEndX = section.startX + section.width * 3 / 4; //内容区域右下X
                section.contentEndY = section.startY + section.height * 3 / 4; //内容区域右下Y
                sectionArray.add(section); //横向顺序排列

                HiLog.warn(TAG, "section:%{public}s", section.toString());
            }
        }
        return sectionArray;
    }

    /**
     * 计算触摸点所在格子
     */
    private Section calculatePointSection(float touchX, float touchY) {
        int size = this.sectionArray.size();
        Section sectionTouch = null;
        for (Section section : this.sectionArray) {
            if (touchX > section.contentStartX && touchX < section.contentEndX && touchY > section.contentStartY && touchY < section.contentEndY) {
                sectionTouch = section;
                break;
            }
        }
        return sectionTouch;
    }

    /**
     * 绘制所有格子-线条样式-初始状态
     */
    private void drawOriginalGesture(Canvas canvas, Paint paint) {
        //画格子
        if (sectionArray != null) {
            sectionArray.forEach(section -> drawSingleSectionLine(section, canvas, paint));
        }
    }

    /**
     * 绘制一个格子-线条样式-初始状态
     */
    private void drawSingleSectionLine(Section section, Canvas canvas, Paint paint) {
        paint.setColor(lineNormalColor);
        paint.setStrokeWidth(lineWidthSection);
        paint.setStyle(Paint.Style.STROKE_STYLE);
        canvas.drawCircle(new Point(section.centerX, section.centerY), (section.contentEndX - section.contentStartX) / 2, paint);
    }

    /**
     * 绘制所有格子-线条样式-选中状态
     */
    private void drawCheckedGesture(Canvas canvas, Paint paint) {
        //画格子
        if (sectionArrayChecked != null) {
            sectionArrayChecked.forEach(section -> drawSingleSectionLineChecked(section, canvas, paint));
        }
    }

    /**
     * 绘制一个格子-线条样式-选中状态
     */
    private void drawSingleSectionLineChecked(Section section, Canvas canvas, Paint paint) {
        if (gestureState == GestureState.ERROR) {
            paint.setColor(lineErrorColor);
        } else {
            paint.setColor(lineCheckedColor);
        }
        paint.setStyle(Paint.Style.FILL_STYLE);
        canvas.drawCircle(new Point(section.centerX, section.centerY), (section.contentEndX - section.contentStartX) / 4, paint);
    }

    /**
     * 绘制所有格子-图片样式-初始状态
     */
    private void drawOriginalGestureImage(Canvas canvas, Paint paint, PixelMapHolder pixelMapHolder) {
        if (pixelMapHolder == null) {
            return;
        }
        if (sectionArray != null) {
            sectionArray.forEach(section -> drawSingleSectionImage(canvas, paint, section, pixelMapHolder));
        }
    }

    /**
     * 绘制所有格子-图片样式-选中状态
     */
    private void drawCheckedGestureImage(Canvas canvas, Paint paint) {
        if (sectionArrayChecked != null) {
            sectionArrayChecked.forEach(section -> {
                if (gestureState == GestureState.ERROR) {
                    drawSingleSectionImage(canvas, paint, section, imageError);
                } else {
                    drawSingleSectionImage(canvas, paint, section, imageChecked);
                }
            });
        }
    }

    /**
     * 绘制一个格子-图片样式-任意图片
     */
    private void drawSingleSectionImage(Canvas canvas, Paint paint, Section section, PixelMapHolder pixelMapHolder) {
        //画布
        canvas.drawPixelMapHolderRect(pixelMapHolder, new RectFloat(section.contentStartX, section.contentStartY, section.contentEndX, section.contentEndY), paint);
    }

    /**
     * 绘制所有的格子到格子线段
     */
    private void drawAllSection2Section(Canvas canvas, Paint paint) {
        if (sectionArrayChecked != null) {
            int size = sectionArrayChecked.size();
            if (size < 2) return;
            for (int index = 0; index < size - 1; index++) {
                drawSinglePoint2Point(canvas, paint, sectionArrayChecked.get(index), sectionArrayChecked.get(index + 1));
            }
        }
    }

    /**
     * 绘制最后一个格子到手触摸点线段
     */
    private void drawSection2Touch(Canvas canvas, Paint paint, float touchX, float touchY) {
        if (sectionArrayChecked != null) {
            int size = sectionArrayChecked.size();
            if (size > 0) {
                Section lastSection = sectionArrayChecked.get(size - 1);
                drawSinglePoint2Point(canvas, paint, lastSection.centerX, lastSection.centerY, touchX, touchY);
            }
        }
    }

    /**
     * 绘制单个点到点线段
     */
    private void drawSinglePoint2Point(Canvas canvas, Paint paint, Section startSection, Section endSection) {
        drawSinglePoint2Point(canvas, paint, startSection.centerX, startSection.centerY, endSection.centerX, endSection.centerY);
    }

    /**
     * 处理手势回调
     */
    private void handleCallback(int code, List<Section> sections) {
        if (onDetectedListener != null) {
            if (code == DetectedListener.DETECT_SUCCESS) {
                int checkedSize = sections.size();
                if (checkedSize < minGestureCount) {//未达到最少手势点个数
                    onDetectedListener.onDetectedGesture(DetectedListener.DETECT_LIMITED_MIN, null);
                } else if (checkedSize > maxGestureCount) {
                    onDetectedListener.onDetectedGesture(DetectedListener.DETECT_LIMITED_MAX, null);
                } else {
                    onDetectedListener.onDetectedGesture(DetectedListener.DETECT_SUCCESS, sections);
                }
            } else {
                onDetectedListener.onDetectedGesture(code, null);
            }
        }
    }

    /**
     * 绘制单个点到点线段
     */
    private void drawSinglePoint2Point(Canvas canvas, Paint paint, float startX, float startY, float endX, float endY) {
        paint.setStrokeWidth(lineWidth);//线条的宽度
        if (gestureState == GestureState.ERROR) {
            paint.setColor(lineErrorColor);
        } else {
            paint.setColor(lineColor);
        }
        paint.setStrokeCap(Paint.StrokeCap.ROUND_CAP);//线条的端点样式
        paint.setStrokeJoin(Paint.Join.ROUND_JOIN);//线条的交点样式
        paint.setStyle(Paint.Style.STROKE_STYLE);
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        canvas.drawPath(path, paint);
    }

    /**
     * 实现 Component.EstimateSizeListener 抽象方法
     * <p>
     * 注意事项
     * 自定义组件测量出的大小需通过setEstimatedSize设置给组件，并且必须返回true使测量值生效。
     * setEstimatedSize方法的入参携带模式信息，可使用Component.EstimateSpec.getChildSizeWithMode方法进行拼接。
     * <p>
     * 测量模式
     * 测量组件的宽高需要携带模式信息，不同测量模式下的测量结果也不相同，需要根据实际需求选择适合的测量模式。
     * 表2 测量模式信息
     * 模式
     * <p>
     * 作用
     * <p>
     * UNCONSTRAINT
     * <p>
     * 父组件对子组件没有约束，表示子组件可以任意大小。
     * <p>
     * PRECISE
     * <p>
     * 父组件已确定子组件的大小。
     * <p>
     * NOT_EXCEED
     * <p>
     * 已为子组件确定了最大大小，子组件不能超过指定大小。
     */
    @Override
    public boolean onEstimateSize(int widthEstimateConfig, int heightEstimateConfig) {
        int width = Component.EstimateSpec.getSize(widthEstimateConfig);
        int height = Component.EstimateSpec.getSize(heightEstimateConfig);
        int min = Math.min(width, height);
        HiLog.warn(TAG, "width:%{public}d height:%{public}d min:%{public}d", width, height, min);
        setEstimatedSize(
                Component.EstimateSpec.getChildSizeWithMode(min, min, Component.EstimateSpec.NOT_EXCEED),
                Component.EstimateSpec.getChildSizeWithMode(min, min, Component.EstimateSpec.NOT_EXCEED));
        /*int paddingHorizontal = getPaddingLeft() + getPaddingRight();
        int paddingVertical = getPaddingTop() + getPaddingBottom();*/
        sectionArray = calculateSection(min, min, column, row, getPaddingLeft(), getPaddingTop());
        invalidate();
        return true;
    }

    /**
     * 实现 Component.TouchEventListener 抽象方法
     *
     * @noinspection StatementWithEmptyBody
     */
    @Override
    public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
        int eventAction = touchEvent.getAction();
        switch (eventAction) {
            case TouchEvent.PRIMARY_POINT_DOWN:

                MmiPoint position = touchEvent.getPointerPosition(touchEvent.getIndex());
                Section pathSection = calculatePointSection(position.getX(), position.getY()); //计算当前手势所在格子
                resetCheckedSections();//清空已选中格子
                if (pathSection != null) {
                    addCheckedSection(pathSection);//添加格子
                }
                gestureState = GestureState.NORMAL;//样式设置为初始状态
                sliding = false;
                invalidate();
                break;
            case TouchEvent.POINT_MOVE:

                int lastSize = sectionArrayChecked.size();
                MmiPoint movePosition = touchEvent.getPointerPosition(touchEvent.getIndex());
                moveX = movePosition.getX();
                moveY = movePosition.getY();
                Section moveSection = calculatePointSection(movePosition.getX(), movePosition.getY()); //计算当前手势所在格子
                if (moveSection != null) {
                    addCheckedSection(moveSection);
                }

                gestureState = GestureState.NORMAL;//样式设置为初始状态
                sliding = true;
                invalidate();
                break;
            case TouchEvent.PRIMARY_POINT_UP:
                sliding = false;
                List<Section> copy = new ArrayList<>(sectionArrayChecked);
                if (gestureDisappear == GestureDisappearMode.DISAPPEAR_ON_FINISH) {
                    resetCheckedSections();//清空已选中格子
                } else if (gestureDisappear == GestureDisappearMode.APPEAR_ONLY_ERROR_ON_FINISH) {
                    int checkedSize = sectionArrayChecked.size();
                    if (checkedSize >= minGestureCount && checkedSize <= maxGestureCount) {//已达到限制手势点个数
                        resetCheckedSections();//清空已选中格子
                    }
                } else if (gestureDisappear == GestureDisappearMode.APPEAR_ON_FINISH) {
                    //do noting
                }
                invalidate();

                handleCallback(DetectedListener.DETECT_SUCCESS, copy);
                break;
            case TouchEvent.CANCEL:
                resetCheckedSections();//清空已选中格子
                sliding = false;
                invalidate();

                handleCallback(DetectedListener.DETECT_CANCEL, null);
                break;
            default:
                //HiLog.warn(TAG, "Touch:action:%{public}d", eventAction);
                break;
        }
        return true;
    }

    /**
     * 实现 Component.DrawTask 抽象方法
     */
    @Override
    public void onDraw(Component component, Canvas canvas) {
        if (gestureDrawOrder == GestureDrawOrder.SECTION_TOP) {
            drawAllSection2Section(canvas, mPaint);//绘制格子到格子路径
            if (sliding) {
                drawSection2Touch(canvas, mPaint, moveX, moveY);//绘制最后一个格子到触摸点
            }
        }
        if (gestureStyle == GestureStyle.LINE) {
            drawOriginalGesture(canvas, mPaint);//绘制初始状态格子
            drawCheckedGesture(canvas, mPaint);//绘制选中格子
        } else {
            drawOriginalGestureImage(canvas, mPaint, imageNormal);
            drawCheckedGestureImage(canvas, mPaint);
        }
        if (gestureDrawOrder == GestureDrawOrder.LINE_TOP) {
            drawAllSection2Section(canvas, mPaint);//绘制格子到格子路径
            if (sliding) {
                drawSection2Touch(canvas, mPaint, moveX, moveY);//绘制最后一个格子到触摸点
            }
        }
    }

    @FunctionalInterface
    public interface DetectedListener {
        int DETECT_SUCCESS = 0;//识别成功，手势点数正常
        int DETECT_LIMITED_MIN = 10;//识别失败，未达到手势点最小个数限制
        int DETECT_LIMITED_MAX = 11;//识别失败，超到手势点最大个数限制
        int DETECT_CANCEL = 20;//识别失败，意外取消
        int DETECT_START = 30;//识别开始

        /**
         * @param code            结果码
         * @param detectedGesture code为DETECT_SUCCESS时不为null,其他为null
         */
        void onDetectedGesture(int code, List<Section> detectedGesture);
    }

    public static class Section implements Serializable {
        private int totalRow;
        private int totalColumn;
        private int row;//格子所在行
        private int column;//格子所在列
        private float startX;//左上X位置
        private float startY;//左上Y位置
        private float endX;//右下X
        private float endY; //右下Y
        private float centerX;//格子中心点X
        private float centerY; //格子中心点Y
        private float width;//格子宽度
        private float height;//格子高度
        private float contentStartX;//内容区域左上X
        private float contentStartY;//内容区域左上Y
        private float contentEndX;//内容区域右下X
        private float contentEndY;//内容区域右下Y

        public int getTotalRow() {
            return totalRow;
        }

        public int getTotalColumn() {
            return totalColumn;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public float getStartX() {
            return startX;
        }

        public void setStartX(float startX) {
            this.startX = startX;
        }

        public float getStartY() {
            return startY;
        }

        public void setStartY(float startY) {
            this.startY = startY;
        }

        public float getEndX() {
            return endX;
        }

        public void setEndX(float endX) {
            this.endX = endX;
        }

        public float getEndY() {
            return endY;
        }

        public void setEndY(float endY) {
            this.endY = endY;
        }

        public float getCenterX() {
            return centerX;
        }

        public void setCenterX(float centerX) {
            this.centerX = centerX;
        }

        public float getCenterY() {
            return centerY;
        }

        public void setCenterY(float centerY) {
            this.centerY = centerY;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public float getContentStartX() {
            return contentStartX;
        }

        public void setContentStartX(float contentStartX) {
            this.contentStartX = contentStartX;
        }

        public float getContentStartY() {
            return contentStartY;
        }

        public void setContentStartY(float contentStartY) {
            this.contentStartY = contentStartY;
        }

        public float getContentEndX() {
            return contentEndX;
        }

        public void setContentEndX(float contentEndX) {
            this.contentEndX = contentEndX;
        }

        public float getContentEndY() {
            return contentEndY;
        }

        public void setContentEndY(float contentEndY) {
            this.contentEndY = contentEndY;
        }

        @Override
        public String toString() {
            return "Section{" +
                    "row=" + row +
                    ", column=" + column +
                    ", startX=" + startX +
                    ", startY=" + startY +
                    ", endX=" + endX +
                    ", endY=" + endY +
                    ", centerX=" + centerX +
                    ", centerY=" + centerY +
                    ", width=" + width +
                    ", height=" + height +
                    ", contentStartX=" + contentStartX +
                    ", contentStartY=" + contentStartY +
                    ", contentEndX=" + contentEndX +
                    ", contentEndY=" + contentEndY +
                    '}';
        }
    }

    /**
     * 根据手势点阵生成序列字符串。
     *
     * @param sections 手势数据
     * @return 横向排列，起始0，顺序+1拼接成字符序列
     */
    public static String section2String(List<Section> sections) {
        if (sections != null) {
            StringBuilder builder = new StringBuilder();
            for (Section section : sections) {
                builder.append((section.row * section.getTotalColumn() + section.column));
            }
            return builder.toString();
        }
        return "";
    }

    /**
     * 判断是否是简单手势：转折点个数是否小于传入值。实际转折点比预期转折点小则为简单手势。反之，则为复杂手势。
     *
     * @param sections       手势点阵数据
     * @param inflexionCount 预期转折点个数
     * @return 是否是简单手势
     */
    public static boolean isSimpleGesture(List<Section> sections, int inflexionCount) {
        if (sections != null) {
            if (sections.size() > 2) {
                List<Float> slopes = new ArrayList<>();
                for (int i = 0; i < sections.size() - 1; i++) {
                    Section sectionS = sections.get(i);
                    Section sectionE = sections.get(i + 1);
                    float slop;
                    if (sectionS.row == sectionE.row) {
                        slop = 0;
                    } else if (sectionS.column == sectionE.column) {
                        slop = -1;
                    } else {
                        slop = (float) ((sectionE.row - sectionS.row) / (sectionE.column - sectionS.column));
                    }
                    if (i == 0) {
                        slopes.add(slop);
                    } else {
                        int newSize = slopes.size();
                        Float lastInflexion = slopes.get(newSize - 1);
                        if (lastInflexion != slop) {
                            slopes.add(slop);
                        }
                    }
                }
                //转折点个数-1 = 斜率个数
                return slopes.size() < (inflexionCount + 1);
            }
        }
        return true;
    }
}
