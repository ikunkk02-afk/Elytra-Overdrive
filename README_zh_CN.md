# Elytra Overdrive

面向 Fabric 的高速鞘翅飞行、空中轰炸、三叉戟破阵与高级飞行控制模组。

[English](README.md)

- Minecraft 1.21.1
- Fabric
- Java 21
- MIT License

## 目录

- [概述](#概述)
- [功能](#功能)
- [前置要求](#前置要求)
- [安装](#安装)
- [使用方法](#使用方法)
- [配置](#配置)
- [服务器管理](#服务器管理)
- [从源码构建](#从源码构建)
- [开发环境](#开发环境)
- [常用 Gradle 命令](#常用-gradle-命令)
- [项目结构](#项目结构)
- [兼容性](#兼容性)
- [常见问题](#常见问题)
- [许可证](#许可证)

## 概述

Elytra Overdrive 是一个面向 Minecraft 1.21.1 的 Fabric 模组，用于扩展原版鞘翅玩法。它提供超载高速飞行附魔、鞘翅附魔台支持、可选的手持烟花高速模式、TNT 空中轰炸、破阵三叉戟附魔、高级航空控制终端和可配置的飞行视觉效果。

核心玩法由服务端负责最终判定。服务端会验证高速飞行资格、实际倍率、轰炸、方块破坏、耐久消耗、权限和安全上限；多人服务器中的客户端不能绕过服务器设置。

## 功能

### Overdrive / 超载附魔

- `Overdrive`（`超载`）是用于 Elytra 的附魔，可通过附魔台、附魔书和铁砧获得或应用。
- 玩家滑翔时按照个人选择的倍率加速。
- 最终倍率永远不会超过服务器上限。
- 玩家标准倍率最高为 `100×`；主动确认性能警告后可解锁 `100×～200×` 实验性极速范围。
- 没有超载附魔的鞘翅保持原版飞行行为，除非可选的手持烟花模式获得授权并由玩家开启。

### 鞘翅附魔台支持

本模组允许原版鞘翅参与正常的附魔台流程：

1. 将鞘翅放入附魔台。
2. 放入青金石。
3. 选择正常生成的附魔选项。

该功能不会强制所有附魔支持鞘翅。宝藏附魔仍遵循原版规则，也不会保证每次都获得超载。

### 手持烟花高速模式

手持烟花高速模式是第二种可选的高速飞行来源，只有满足对应条件时才会激活：

- 玩家正在使用可飞行的鞘翅滑翔。
- 主手或副手持有 Firework Rocket。
- 玩家个人选项已开启。
- 服务器允许该功能，或者玩家是集成式单人服务器的世界拥有者。

仅手持烟花不会消耗物品。主动右键使用烟花仍然遵循原版的消耗和推进规则。玩家个人选项和专用服务器权限默认都是 `false`，并且服务端倍率上限始终生效。

### TNT 空中轰炸

滑翔时主手持 Flint and Steel，副手持 TNT：

- 单击右键投下一颗已点燃的 TNT。
- 长按右键按照服务端配置的间隔连续投弹。
- 松开右键停止。

生存模式每次成功生成炸弹会消耗一颗 TNT 和一点打火石耐久；创造模式不消耗这两种物品。TNT 生成、引信、速度、物品消耗和频率限制全部由服务端控制。

### Breach / 破阵

`Breach`（`破阵`）是仅用于原版 Trident 的三级附魔：

| 等级 | 截面范围 |
|---|---|
| 破阵 I | 中心一格（`1×1`） |
| 破阵 II | 五格十字形 |
| 破阵 III | 完整 `3×3` |

玩家必须正在鞘翅滑翔、主手持有破阵三叉戟，并达到配置的最低速度。服务端使用预测式路径扫描和碰撞前清障，并设置每 tick 预算，不会为了破阵强制加载区块。

不可破坏方块和管理员方块会受到保护，Block Entity 默认也受保护。每个成功破坏方块造成的三叉戟耐久消耗随方块硬度增加。

### 飞行视觉效果

客户端飞行反馈包括：

- 翼尖气流（Wingtip Trails）
- 速度线（Speed Lines）
- 音爆环（Sonic Boom Ring）

`Performance`、`Balanced` 和 `Cinematic` 三种预设使用不同的粒子硬上限。各个效果可以单独关闭，Reduce Motion 会减少粒子活动并关闭速度线与音爆环。视觉设置不会改变服务端实际速度。

### 航空控制终端

自定义控制终端包含五个页面：

- Flight / 飞行
- Bombing / 轰炸
- Breach / 破阵
- Visual / 视觉
- Server / 服务器

界面显示服务端确认的飞行状态、激活来源、玩家倍率、服务器上限、当前速度和鞘翅耐久。玩家和视觉设置可在本地修改；轰炸、破阵和服务器策略对普通玩家只读。

Mod Menu 可以方便地打开控制终端，但不是必需依赖。

### 实验性极速模式

- 玩家标准范围：最高 `100×`
- 实验性极速范围：最高 `200×`
- 服务器默认最大倍率：`100×`
- 服务器可配置最大倍率：`200×`
- 实验性极速模式默认关闭

`100×` 以上可能显著提高区块加载压力、世界生成压力、内存占用、网络流量、客户端帧时间和服务器 Tick 负载。实际体验取决于硬件、地图是否预生成、服务器性能、区块加载速度和网络环境。`200×` 属于实验性范围，不保证在所有服务器上稳定。

## 前置要求

### 必需

| 组件 | 版本 |
|---|---|
| Minecraft | `1.21.1` |
| Fabric Loader | `0.19.3` 或更新的兼容版本 |
| Fabric API | `0.116.13+1.21.1` 或更新的兼容版本 |
| owo-lib | `0.12.15.4+1.21` 或更新的兼容版本 |
| Java | `21` 或更新的兼容运行时 |

### 可选

| 组件 | 版本 | 用途 |
|---|---|---|
| Mod Menu | `11.0.4` 或更新的兼容版本 | 方便打开配置界面 |

多人游戏必须在客户端和服务器双方安装 Elytra Overdrive。专用服务器不需要 Mod Menu；缺少 Mod Menu 不会导致模组本体无法加载。

## 安装

### 客户端

1. 安装 Minecraft 1.21.1。
2. 安装对应版本的 Fabric Loader。
3. 下载适用于 Minecraft 1.21.1 的 Fabric API 和 owo-lib。
4. 可选安装 Mod Menu。
5. 将 Elytra Overdrive 正式 JAR 和必需依赖放入 Minecraft 的 `mods` 目录：
   - Windows：`%APPDATA%\.minecraft\mods`
   - Linux：`~/.minecraft/mods`
   - macOS：`~/Library/Application Support/minecraft/mods`
6. 启动 Fabric 游戏配置。

请使用正式构建的发布 JAR，不要把仓库源码 ZIP、sources JAR 或开发 JAR 放入 `mods`。

### 专用服务器

1. 安装 Minecraft 1.21.1 Fabric Server。
2. 将 Fabric API、owo-lib 和 Elytra Overdrive 正式 JAR 放入服务器 `mods/`。
3. 启动一次服务器以生成配置。
4. 停止服务器，并在需要时编辑 `config/elytra-overdrive.json5`。
5. 重新启动服务器。

专用服务器不需要 Mod Menu。服务器设置始终优先于玩家个人设置。

## 使用方法

### 使用超载附魔高速飞行

1. 获得鞘翅。
2. 通过附魔台、附魔书或铁砧获得超载。
3. 装备鞘翅并开始滑翔。
4. 在控制终端中选择大于 `1.0×` 的倍率。

### 使用手持烟花高速飞行

1. 服务器管理员启用手持烟花模式，或者玩家进入自己的单人世界。
2. 开启个人手持烟花高速选项。
3. 在任意一只手中持有 Firework Rocket。
4. 装备可用的鞘翅并开始滑翔。

### 空中轰炸

1. 装备鞘翅并开始滑翔。
2. 主手持 Flint and Steel。
3. 副手持 TNT。
4. 单击或长按右键投弹。

### 破阵

1. 获得三叉戟并附魔破阵。
2. 鞘翅滑翔时将三叉戟持在主手。
3. 达到服务器配置的最低速度。
4. 朝向可破坏方块飞行。

## 配置

配置文件为 `config/elytra-overdrive.json5`。玩家设置可以在控制终端中编辑；服务端权威字段由服务器同步，对普通玩家显示为只读。

### 飞行与视觉设置

| 选项 | 默认值 | 说明 |
|---|---:|---|
| `playerSelectedMultiplier` | `2.0` | 玩家请求的飞行倍率，持久化范围 `1.0–200.0`，之后仍受服务端上限限制。 |
| `enableHeldFireworkOverdrive` | `false` | 玩家是否请求使用手持烟花高速模式。 |
| `enableExperimentalExtremeSpeed` | `false` | 解锁带警告的 `100×～200×` 本地界面范围，不代表获得服务端权限。 |
| `showHighSpeedParticles` | `true` | 本地高速粒子总开关。 |
| `visualPreset` | `BALANCED` | 粒子预算预设：`PERFORMANCE`、`BALANCED` 或 `CINEMATIC`。 |
| `enableWingtipTrails` | `true` | 服务端确认高速飞行时显示翼尖气流。 |
| `enableSpeedLines` | `true` | 当前预设允许时显示外围速度线。 |
| `enableSonicBoomRing` | `true` | 当前预设允许时启用阈值触发的音爆环。 |
| `reduceMotion` | `false` | 降低动态粒子，并关闭速度线与音爆环。 |

### 服务端飞行设置

| 选项 | 默认值 | 说明 |
|---|---:|---|
| `enableHighSpeedFlight` | `true` | 所有高速飞行的服务端总开关。 |
| `allowHeldFireworkOverdrive` | `false` | 是否允许服务器玩家请求手持烟花模式。 |
| `serverMaximumMultiplier` | `100.0` | 新配置文件的服务端最终倍率上限，范围 `1.0–200.0`；已有配置值保持不变。 |
| `extraDurabilityDamage` | `true` | 高速飞行期间是否额外消耗鞘翅耐久。 |
| `extraDurabilityIntervalTicks` | `40` | 额外耐久尝试间隔，范围 `10–200` tick。 |
| `elytraEnchantability` | `10` | 鞘翅在附魔台中的附魔品质，范围 `1–30`。 |

### 轰炸设置

| 选项 | 默认值 | 说明 |
|---|---:|---|
| `enableBombing` | `true` | 启用 TNT 空中轰炸。 |
| `bombingIntervalTicks` | `12` | 长按使用键时两颗炸弹之间的服务端间隔，范围 `4–100` tick。 |
| `bombFuseTicks` | `80` | 投放 TNT 的引信时间，范围 `20–200` tick。 |
| `bombHorizontalInertia` | `0.70` | TNT 继承玩家水平速度的比例，范围 `0.0–1.5`。 |

### 破阵设置

| 选项 | 默认值 | 说明 |
|---|---:|---|
| `enableTridentBreach` | `true` | 启用破阵系统。 |
| `minimumBreachSpeed` | `1.2` | 触发所需真实速度，单位为格/tick，范围 `0.3–10.0`。 |
| `maximumBreachBlocksPerTick` | `32` | 每位玩家每个服务端 tick 检查的唯一位置上限，范围 `1–128`。 |
| `breachDurabilityMultiplier` | `1.0` | 根据方块硬度缩放耐久消耗，范围 `0.1–10.0`。 |
| `breachDropsBlocks` | `true` | 获准破坏后按照战利品表生成掉落。 |
| `protectBlockEntitiesFromBreach` | `true` | 保护容器和其他 Block Entity。 |

## 服务器管理

专用服务器默认关闭手持烟花高速权限。以下已实现命令需要权限等级 4：

```mcfunction
/elytraoverdrive firework_mode true
/elytraoverdrive firework_mode false
```

- `true`：允许玩家开启自己的手持烟花选项。
- `false`：撤销该权限。
- 修改成功后会保存 owo 配置，并立即同步所有在线玩家。
- 集成式单人服务器的本地世界拥有者即使在服务器选项为 `false` 时也拥有授权；通过 LAN 加入的其他玩家不会获得该豁免。

其他服务端规则通过 `config/elytra-overdrive.json5` 管理，并按照 owo-lib 的配置行为生效。

## 从源码构建

项目使用自带的 Gradle Wrapper，不需要单独安装 Gradle。请安装 Git 和任意 JDK 21 发行版，例如 Eclipse Temurin、OpenJDK 或 Microsoft Build of OpenJDK。

### 1. 检查 Java

```bash
java -version
```

输出应显示 Java 21。

### 2. 克隆仓库

```bash
git clone https://github.com/ikunkk02-afk/Elytra-Overdrive.git
cd Elytra-Overdrive
```

### 3. Windows 构建

```powershell
.\gradlew.bat clean build
```

### 4. Linux 或 macOS 构建

```bash
chmod +x gradlew
./gradlew clean build
```

### 5. 构建产物

正式 remapped JAR 位于：

```text
build/libs/elytra-overdrive-<version>.jar
```

版本 1.0.0 对应 `build/libs/elytra-overdrive-1.0.0.jar`。不要发布 `*-sources.jar` 或任何开发/测试 JAR。

### 6. 运行测试

Windows：

```powershell
.\gradlew.bat clean test
```

Linux/macOS：

```bash
./gradlew clean test
```

### 7. 完整验证

Windows：

```powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Linux/macOS：

```bash
./gradlew clean test
./gradlew build
```

## 开发环境

IntelliJ IDEA 和 VS Code 都可以使用，也可以选择其他支持 Gradle 与 Java 的 IDE。

1. 克隆仓库。
2. 以 Gradle 项目打开或导入。
3. 为项目和 Gradle toolchain 选择 JDK 21。
4. 允许 Gradle 下载依赖。
5. 等待 Fabric Loom 完成开发环境准备。

启动开发客户端：

```powershell
.\gradlew.bat runClient
```

启动开发服务器：

```powershell
.\gradlew.bat runServer
```

Linux/macOS 请把 `gradlew.bat` 替换为 `./gradlew`。开发服务器第一次运行时可能需要接受 Minecraft EULA。

## 常用 Gradle 命令

| Windows 命令 | 用途 |
|---|---|
| `.\gradlew.bat build` | 构建并测试模组 |
| `.\gradlew.bat test` | 运行单元测试和资源契约测试 |
| `.\gradlew.bat clean` | 删除构建产物 |
| `.\gradlew.bat runClient` | 启动开发客户端 |
| `.\gradlew.bat runServer` | 启动开发服务器 |
| `.\gradlew.bat tasks` | 列出可用 Gradle 任务 |

Linux/macOS 请将 `.\gradlew.bat` 替换为 `./gradlew`。

## 项目结构

| 路径 | 用途 |
|---|---|
| `src/main/java` | 公共与服务端权威玩法、网络、配置、命令和 Mixin |
| `src/client/java` | 客户端界面、输入、粒子和视觉状态 |
| `src/main/resources` | Fabric metadata、数据驱动附魔、标签、Mixin 配置、图标和语言资源 |
| `src/client/resources` | 客户端专用 Mixin 配置 |
| `src/test/java` | 纯逻辑和资源契约单元测试 |
| `docs/implementation` | 详细实现记录和手动测试清单 |

## 兼容性

- 支持 Minecraft 1.21.1、Fabric 和 Java 21。
- 不支持 Forge 或 NeoForge，也不承诺兼容其他 Minecraft 大版本。
- 多人游戏需要客户端与服务器双方安装，因为服务端会执行必需的协议检查。
- 服务端最终控制速度、权限、轰炸、方块破坏、耐久和安全上限。
- 客户端界面和视觉代码位于 Fabric client source set，专用服务器不会加载这些类。
- 自定义配置界面不依赖 Shader，设计目标是在常见 Fabric 渲染优化模组环境中保持可用，但不保证兼容所有模组或整合包。
- 改写服务端最终飞行物理阶段或拦截方块破坏的模组可能需要额外兼容性测试。

## 常见问题

### 游戏启动时崩溃

- 确认 Minecraft 正好是 1.21.1。
- 确认正在使用 Java 21。
- 确认已经安装适用于 Minecraft 1.21.1 的 Fabric API 和 owo-lib。
- 确认 Fabric Loader 满足最低版本。
- 删除 `mods` 中重复或版本错误的 JAR。

### 模组没有出现在 Mod Menu 中

Mod Menu 是可选依赖。没有 Mod Menu 不代表 Elytra Overdrive 没有加载。请确认模组 JAR 位于正确的 `mods` 目录，并检查游戏日志。安装兼容版本的 Mod Menu 后可以方便地打开控制终端。

### 手持烟花高速模式不生效

- 确认服务器已允许该模式，或者你是集成式服务器拥有者。
- 在控制终端中开启玩家个人选项。
- 使用可飞行的鞘翅开始滑翔。
- 在主手或副手持有 Firework Rocket。
- 选择大于 `1.0×` 的倍率。

### 高速飞行没有激活

- 确认鞘翅拥有超载，或者已经满足全部手持烟花条件。
- 确认个人倍率大于 `1.0×`。
- 确认服务端 `enableHighSpeedFlight=true`。
- 确认鞘翅没有损坏到无法飞行。

### 破阵无法破坏方块

- 确认主手三叉戟拥有破阵。
- 确认玩家正在滑翔、达到最低速度，并且视线方向与运动方向基本一致。
- 检查方块是否受保护、是否包含 Block Entity、是否超过等级硬度上限，或是否位于出生点保护范围。
- 确认 `enableTridentBreach=true`。

### 构建失败

首先检查 Java 21：

```bash
java -version
```

然后使用 stack trace 重试。

Windows：

```powershell
.\gradlew.bat clean build --stacktrace
```

Linux/macOS：

```bash
./gradlew clean build --stacktrace
```

## 许可证

本项目使用 [MIT License](LICENSE)。Copyright (c) 2026 寿云。
