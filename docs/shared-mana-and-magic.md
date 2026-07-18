# 共享法力值与魔幻调律协议

## 核心决定

`魔幻地球 on Minecraft` 不再把魔力做成与玄幻灵力割裂的第二条能量。两个独立模组同时安装时，魔力和灵力统一为“法力值”，但来源、训练方式和内容风格不同。

```text
法力上限 = 基础法力
        + 玄幻路线贡献
        + 魔幻路线贡献
        + 装备贡献
        + 临时效果贡献
```

玩家同时学习魔幻和玄幻时，两个贡献直接相加。魔幻不覆盖玄幻，玄幻也不覆盖魔幻。

## 共享持久数据键

本分支与玄幻分支约定使用：

```text
earth_online_arcana.current_mana
earth_online_arcana.base_mana
earth_online_arcana.xuanhuan_mana_bonus
earth_online_arcana.magic_mana_bonus
earth_online_arcana.equipment_mana_bonus
earth_online_arcana.temporary_mana_bonus
earth_online_arcana.qi_absorption_rate
earth_online_arcana.magic_attunement_rate
earth_online_arcana.cultivation_level
earth_online_arcana.magic_research_level
```

魔幻分支只负责写入：

- `magic_mana_bonus`
- `magic_attunement_rate`
- `magic_research_level`
- 当前法力的恢复值

它会读取玄幻贡献用于显示总法力，但不会重置玄幻贡献。

## 魔幻入门

第一版魔幻入门物品是 `奥术启蒙笔记`：

- 由 `魔幻田野手册 + 魔尘` 合成。
- 首次右键学习，增加魔幻路线法力上限和魔力调律率。
- 之后右键表示冥想调律，恢复少量法力。

魔幻手册继续负责新手路线说明；奥术启蒙笔记负责实际解锁魔力路线。

## 与玄幻分支联动

玩家同时安装两个独立模组时，推荐起步体验：

1. 用泥土/木板/石头合成玄幻的 `引气诀`，学习灵气路线。
2. 用魔幻手册找到魔尘配方，再合成 `奥术启蒙笔记`，学习魔力路线。
3. 用寻灵罗盘或魔幻手册查看统一法力值：基础、灵力贡献、魔力贡献会一起显示。

## 后续开发顺序

1. 魔幻手册 GUI 展示法力条、魔力路线、玄幻路线贡献。
2. 炼金台、符文刻台、仪式基座消耗统一法力值，而不是各自造一套能量。
3. 仪式结构提供临时法力上限或恢复加成。
4. JEI 按炼金、符文、仪式分开页面，并在说明里写明消耗的是统一法力值。
5. 等玄幻/魔幻/未来路线稳定后，再抽 `earth_online_arcana_api`。
