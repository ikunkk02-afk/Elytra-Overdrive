# 高速飞行视觉效果 V2 实现

## 状态来源

粒子仅在 `ClientOverdriveState` 收到服务端确认的以下状态后运行：

- `active=true`
- `effectiveMultiplier>1.0`
- `activationSource!=NONE`

客户端看到手中有烟花不等于已经获得高速授权。烟花模式被服务器拒绝时不会播放高速效果。

## 纯强度算法

`VisualIntensity.fromSpeed` 接收实际速度、有效倍率、确认激活状态、视觉预设和 Reduce Motion。

以下输入返回 `ZERO`：

- 非有限或负速度；
- 未激活；
- 非有限倍率；
- 倍率小于等于 `1.0`；
- 空预设。

有效输入使用从约 `0.7` 到 `5.0 blocks/tick` 的 smoothstep 曲线。速度提高时轨迹长度和速度线数量平滑增加，但所有计数都受预设硬上限约束。

| 预设 | 普通粒子每 tick 硬上限 | 速度线 | 音爆环 |
|---|---:|---|---|
| PERFORMANCE | 8 | 关闭 | 关闭 |
| BALANCED | 20 | 开启 | 开启 |
| CINEMATIC | 32 | 开启 | 开启 |

Reduce Motion 将普通预算限制到 6，最多保留每侧一个翼尖点，并关闭速度线和音爆环。

## 飞行方向基向量

`FlightBasis` 将玩家 look direction 归一化为 forward，然后计算 right 和 up。forward 接近世界上方向时，使用 X 轴作为备用参考，避免叉积退化。零向量和非有限向量使用稳定的 `(0,0,1)` forward。

这使翼尖、速度线和圆环随玩家朝向旋转，而不是固定在世界坐标轴。

## 三层粒子

### 翼尖气流

以玩家中部偏后位置为中心，沿 right 向量左右偏移约 `0.95` 格作为近似翼尖。每侧 DustColorTransition 点沿 `-forward` 排列，颜色从青蓝过渡到近白色。

### 速度线

起点从飞行轴周围半径 `0.85–1.60` 的环带随机取样，因此不会集中在准心中心。每条线由 3–4 个 Dust 点沿 `-forward` 组成。普通粒子共享同一个每 tick 预算。

### 音爆环

`SonicBoomState` 参数：

- 触发阈值：`4.0 blocks/tick`
- 重置阈值：`3.2 blocks/tick`
- 冷却：`60 tick`
- 实际圆环粒子：`24`
- 独立硬上限：`28`

触发后立即 disarm。冷却结束本身不会重新武装；速度还必须先下降到重置阈值。圆环使用 right/up 平面，因而大致垂直于 forward。它只调用客户端 `addParticle`，没有伤害、击退、方块修改或网络广播。

## 已知限制

- 翼尖位置是朝向近似值，不跟踪玩家模型的鞘翅动画骨骼。
- 原版 Dust 粒子自行完成生命周期淡出，模组没有自定义 alpha Shader。
- 第一人称遮挡、第三人称翼尖位置、Sodium 和 Iris 兼容性需要实际游戏画面确认。
