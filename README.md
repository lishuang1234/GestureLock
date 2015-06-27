# GestureLock

##GestureLockView手势滑锁


 实现思路:
 
 1.自定义View,根据模式绘制其颜色及状态
 
 2.自定义ViewFroup继承RelativeLayout,获取配置View参数,最多尝试次数,
 
 在onMeasure()初始化位参数,设置子View位置
 
 在onTouchEvent()根据手势状态绘制连接线条,更新子View状态,重绘.
 
 
 

大神教程[传送门](http://blog.csdn.net/lmj623565791/article/details/36236113)

演示效果图：

![image](https://github.com/lishuang1234/GestureLock/blob/master/GestureLock/screenshort/1.gif)
