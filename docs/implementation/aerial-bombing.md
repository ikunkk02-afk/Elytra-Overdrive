# TNT 空中轰炸实施记录

## 架构与权威边界

空中轰炸独立于“超载”附魔。客户端 `BombingInputHandler` 只在滑翔、主手打火石、副手 TNT 且同步配置开启时拦截主手使用：首次按下发送一次 `StartBombingC2SPayload` 并返回失败结果阻止同次原版点火包；松开或本地条件失效时发送一次 `StopBombingC2SPayload`。长按期间不会逐 tick 或逐次右键重复发送 START。

服务端 `BombingHandler` 使用 `Map<UUID, BombingSessionState>`，不保存 `ServerPlayer`。START 会重新验证存活、旁观模式、滑翔状态、双手物品和服务端配置；合法时立即投放首颗并建立唯一冷却。重复 START 不重置冷却，STOP 可重复调用。后续投弹只由 `END_WORLD_TICK` 和服务端间隔驱动。

退出、重生、换维度、传送和停服会移除状态；停止滑翔、切换物品、TNT 耗尽、打火石损坏、进入旁观或关闭配置会停止 active 状态。网络协议版本提升为 2，避免缺少新 payload 的旧客户端进入服务器。

## TNT 轨迹与消耗

TNT 在玩家碰撞箱底部下方 `0.9` 格、沿水平速度方向后移 `0.35` 格生成；水平速度计算为：

```text
bombVx = playerVx × bombHorizontalInertia
bombVz = playerVz × bombHorizontalInertia
```

水平继承速度最大限制为 `10 blocks/tick`。垂直速度保留玩家垂直速度的 25%，限制在 `[-1.0, 0.1]` 后再减 `0.15`，因此向上飞行时炸弹仍不会持续冲到玩家前面。任意玩家速度或惯性为 NaN/Infinity 时使用 `(0, -0.15, 0)`。

实体成功加入世界后才播放原版 TNT 点燃音效、发送两个烟雾粒子并消耗物品。生存模式副手 TNT 减一，主手打火石通过 `hurtAndBreak(1, player, MAINHAND)` 消耗耐久；创造模式不消耗。生成失败不消耗任何物品。

## 配置

- `enableBombing=true`
- `bombingIntervalTicks=12`，运行时限制 4–100
- `bombFuseTicks=80`，运行时限制 20–200
- `bombHorizontalInertia=0.70`，运行时限制 0.0–1.5

全部字段使用 owo `OVERRIDE_CLIENT`，服务端始终是投弹频率、引信和惯性的最终权威。

## 自动验证

JUnit 覆盖立即首投、间隔计时、STOP、重复 START 幂等、非法间隔限制、水平惯性和非有限速度回退。完整 `clean test`、`build` 和专用服务器启动验证在本轮完成。

## 游戏内手动测试清单

1. 地面手持打火石仍能正常点火。
2. 飞行时没有副手 TNT，打火石行为不异常。
3. 飞行、主手打火石、副手 TNT 时单击只投一颗。
4. 长按能够连续投弹。
5. 松开立即停止。
6. TNT 间隔符合服务器配置。
7. TNT 有合理水平惯性。
8. 高速 Overdrive 飞行时 TNT 形成连续投弹路线。
9. TNT 用完立即停止。
10. 打火石损坏立即停止。
11. 停止滑翔立即停止。
12. 创造模式不消耗物品。
13. 快速重复输入不能绕过服务端间隔。
14. 退出并重新进入后不存在残留连续投弹状态。
