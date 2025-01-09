// 比赛
interface Game {
    id: number,
    users: GameUser[],
    info: string,
    pool: Pool[],
}

interface GameUser {
    id: number,
    name: string,
    role: GameRole,
    // todo: 补充一下其他用到的信息?
}

enum GameRole {
    "host",
    "manager",
    "beatmap selector",
    "beatmap tester",
} // 单个比赛中的权限(角色)

// 登陆后 一个接口查看所有的邀请信息, 然后有接口可以选择 接受/拒绝
interface GameInvite {
    id: number,
    gameId: number,
    gameName: string,
    target: number,
    role: GameRole,
}

// 登陆后通过 `/api/game/getSelf` 获取 Game[]

interface Pool {
    id: number,
    name: string,
    // 表示是否待选
    pending: boolean,
    category: Category[],
} // 图池

// 玩家可以获取的图池 而且可以直接打包下载
interface PoolView extends Pool {
    // todo: 选图结束后应该有一个最终结构, 公开展示
}

interface Category {
    info: CategoryInfo,
    id: number,
    name: string,
    color?: number, // 颜色? 可以增加区分度
    // 必开的 mod, 用于修改 slots 中谱面的 星级/属性
    mustMods: [],
    // 预想是可以带上可选的 mod, 可以实时更新 星级/属性
    mods: [],
    slots: CategorySlot[],
} // 各模组图池

interface CategoryInfo {
    name: string,
    MustMod: string[],

// 由 manager 可以根据测图人的反馈,
// 直接确定一个 CategorySlotPending id, 此时后端移除其他的CategorySlotPending
// 并且将其转换为 CategorySlot
interface CategorySlot {
    id: number,
    userId: number,
    beatmapInfo: {
        id: number,
        // todo: 要哪一些信息最好精确一点
    },
    // todo: 加一个冤有头债有主的字段?
} // 模组中某位置的图

// 这个当选完后移除, 不再展示
interface CategorySlotPending extends CategorySlot {
    // todo: 推荐理由

    // 测图人评语
    feedback: CategorySlotFeedback[],
}

// 测图可以给出评语
interface CategorySlotFeedback {
    userId: number,
    // todo: 评语 推荐度
}

/*************************************************/
// 选完结束, 进入比赛阶段:

interface Tournament {
    rounds: Round[],
} // 整个比赛

interface Round {
    id: number,
    name: string,
    pool: PoolView,
    // 一轮包含多场比赛
    match: Match[],
} // 单轮所有对局

interface Match {
    red: Team,
    blue: Team,
    winner: "red" | "blue" | "pending",
    action: PlayAction[],
    scores: PlayScores[], // todo: 待补充
    // todo: 针对特殊规则, 比如 acc 排名, EZ分数*2 等描述信息, 以及中间有选手退出导致重赛
} // 单场对局

interface Team {
    name: string,
    players: GameUser[], // 这里建议是分开, 因为可能包含费用之类的
}

interface PlayAction {
    id: number,
    team: "red" | "blue",
    type: "ban" | "pick" | "warmup",
    target: CategorySlot,
    scores?: [],
} // 对局中动作

interface PlayScores {
    team: "red" | "blue",
    score: number,
    playerScores: playerScore[],
}

interface playerScore {
    player: GameUser,
    score: number
}

/*************************************************/
// 比赛结束
