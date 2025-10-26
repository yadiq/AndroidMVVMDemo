# AndroidMVVMDemo
AndroidMVVM的精简版本，快速开发框架
使用MVVM框架+Kotlin语言

## 图标
1. mipmap文件夹只存放启动图标icon
2. Android手机屏幕标准                    对应图标尺寸标准      屏幕密度       比例
   xxxhdpi 3840*2160                         192*192             640          16
   xxhdpi 1920*1080                          144*144             480          12
   xhdpi  1280*720                            96*96              320           8

## 屏幕适配
1. 主要适配屏幕信息：1080x1920 px ,360x640 dp (对角线2202.91px)
2. density（dp密度，1dp上有多少个像素）=1080px / 360dp = 3 px/dp
3. densitydpi（屏幕像素密度，简称dpi，表示1英寸上对应有多少个像素）=160 * density= 480（因为第一款Android设备 160dpi)
	(屏幕尺寸=对角线像素数/densitydpi=4.59英寸)
4. 注意.xml文件预览仅支持部分densitydpi（例如：400 420 440 480等）

## 当前版本选择(NDK27.0代码调试速度慢，所以降低版本)
AGP8.3.2 Gradle8.4 NDK25.2.9519653 JDK17
Kotlin2.1.20(K2编译器)
C++版本 C++14
CMake版本 3.22.1

## 最新版本选择(最佳性能,最新版本的速度更快)
AGP8.10.1 Gradle8.11.1 NDK27.0.12077973 JDK17
Kotlin2.1.20(K2编译器)
C++版本 C++14
CMake版本 3.30.3

## 旧版本选择(稳定兼容,长期支持版本)
AGP7.4.2 Gradle7.6 NDK23.1 JDK11
Kotlin1.9.22
C++版本 C++14
CMake版本 3.22.1

## 工具说明
AGP   构建系统层。构建系统插件，协调所有编译任务
CMake 构建工具层。负责生成本地 (Native) C/C++ 的编译规则
NDK   编译工具链层。编译 C/C++ 代码


## AGP发布信息
[官方文档](https://developer.android.google.cn/build/releases/past-releases/agp-8-10-0-release-notes)
1. AGP8.10
   子版本：8.10.0 8.10.1
   默认版本：Gradle8.11.1 SDK35.0 NDK27.0 JDK17

2. AGP8.3
   子版本：8.3.0 8.3.1 8.3.2
   默认版本：Gradle8.4 SDK34.0 NDK25.1 JDK17

2. AGP8.0
   子版本：8.0.0 8.0.1 8.0.2
   默认版本：Gradle8.0 SDK30.0 NDK25.1 JDK17

3. AGP7.4
   子版本：7.4.0 7.4.1 7.4.2
   默认版本：Gradle7.5 SDK30.0 NDK23.1 JDK11

4. AGP7.0
   子版本：7.0.0 7.0.1
   默认版本：Gradle7.0.2 SDK30.0 NDK21.4 JDK11

## 版本对应关系
[官方文档](https://developer.android.google.cn/build/releases/gradle-plugin?hl=zh-cn#updating-gradle)
1. AGP 与 Gradle
+ AGP版本   最低Gradle版本
+ 8.13     8.13
+ 8.12     8.13
+ 8.11     8.13
+ 8.10     8.11.1  #当前选择8.10.1
  ...
+ 8.6      8.7
+ 8.5      8.7
  ...
+ 8.0      8.0
+ 7.4      7.5
  ...
+ 7.0      7.0
+ 4.2.0+   6.7.1

2. AGP 与 Android Studio 的版本对应关系
   AS版本                              支持AGP版本
   Narwhal 4 Feature Drop | 2025.1.4  4.0-8.13
   Narwhal 3 Feature Drop | 2025.1.3  4.0-8.13
   Narwhal Feature Drop | 2025.1.2	   4.0-8.12
   Narwhal | 2025.1.1	               3.2-8.11
   Meerkat Feature Drop | 2024.3.2	   3.2-8.10 #当前版本 2024.3.2 Patch 1
   Meerkat | 2024.3.1	               3.2-8.9
   Ladybug Feature Drop | 2024.2.2	   3.2-8.8