# 【免安装】不用装任何软件，云端自动打包 APK 教程
> 老板您好！这条路**完全免费、完全合法、电脑上什么都不用装**，全程网页点点点，约 30 分钟拿到 APK。
## 一、注册 GitHub 账号（免费）
1. 打开 https://github.com
2. 点右上角 **Sign up**，用邮箱注册一个账号（就像注册微信一样简单）
## 二、上传项目（网页操作，不用装软件）
1. 登录后，点右上角 **+** → **New repository**
2. 名字随便填，比如 `free-audiobook`，选 **Public**（公开），点 **Create repository**
3. 进入仓库页面后，点 **uploading an existing file**（上传文件）
4. 把 `android_app` 文件夹里的**所有文件和文件夹**拖进网页上传区
   - ⚠️ 注意：要把 `.github` 文件夹（含 workflows）也一起传上去，这是自动打包的关键
   - 如果网页上传文件夹不方便，可以先把 android_app 打压成 zip 再逐个上传，或安装 GitHub Desktop 客户端拖拽上传
5. 点 **Commit changes** 确认上传
## 三、等待自动打包（约 5-10 分钟）
1. 上传完成后，点仓库顶部的 **Actions** 标签
2. 会看到一条「自动打包APK」正在运行（黄色圆点 = 进行中）
3. 等它变成 **绿色对勾 ✅**
## 四、下载 APK
1. 点进那条绿色对勾的记录
2. 拉到页面底部 **Artifacts** 区域
3. 点击 **免费听书-APK** 下载（是个 zip，解压出来就是 apk）
4. 把 apk 传到手机安装，搞定！
## 五、以后想改代码再打包？
把改过的文件在网页上重新上传覆盖，Actions 会自动再打一次包，重复第三、四步即可。
## 常见问题
**Q：GitHub 打不开或很慢？**
A：多刷新几次，或换个时间段；国内网络偶尔波动属正常。
**Q：Actions 提示需要启用？**
A：进入 Actions 标签页，如果有个绿色按钮「I understand my workflows, go ahead and enable them」，点它启用即可。