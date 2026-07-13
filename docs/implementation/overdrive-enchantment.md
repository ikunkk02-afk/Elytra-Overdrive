# 超载鞘翅附魔实施记录

## 项目与范围

Elytra Overdrive 面向 Minecraft 1.21.1、Fabric Loader 0.19.3、Java 21 和 Mojang 官方映射。本记录只描述“超载”附魔、owo-lib 配置、安全高速飞行、轻量视觉反馈和额外耐久消耗；后续 TNT 轰炸与三叉戟破阵分别记录在独立实施文档中。

## 实际修改文件

### 构建与元数据

- `.github/workflows/build.yml`：CI 固定为 Java 21。
- `.gitignore`：排除构建、运行、IDE、日志、崩溃、环境变量和密钥文件。
- `build.gradle`、`gradle.properties`：加入 owo-lib、Mod Menu 本地运行依赖和 JUnit 5；Java toolchain 固定为 21。
- `fabric.mod.json`：Fabric API 与 owo 为必需依赖，Mod Menu 为建议依赖；区分 main/client 入口点和 mixin。
- `LICENSE`：保留远端已有的 2026 寿云 MIT License。

### 主源码

- `ElytraOverdrive`：加载配置并初始化网络、飞行、轰炸和破阵处理器。
- `config/OverdriveConfigModel`：owo-lib 配置模型。
- `enchantment/OverdriveEnchantments`：附魔资源键、查询和鞘翅条件辅助方法。
- `flight/FlightVelocity`：不依赖 Minecraft 的有限三维速度值。
- `flight/FlightSpeedController`：倍率校验、目标速度、推进和硬上限纯逻辑。
- `flight/FlightSessionState`：在线会话选择倍率、激活状态和耐久计时。
- `flight/OverdriveFlightHandler`：服务端世界 tick 检查、速度应用、耐久与状态清理。
- `network/RequiredClientPayload`：配置阶段协议版本和客户端安装检查。
- `network/SelectedMultiplierC2SPayload`：玩家选择倍率的 C2S 提交。
- `network/OverdriveStateS2CPayload`：服务端最终倍率和激活状态的 S2C 通知。
- `network/OverdriveNetworking`：payload 注册、配置阶段验证和 C2S 接收。
- `mixin/ServerGamePacketListenerImplMixin`：只在六参数 teleport 入口清理各系统运行状态。

### 客户端源码

- `client/ElytraOverdriveClient`：客户端网络与视觉初始化。
- `client/ClientOverdriveNetworking`：协议检查、倍率发送和服务端状态接收。
- `client/ClientOverdriveState`：仅保存服务端确认的视觉状态。
- `client/OverdriveVisuals`：本地、限量的高速粒子 tick。
- `client/mixin/ClientCommonPacketListenerImplAccessor`：协议不兼容时取得配置连接并断开。

### 数据与测试

- `data/elytra-overdrive/enchantment/overdrive.json`：数据驱动附魔定义。
- `data/elytra-overdrive/tags/item/enchantable/overdrive.json`：仅包含 `minecraft:elytra` 的专用标签。
- `data/minecraft/tags/enchantment/non_treasure.json`：将附魔加入原版非宝藏池。
- `assets/elytra-overdrive/lang/en_us.json`、`zh_cn.json`：附魔、描述、标签、配置和断开提示翻译。
- `FlightSpeedControllerTest`、`FlightSessionStateTest`、`ResourceContractTest`：纯逻辑与资源契约测试。

## 附魔注册方案

Minecraft 1.21.1 的附魔通过注册表数据包加载，不注册旧式 Java `Enchantment` 子类。`overdrive.json` 的最大等级为 1、权重为 2、铁砧成本为 4、槽位为 `chest`，效果表为空。`supported_items` 指向专用物品标签，该标签只有 `minecraft:elytra`，所以胸甲和其他物品不受支持。

附魔被加入 `minecraft:non_treasure`。原版的附魔台、随机附魔书和相关非宝藏池会通过该标签关系识别它；命令和附魔书直接使用注册表 ID `elytra-overdrive:overdrive`。Java 辅助类只持有动态 `ResourceKey<Enchantment>` 并在运行时从注册表查找。

