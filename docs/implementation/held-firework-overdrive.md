# 手持烟花高速模式实现

## 目标与边界

手持烟花是一条新的高速资格来源，不是新的速度系统。它复用 `OverdriveFlightHandler`、`FlightSpeedController`、服务端速度上限、额外耐久计时和现有停用速度限制。

模组只读取两只手中的 `ItemStack`，不调用 `shrink`、`hurtAndBreak` 或烟花使用方法，因此被动手持不会消耗烟花。模组也没有为烟花注册 `UseItemCallback` 或覆盖 `FireworkRocketItem`；主动右键继续经过原版消耗和推进路径。

## 授权判断

`FlightActivationResolver` 接收四个服务端事实：

```text
hasOverdriveEnchantment
playerPreferenceEnabled
holdingFireworkRocket
serverPolicyAllowsPlayer
```

烟花来源成立条件：

```text
playerPreferenceEnabled
AND holdingFireworkRocket
AND serverPolicyAllowsPlayer
```

最终来源枚举：

- `NONE`
- `ENCHANTMENT`
- `HELD_FIREWORK`
- `BOTH`

最终高速激活还要求玩家存活、不是旁观者、正在滑翔、胸甲槽是可用鞘翅、`enableHighSpeedFlight=true`，并且服务端裁剪后的倍率大于 `1.0`。

## 服务端策略与单人世界

服务端字段：

```text
allowHeldFireworkOverdrive=false
```

它使用 `@Sync(Option.SyncMode.OVERRIDE_CLIENT)` 参与初始 owo 服务端配置覆盖。运行中管理员命令改变策略时，模组自己的确认状态 Payload 会立即同步新策略。

单人世界豁免使用 Minecraft 1.21.1 Mojang 映射中的：

```java
player.server.isSingleplayerOwner(player.getGameProfile())
```

没有使用 `!server.isDedicatedServer()`，因此 LAN 普通加入者仍然受服务端字段约束。

## 玩家偏好与生命周期

本地玩家字段：

```text
enableHeldFireworkOverdrive=false
```

新服务端 `FlightSessionState` 的偏好始终从 false 开始。客户端在 JOIN 后发送一次保存的偏好，并在控制终端保存时再次发送。断开连接会清理客户端策略、来源和有效倍率；服务端断开会移除会话。

本地配置可以保留 true，但服务器拒绝时：

- `acceptedHeldFireworkPreference=false`；
- 来源不能成为 `HELD_FIREWORK`；
- UI 开关锁定；
- 粒子不运行。

## 网络 Payload 与协议

协议版本由 2 提升为 3。配置阶段仍使用 `RequiredClientPayload` 检查精确协议版本，不匹配时明确断开。

客户端到服务端：

```text
HeldFireworkPreferenceC2SPayload(boolean enabled)
```

该包只表达玩家偏好。服务端不接受客户端提供的手持物、来源、授权或倍率结论。

服务端到客户端 `state_v2`：

```text
double effectiveMultiplier
boolean active
int activationSourceOrdinal
boolean allowHeldFireworkOverdrive
boolean localOwnerOverride
boolean acceptedHeldFireworkPreference
```

客户端验证倍率和枚举范围；非法值会重置确认状态。

## 管理员命令

```mcfunction
/elytraoverdrive firework_mode <true|false>
```

处理器使用 `CommandSourceStack.hasPermission(4)`。普通玩家得到翻译后的无权限消息。成功时调用生成的 owo setter、公共 `CONFIG.save()`，然后调用 `OverdriveFlightHandler.refreshPolicyForAll`。

刷新不会额外执行一次加速：

- firework-only 玩家立即停用并走现有安全减速；
- `BOTH` 玩家回落为 `ENCHANTMENT`；
- 所有在线客户端立即收到新的策略和来源。

## 已知限制

- 单人拥有者和 LAN 普通玩家的差异需要实际打开局域网并使用第二客户端验证。
- 其他模组如果在服务端 tick 后覆盖玩家速度，可能改变最终飞行体验，但不能绕过本模组计算的授权和倍率上限。
