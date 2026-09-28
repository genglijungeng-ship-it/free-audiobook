# 【保姆级教程】把「免费听书」打包成手机能装的 APK
> 老板您好！这份教程是给完全不懂技术的人看的，照着一步步点就行，全程大约 20 分钟。
## 一、准备工作（只需做一次）
### 1. 下载安装 Android Studio
- 打开网址：https://developer.android.com/studio
- 点击绿色的 **Download Android Studio** 按钮
- 下载完成后双击安装，一路点「Next」→「Install」→「Finish」，全部用默认选项即可
- 安装包约 1GB，请耐心等待
### 2. 首次打开 Android Studio
- 第一次打开会提示下载 SDK，点 **Next** → **Accept** → **Finish**，等它下载完（约 10-20 分钟）
## 二、导入本项目
1. 打开 Android Studio，点 **Open**（打开）
2. 在弹窗中找到本项目文件夹 `android_app`，选中后点 **OK**
3. 等待右下角进度条跑完（首次会下载依赖，约 5-10 分钟）
4. 如果弹出提示「Gradle 需要更新」，点 **Update** 即可
## 三、打包成 APK
1. 顶部菜单点 **Build**（构建）
2. 选择 **Build Bundle(s) / APK(s)** → **Build APK(s)**
3. 等待右下角出现绿色提示 **APK(s) generated successfully**
4. 点击提示里的 **locate** 链接，就能找到生成的 APK 文件
   - 位置一般在：`android_app\app\build\outputs\apk\debug\app-debug.apk`
## 四、安装到手机
1. 把 `app-debug.apk` 通过微信/QQ/数据线传到手机
2. 手机上点击这个文件安装
3. 如果提示「禁止安装未知来源应用」，去手机 **设置 → 安全 → 允许安装未知来源应用**，打开后再装
4. 安装完成，桌面会出现「免费听书」图标，点开就能用！
## 五、常见问题
**Q：提示找不到 SDK？**
A：Android Studio 里点 **File → Settings → Appearance & Behavior → System Settings → Android SDK**，勾选 Android 14.0 (API 34)，点 Apply 下载。
**Q：打包报错 "Unsupported class file major version"？**
A：说明 Java 版本不对。Android Studio 自带 JDK，请在 **File → Settings → Build → Build Tools → Gradle → Gradle JDK** 里选择 **jbr-17**。
**Q：手机装不上？**
A：先卸载旧版本再装；或者手机设置里允许「未知来源」。
**Q：还是没有声音？**
A：检查手机是否静音；安卓系统自带TTS引擎，一般无需额外安装。若提示缺少语音数据，去 **设置 → 语言和输入法 → 文字转语音** 里下载中文语音包。