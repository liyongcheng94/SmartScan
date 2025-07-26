# SmartScan APK 发布指南

## 准备工作

### 1. 配置密钥库密码

编辑项目根目录下的 `keystore.properties` 文件，替换为您的实际密码：

```properties
storePassword=您刚才设置的密钥库密码
keyPassword=您刚才设置的密钥密码
keyAlias=smartscan
storeFile=../smartscan-release-key.keystore
```

### 2. 更新版本信息

在发布新版本前，请在 `app/build.gradle.kts` 中更新：

- `versionCode`: 每次发布都需要递增（当前为 1）
- `versionName`: 版本号字符串（当前为 "1.0"）

## 构建发布版 APK

### 方法一：使用 Android Studio

1. 选择菜单 `Build` → `Generate Signed Bundle / APK`
2. 选择 `APK`
3. 选择您的密钥库文件 `smartscan-release-key.keystore`
4. 输入密钥库密码和密钥密码
5. 选择 `release` 构建变体
6. 点击 `Finish`

### 方法二：使用命令行

#### 构建 Release APK

```bash
./gradlew assembleRelease
```

#### 构建 Debug APK（用于测试）

```bash
./gradlew assembleDebug
```

## APK 输出位置

构建完成后，APK 文件将位于：

- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`

## 发布前检查清单

### 1. 功能测试

- [ ] 扫描功能正常
- [ ] 批量扫描正常
- [ ] 历史记录功能正常
- [ ] 数据导出功能正常
- [ ] 权限申请正常

### 2. 性能测试

- [ ] 应用启动速度
- [ ] 扫描响应速度
- [ ] 内存使用情况
- [ ] 电池消耗情况

### 3. 兼容性测试

- [ ] 不同 Android 版本测试（最低 API 26）
- [ ] 不同屏幕尺寸测试
- [ ] 不同设备厂商测试

### 4. 安全检查

- [ ] 代码混淆已启用
- [ ] 资源压缩已启用
- [ ] 密钥库文件安全保存
- [ ] 敏感信息已移除

## 版本发布流程

### 1. 准备发布

```bash
# 更新版本号
# 运行测试
./gradlew test

# 清理项目
./gradlew clean

# 构建 Release 版本
./gradlew assembleRelease
```

### 2. 验证 APK

```bash
# 检查 APK 内容
./gradlew analyzeReleaseBundle

# 安装测试
adb install app/build/outputs/apk/release/app-release.apk
```

### 3. 发布到应用商店

- Google Play Store
- 华为应用市场
- 小米应用商店
- 腾讯应用宝
- 360 手机助手
- 等其他应用市场

## 注意事项

1. **密钥库安全**：

   - 密钥库文件需要安全保存，丢失后无法更新应用
   - 不要将密钥库文件上传到版本控制系统
   - 建议制作备份

2. **版本控制**：

   - 每次发布都需要增加 `versionCode`
   - `versionName` 应该遵循语义化版本规范

3. **混淆配置**：

   - 检查 `proguard-rules.pro` 文件
   - 确保不会混淆必要的类和方法

4. **权限检查**：
   - 确保 `AndroidManifest.xml` 中的权限声明正确
   - 测试运行时权限申请

## 常见问题

### Q: 构建失败怎么办？

A: 检查以下项目：

- 密钥库路径是否正确
- 密码是否正确
- 依赖是否都已下载
- SDK 版本是否正确

### Q: 如何减小 APK 大小？

A: 可以尝试：

- 启用代码混淆和资源压缩
- 移除未使用的资源
- 使用 WebP 格式图片
- 启用 APK 分包

### Q: 如何调试发布版本？

A: 可以创建一个 staging 构建类型：

```kotlin
buildTypes {
    create("staging") {
        initWith(getByName("release"))
        isDebuggable = true
        applicationIdSuffix = ".staging"
    }
}
```