`primary_items` 显式指向同一个仅含 `minecraft:elytra` 的专用标签。附魔台候选仍由原版 `#minecraft:in_enchanting_table`、`primary_items`、`supported_items`、等级费用和互斥集合共同过滤，不硬编码添加 Overdrive。

## 鞘翅附魔台支持

Minecraft 1.21.1 没有后续版本的 `DataComponents.ENCHANTABLE`。`ItemStack#isEnchantable` 已会把单堆叠且具有耐久的 Elytra 视为可附魔；真正导致附魔台没有选项的是 `EnchantmentHelper#getEnchantmentCost` 和 `selectEnchantment` 读取 `Item#getEnchantmentValue()`，而 `ElytraItem` 未覆盖该方法，原版返回值为 0。

`ItemMixin` 在 `Item#getEnchantmentValue()` 的 `RETURN` 注入，仅当目标对象严格等于 `Items.ELYTRA` 时，将结果替换为服务器同步的 `elytraEnchantability`。默认值为 10，运行时安全限制为 1–30。其他物品保留原返回值；没有修改默认 Data Component，也没有修改或重写 `EnchantmentMenu`、`EnchantmentHelper`、附魔书、铁砧或磨石。

附魔台继续由服务器生成费用和最终附魔，并正常消耗经验等级与青金石。原版 `unbreaking` 的 supported-items durability 标签已经包含 Elytra；`mending` 不在 `#minecraft:in_enchanting_table`，因此仍不能通过普通附魔台获得。其他附魔也必须同时通过自身 primary/supported items、费用和互斥规则。

## 配置与同步方案

owo 配置模型分为玩家与服务器两组：

- 玩家：倍率默认 `2.0`、粒子默认开启。
- 服务器：高速总开关默认开启、最大倍率默认 `10.0`、额外耐久默认开启、间隔默认 `40 tick`。
- 鞘翅附魔台：附魔能力默认 `10`，范围 `1–30`，由服务器同步。

倍率范围为 `1.0–20.0`，耐久间隔范围为 `10–200 tick`。服务器字段使用 owo `OVERRIDE_CLIENT`；远程客户端接收服务器值并不能覆盖安全限制，单人世界仍可本地管理。

配置阶段服务器用 `RequiredClientPayload` 检查客户端是否声明对应频道，不支持时在进入世界前断开。客户端进入 play 阶段后发送 `SelectedMultiplierC2SPayload(double)`。服务端会话初始倍率为 `1.0`，只有收到有限且位于 `[1,20]` 的值才更新；NaN、Infinity、负数和越界值均拒绝并保留上次安全值。最终倍率为：

```text
finalMultiplier = clamp(min(playerSelectedMultiplier, serverMaximumMultiplier), 1.0, 20.0)
```

客户端只收到 `OverdriveStateS2CPayload(finalMultiplier, active)` 用于视觉效果，不在客户端应用任何速度。

## 速度算法

服务端在 `ServerTickEvents.END_WORLD_TICK` 处理玩家，因此原版鞘翅俯冲、抬头、转向、空气阻力和烟花推进先执行。只有滑翔中、胸甲槽为可用原版鞘翅、鞘翅有“超载”、服务器启用、玩家非旁观且最终倍率大于 1 时才进入高速逻辑。

```text
targetSpeed = 0.8 × finalMultiplier blocks/tick
acceleration = min(0.04 × (finalMultiplier - 1), 0.8) blocks/tick²
```

每 tick 沿当前有限速度方向追加加速度；当前速度接近零时才使用有限视线方向。结果统一经过目标速度硬上限，不会把当前速度重复乘倍率，也不会因烟花产生无限加速。倍率为 1 时完全旁路。非法速度向量会恢复为零；从激活转为失效时，残余速度最多保留 `1.5 blocks/tick`。

## Mixin 注入点

