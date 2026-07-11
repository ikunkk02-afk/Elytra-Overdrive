# Elytra Overdrive

Elytra Overdrive 是一个面向 Minecraft 1.21.1 Fabric 的鞘翅战斗与高速飞行模组。它提供数据驱动的“超载（Overdrive）”和“破阵（Breach）”附魔，以及完全由服务端控制的 TNT 空中轰炸。

## 当前功能

- “超载”一级附魔，仅支持原版鞘翅。
- 可从附魔台、附魔书和铁砧获得或应用，也可使用 `/enchant @s elytra-overdrive:overdrive`。
- 玩家可选择 `1.0–20.0` 倍倍率；实际倍率始终受服务端最大倍率限制。
- 有限目标速度与逐 tick 加速度，不会反复乘当前速度造成指数级加速。
- 轻量本地云粒子与平滑 FOV 反馈，均可单独关闭。
- 可配置的高速额外耐久消耗，遵循耐久附魔，创造模式默认不额外消耗。
- 传送、换维度、重生、停止滑翔、卸下或损坏鞘翅时清理临时高速状态。
- 普通或超载鞘翅飞行时，主手打火石、副手 TNT 可右键投弹；单击单发，长按连续轰炸。
- “破阵”最高三级，仅支持原版三叉戟；高速滑翔时按 1 格、十字或 3×3 截面扫掠击碎前方方块。
- 轰炸和破阵均不要求鞘翅拥有“超载”，并由服务端验证速度、物品、耐久、权限和频率。

## 运行要求

- Minecraft：`1.21.1`
- Fabric Loader：`0.19.3` 或更高的兼容版本
- Java：`21`
- 必需前置：Fabric API `0.116.13+1.21.1`、owo-lib `0.12.15.4+1.21`
- 可选前置：Mod Menu `11.0.4`（仅用于提供配置按钮）

Mod Menu 未安装时模组本体仍可启动。Sodium、Lithium、Jade、JEI 等本地测试模组不是 Elytra Overdrive 的依赖。

## 三个独立系统

### Overdrive / 超载

将“超载”附魔应用到原版鞘翅后开始滑翔即可启用。没有附魔的鞘翅和倍率为 `1.0` 时保持原版速度行为。烟花火箭仍可正常推进，但最终速度不会突破高速上限。

### Bombing / 空中轰炸

滑翔时主手拿打火石、副手拿 TNT：快速点击右键立即投下一颗，长按右键则在首颗后按服务端间隔连续投弹，松开立即停止。TNT 在玩家身体下方偏后生成，并继承配置比例的水平速度。生存模式每颗消耗一枚副手 TNT 和一点打火石耐久；创造模式默认不消耗。

未滑翔、没有副手 TNT 或服务器关闭轰炸时，打火石保持完整原版右键行为。客户端只发送 START/STOP 输入状态，实际投弹与冷却由服务器决定。

### Breach / 破阵

“破阵”只支持原版三叉戟，可从附魔台、附魔书、铁砧获得，也可使用 `/enchant @s elytra-overdrive:breach 3`。玩家滑翔速度达到服务器阈值且移动方向与视线基本一致时生效：

- 破阵 I：中心 `1×1`。
- 破阵 II：中心加上下左右，十字形最多 5 格。
- 破阵 III：完整 `3×3`，最多 9 格截面。

系统以 0.35 格步长扫描上一 tick 到当前位置及少量前向缓冲，避免高速穿墙漏掉中间方块。每 tick 检查数量有服务器硬上限；负硬度、管理/传送门核心方块永远跳过，默认也保护所有 BlockEntity。

三个系统互相独立：普通鞘翅可以轰炸或在速度达标后破阵；超载鞘翅只是更容易维持高速。轰炸要求主手打火石，破阵要求主手三叉戟，因此两种攻击方式天然互斥。

## 配置

安装 Mod Menu 后，可在模组列表中点击 Elytra Overdrive 的配置按钮。没有 Mod Menu 时也可编辑 `config/elytra-overdrive.json5`。远程服务器的高速开关、最大倍率和耐久规则以服务器配置为准；例如客户端选择 `20.0`、服务器上限为 `5.0` 时，实际倍率只有 `5.0`。

空中轰炸服务器配置：

- `enableBombing=true`
- `bombingIntervalTicks=12`（4–100）
- `bombFuseTicks=80`（20–200）
- `bombHorizontalInertia=0.70`（0.0–1.5）

三叉戟破阵服务器配置：

- `enableTridentBreach=true`
- `minimumBreachSpeed=1.2`（0.3–10.0 格/tick）
- `maximumBreachBlocksPerTick=32`（1–128）
- `breachDurabilityMultiplier=1.0`（0.1–10.0）
- `breachDropsBlocks=true`
- `protectBlockEntitiesFromBreach=true`

## 兼容性说明

- 破阵通过原版 `ServerPlayerGameMode.destroyBlock` 执行，并保留 Fabric 方块破坏事件、原版出生点/管理员方块限制以及注入该入口的保护模组逻辑。
- 只监听客户端挖掘数据包、距离检查或自定义专用入口的领地模组可能仍需专门适配。
- 开启掉落时使用方块战利品表和无附魔下界合金镐作为虚拟采掘工具；要求特殊工具组件或特殊交互的模组方块可能产生不同掉落。
- 默认 BlockEntity 保护用于避免容器内容和重要 NBT 被高速删除；关闭该选项属于服务器管理员主动承担的数据风险。

技术细节和测试清单见 [超载附魔](docs/implementation/overdrive-enchantment.md)、[TNT 空中轰炸](docs/implementation/aerial-bombing.md) 和 [三叉戟破阵](docs/implementation/trident-breach.md)。

## License

本项目使用 [MIT License](LICENSE)，Copyright (c) 2026 寿云。
