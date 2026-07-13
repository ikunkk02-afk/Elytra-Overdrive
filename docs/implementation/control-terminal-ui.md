# 航空控制终端实现

## 集成方式

`OverdriveConfigModel` 不再使用 `@Modmenu` 自动生成纵向列表。客户端初始化时调用：

```java
ConfigScreenProviders.register(ElytraOverdrive.MOD_ID, OverdriveControlScreen::new);
```

owo-lib 的可选 Mod Menu 插件读取这个 Provider。项目不导入 Mod Menu API，也没有添加强制 Mod Menu 依赖。所有 Screen、Minecraft 客户端读取和 owo UI 组件均在 `src/client`。

## 代码结构

- `OverdriveControlScreen`：Screen 生命周期、响应式外壳、保存/默认/退出确认。
- `ControlTerminalTheme`：深蓝黑背景、面板、边框、青蓝和橙色常量。
- `ControlPanelComponents`：通用卡片、行、标签、按钮和开关。
- `FlightSettingsPanel`：倍率、烟花个人开关和来源状态。
- `BombingSettingsPanel`：只读轰炸规则和 SAFE/ARMED 本地装备提示。
- `BreachSettingsPanel`：只读破阵规则与方块实体保护强调。
- `VisualSettingsPanel`：视觉预设、三个效果开关和减少动态效果。
- `ServerPolicyPanel`：只读服务端规则和连接角色。
- `FlightStatusPanel`：服务端确认状态与本地只读速度/耐久。

无客户端导入的纯状态类位于 common，便于普通 JUnit 测试：

- `ConfigDraft`
- `ControlScreenState`
- `ConfigPermissionState`
- `NavigationSection`
- `ControlValueFormatter`

## 草稿与保存

打开界面时，`ConfigDraft.from(OverdriveConfig)` 复制当前玩家字段。滑条和开关只修改草稿。

保存时：

1. 倍率裁剪到 `1.0–200.0`。
2. 只调用玩家字段的生成 setter。
3. 调用 `OverdriveConfig.save()`。
4. 发送一次倍率和烟花偏好。
5. 显示 40 tick 的保存成功提示。

恢复默认需要二次点击，只恢复玩家倍率、烟花偏好和视觉字段。服务器字段没有 setter 入口出现在 UI 中。存在未保存修改时，返回或 ESC 需要二次确认。

## 响应式布局

逻辑界面宽度 `>=900`：

```text
导航栏 | 可滚动当前页面 | 实时状态
```

逻辑界面宽度 `<900`：

```text
导航栏 | 实时状态卡 + 可滚动当前页面
```

页脚不在滚动区域内，三个按钮按 33/33/34 比例占用可用宽度。854×480 或较高 GUI Scale 下，右栏不会挤压内容，中央区域保持可滚动，返回按钮始终位于页脚。

`resize` 保留当前 `ConfigDraft` 和导航状态。页面切换状态使用四个客户端 tick 的轻量进度，不使用 Shader、模糊或反射。

## 权限锁定与实时状态

烟花个人开关只有在以下任一服务端确认状态为真时可编辑：

- `allowHeldFireworkOverdrive`
- `localOwnerOverride`

否则显示普通文本 `LOCKED` 和“此功能需要服务器管理员启用”。本地保存值即使为 true，也不能改变禁用状态。

服务端状态 Payload 更新策略后，打开的 Screen 在客户端 tick 中重建权限相关组件。右侧状态使用服务端确认的 active、source 和 effectiveMultiplier；当前速度和鞘翅耐久只是可靠的本地只读观察。

主菜单没有玩家或世界时显示 `NO FLIGHT DATA`，不会访问空玩家引用。

## 兼容性限制

- 完整的文字换行、悬停和按钮尺寸仍需要在实际 GUI Scale 与语言环境中人工检查。
- 界面未使用 Sodium/Iris 渲染扩展，目标是兼容常见优化模组；整合包仍应做人工冒烟测试。
- ARMED 是同步轰炸总开关加本地滑翔/装备条件的准备提示，不声称服务端已经生成 TNT。