- 服务端只有 `ServerGamePacketListenerImpl#teleport(double, double, double, float, float, Set<RelativeMovement>)` 的 `HEAD` 注入。传送是清除运行状态的明确边界；仅在传送前确实处于超载激活状态时归零速度，因此不会改变普通玩家的原版传送行为，普通移动逻辑也不被覆盖。

## 临时状态与耐久

会话 Map 以在线玩家 UUID 为键，不保存玩家对象。退出和停服时删除，重生、换维度、传送和停止激活时清理运行状态。卸下鞘翅、入水导致停止滑翔、鞘翅损坏或关闭功能会立即停止高速处理，无法通过快速穿脱叠加倍率。

额外耐久仅在倍率大于 1 且高速激活时计时。达到配置间隔后调用原版 `ItemStack.hurtAndBreak(1, player, EquipmentSlot.CHEST)`，因此耐久附魔照常参与；创造模式的 `instabuild` 玩家不额外消耗。停止高速会重置计时。

## 视觉反馈

粒子只在本地当前玩家、服务端确认激活、倍率大于 1 且速度超过 `0.9 blocks/tick` 时出现。每 4 tick 最多生成 2 个原版云粒子，不由服务端全局广播。

所有 Screen、渲染、粒子和客户端网络代码都位于 client source set。

## 已运行的自动验证

- `gradlew.bat clean build`：基线和各功能阶段成功。
- `gradlew.bat test --tests '*FlightSpeedControllerTest'`：纯速度逻辑测试成功。
- `gradlew.bat test --tests '*FlightSessionStateTest'`：会话安全默认与更新测试成功。
- `gradlew.bat clean build`：20 项 JUnit 测试全部通过。
- `gradlew.bat runServer`：专用服务器到达 `Done` 并正常停止。
- `gradlew.bat runClient`：客户端完成资源加载并进入主界面阶段，无 Elytra Overdrive 加载错误。
- Gradle `dependencyInsight`：项目解析 owo-lib `0.12.15.4+1.21`。

## 游戏内手动测试清单

以下行为仍需在真实游戏会话中逐项操作确认：

1. 普通鞘翅保持原版速度。
2. 带超载附魔的鞘翅启用高速功能。
3. 倍率为 1.0 时保持原版行为。
4. 倍率为 2.0 时明显加速但操作正常。
5. 倍率为 10.0 时不会无限加速或产生 NaN。
6. 卸下鞘翅后功能立即停止。
7. 鞘翅损坏后功能停止。
8. 停止滑翔后高速粒子立即停止。
9. 重新进入世界后配置仍然存在。
10. 没有 Mod Menu 时模组仍能启动。
11. 专用服务器不会加载客户端类。
12. 客户端设置 20 倍、服务器上限 5 倍时实际只能使用 5 倍。
13. 烟花推进不会绕过最大速度限制。
14. 高速飞行会按配置额外消耗耐久。
15. 普通 Elytra 可以放入附魔台并出现三档选项。
16. 不同书架等级会改变附魔费用，点击后正常消耗经验和青金石。
17. 可随机获得 Overdrive、耐久或其他真正兼容的普通附魔。
18. 不会出现锋利、效率等不支持 Elytra 的附魔。
19. 经验修补等宝藏附魔仍不从普通附魔台出现。
20. 已附魔 Elytra 可通过磨石移除非诅咒附魔，并保留正常铁砧/附魔书行为。
21. 附魔后的 Overdrive Elytra 高速飞行正常，Breach 三叉戟附魔台候选不受影响。

## 已知限制与尚未实现

- 自动测试只覆盖纯 Java 数值与资源契约，无法在普通 JUnit 中模拟完整 `ServerPlayer` 飞行。
- 粒子只表示当前本地玩家的服务端确认状态，附近其他玩家不会看到这条粒子尾迹。
- 当前网络协议要求客户端和服务端都安装本模组，不提供 vanilla 客户端兼容模式。
- TNT 空中轰炸与三叉戟破阵已在后续阶段实现；自定义音效、音爆、镜头震动、自定义模型和贴图仍不在当前范围。
