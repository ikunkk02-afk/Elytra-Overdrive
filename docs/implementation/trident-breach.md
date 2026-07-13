# 三叉戟破阵实施记录

## 数据驱动附魔

`elytra-overdrive:breach` 延续 Minecraft 1.21.1 已验证的数据驱动附魔方式，不注册 Java `Enchantment` 实例。`breach.json` 最大等级 3、槽位 `mainhand`，`supported_items` 指向只包含 `minecraft:trident` 的专用标签，并加入原版 `non_treasure` 池，因此支持附魔台、附魔书、铁砧和 `/enchant`。Java 侧只保存动态 `ResourceKey<Enchantment>` 并从注册表读取实际等级。

## 触发与扫掠算法

服务端仅在玩家存活、非旁观、滑翔、主手为仍可用的附魔原版三叉戟且功能开启时计算。真实速度必须达到 `minimumBreachSpeed`，归一化速度与视线方向点积必须至少为 `0.65`。零向量、NaN 和 Infinity 不会归一化或触发。

首次进入有效状态只记录身体中心位置。后续 tick 从 previous position 到 current position 加 `0.75` 格前向缓冲，以 `0.35` 格从近到远采样。速度方向与稳定参考轴叉乘得到 right，再由 right 与速度叉乘得到 up；接近垂直飞行时切换参考轴，避免截面退化。

- 破阵 I：中心，最多 1 格截面。
- 破阵 II：中心、上下左右，最多 5 格截面。
- 破阵 III：完整 3×3，最多 9 格截面。

候选位置使用有序集合去重。`maximumBreachBlocksPerTick` 限制检查的唯一位置总数，包括空气和被保护方块；采样循环本身还限制为 `min(1024, maximum×8)` 次，极端有限速度也不会产生无界循环。超过上限时本 tick 从近到远停止处理。

## 方块保护、权限与掉落

处理每个候选前检查世界边界、合法 Y 和 `ServerLevel.isLoaded`，不会强制加载区块。空气、负硬度、超过等级硬度上限和 `elytra-overdrive:breach_protected` 标签中的方块跳过。标签默认含基岩、屏障、末地传送门及框架、末地折跃门、下界传送门、命令方块系列、结构方块/结构空位、拼图方块、光源方块和移动活塞。默认情况下任何存在 BlockEntity 的位置也会跳过。

实际破坏调用原版 `ServerPlayerGameMode.destroyBlock`。短生命周期 `BreachBreakContext` 只匹配当前线程、玩家 UUID 和 BlockPos；服务端 Mixin 在该上下文内允许创造模式三叉戟进入原版入口，并抑制原生正确工具掉落，避免重复。Fabric `PlayerBlockBreakEvents`、原版出生点/管理员方块限制以及注入 `destroyBlock` 的第三方逻辑仍有机会取消破坏。取消或失败时不掉落、不扣耐久。

`breachDropsBlocks=true` 时，成功破坏后用破坏前 BlockState/BlockEntity 和无附魔下界合金镐评估方块战利品表，并遵守 `doBlockDrops`；关闭时不生成普通战利品。只监听客户端挖掘包、距离检查或自定义入口的领地模组可能仍需专门适配。需要特殊工具组件或特殊交互的模组方块也可能产生不同掉落。

## 硬度与耐久

等级硬度上限分别为 3.0、10.0 和 50.0；`hardness < 0` 永远禁止。每个实际成功破坏的方块按以下公式消耗真实主手三叉戟：

```text
damageCost = max(1, ceil(blockHardness × breachDurabilityMultiplier))
```

调用标准 `hurtAndBreak`，因此耐久附魔和物品损坏回调正常参与。创造模式不消耗；损坏或不再可用后立即停止本 tick 剩余穿刺。

## 配置

- `enableTridentBreach=true`
- `minimumBreachSpeed=1.2`，范围 0.3–10.0
- `maximumBreachBlocksPerTick=32`，范围 1–128
- `breachDurabilityMultiplier=1.0`，范围 0.1–10.0
- `breachDropsBlocks=true`
- `protectBlockEntitiesFromBreach=true`

全部是 owo `OVERRIDE_CLIENT` 服务端同步字段。

## 游戏内手动测试清单

1. 没有 Breach 附魔的三叉戟不能破坏方块。
2. 站在地面拿附魔三叉戟不能破坏方块。
3. 低速滑翔不能触发。
4. 达到最低速度后可以触发。
5. Breach I 为 1×1。
6. Breach II 为十字范围。
7. Breach III 为完整 3×3。
8. 高速穿过厚墙不会漏掉中间方块。
9. 基岩不会被破坏。
10. 末地传送门框架不会被破坏。
11. 默认不会破坏箱子等 BlockEntity。
12. 石头和黑曜石的耐久消耗明显不同。
13. 三叉戟损坏后立即停止。
14. 超载高速飞行时不会一 tick 破坏无限方块。
15. 未加载区块不会被强制加载。
16. 玩家切换物品后立即停止。
17. 传送或切换维度后不会从旧位置扫到新位置。

## 组合测试清单

1. 普通鞘翅可以轰炸。
2. 普通鞘翅达到速度后可以破阵。
3. Overdrive 鞘翅可以高速轰炸。
4. Overdrive 鞘翅可以高速破阵。
5. 原有 Overdrive 飞行、粒子和耐久逻辑完全正常。
