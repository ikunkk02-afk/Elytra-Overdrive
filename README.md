# Elytra Overdrive

Elytra Overdrive 是一个面向 Minecraft 1.21.1 Fabric 的高速鞘翅模组。当前版本加入数据驱动的“超载（Overdrive）”附魔，并用服务端权威的速度上限实现可配置高速飞行。

## 当前功能

- “超载”一级附魔，仅支持原版鞘翅。
- 可从附魔台、附魔书和铁砧获得或应用，也可使用 `/enchant @s elytra-overdrive:overdrive`。
- 玩家可选择 `1.0–20.0` 倍倍率；实际倍率始终受服务端最大倍率限制。
- 有限目标速度与逐 tick 加速度，不会反复乘当前速度造成指数级加速。
- 轻量本地云粒子与平滑 FOV 反馈，均可单独关闭。
- 可配置的高速额外耐久消耗，遵循耐久附魔，创造模式默认不额外消耗。
- 传送、换维度、重生、停止滑翔、卸下或损坏鞘翅时清理临时高速状态。

## 运行要求

- Minecraft：`1.21.1`
- Fabric Loader：`0.19.3` 或更高的兼容版本
- Java：`21`
- 必需前置：Fabric API `0.116.13+1.21.1`、owo-lib `0.12.15.4+1.21`
- 可选前置：Mod Menu `11.0.4`（仅用于提供配置按钮）

Mod Menu 未安装时模组本体仍可启动。Sodium、Lithium、Jade、JEI 等本地测试模组不是 Elytra Overdrive 的依赖。

## 使用方法

将“超载”附魔应用到原版鞘翅后开始滑翔即可启用。没有附魔的鞘翅和倍率为 `1.0` 时保持原版速度行为。烟花火箭仍可正常推进，但最终速度不会突破高速上限。

安装 Mod Menu 后，可在模组列表中点击 Elytra Overdrive 的配置按钮。没有 Mod Menu 时也可编辑 `config/elytra-overdrive.json5`。远程服务器的高速开关、最大倍率和耐久规则以服务器配置为准；例如客户端选择 `20.0`、服务器上限为 `5.0` 时，实际倍率只有 `5.0`。

## 开发状态与计划

当前阶段已完成高速附魔、配置同步、服务端飞行控制、粒子、FOV 和耐久消耗。以下内容尚未实现：

- TNT 空中轰炸
- 三叉戟穿刺方块
- 自定义音效、音爆、镜头震动、模型或鞘翅贴图

技术细节和测试清单见 [超载附魔实施记录](docs/implementation/overdrive-enchantment.md)。

## License

本项目使用 [MIT License](LICENSE)，Copyright (c) 2026 寿云。
