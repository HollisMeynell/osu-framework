import { Mod, GameMode, BeatmapInfo } from './common';

// 比赛阶段:
interface Game {
    id: number,
    playerCount: number, // 参赛人数
    playerLimit: number, // 人数上限
    teams: Team[],
    staffs: Staff[],
    pool: Pool[],
    tournament: Tournament,
}

interface Staff {
    uid: number,
    name: string,
    role: GameRole[],
    timezone?: string; // 时区，便于安排比赛时间
    // TODO: 补充一下其他用到的信息?
}

interface Player {
    uid: number,
    name: string,
    rank: number,
    cost: number,
    etx: number,
    timezone?: string; // 时区，便于安排比赛时间
    // TODO: 补充一下其他用到的信息?
}

enum GameRole {
    "host",
    "manager",
    "beatmap selector",
    "beatmap tester",
} // 单个比赛中的权限(角色)

// 登陆后 一个接口查看所有的邀请信息, 然后有接口可以选择 接受/拒绝
interface GameInvite {
    uid: number,
    game: Game,
    target: number,
    role: GameRole,
}
// 登陆后通过 `/api/game/getSelf` 获取 Game[]

/*************************************************/
// 选图阶段:

interface Pool {
    id: number,
    name: string,
    // 表示是否待选
    pending: boolean,
    category: Category[],
    publishDate?: Date;    // 图池发布时间
    testDeadline?: Date;   // 测图截止时间
} // 图池

// 玩家可以获取的图池 而且可以直接打包下载
interface PoolView extends Pool {
    // TODO: 选图结束后应该有一个最终结构, 公开展示
}

interface Category {
    info: CategoryInfo,
    slots: CategorySlot[],
} // 某个模组的图池

interface CategoryInfo {
    name: string,
    mustMod: Mod,// 必开的 mod, 用于修改 slots 中谱面的 星级/属性
    optionalMods: Mod[],// 预想是可以带上可选的 mod, 可以实时更新 星级/属性
    color?: number, // 颜色? 可以增加区分度
} // 模组信息

// 由 manager 可以根据测图人的反馈,
// 直接确定一个 CategorySlotPending id, 此时后端移除其他的CategorySlotPending
// 并且将其转换为 CategorySlot
interface CategorySlot {
    id: number,
    beatmapInfo: BeatmapInfo,
    suggestedBy: number;   // 推荐者ID
    feedback: CategorySlotFeedback[], // 测图人评语
    status: "pending" | "confirmed" | "rejected", // 状态
    confirmedBy?: number;    // 由哪个manager确认的
} // 模组中某位置的单张图

interface CategorySlotFeedback {
    userId: number,
    rating: number,
    feedback: string,
    tags: string[],
}

/*************************************************/
// 选完结束, 进入比赛阶段:

interface Tournament {
    title: string,
    fullTitle: string,
    poster: string, // 海报
    mode: GameMode,
    info: string, // 介绍
    mainSheetUrl: string, // 主表格地址
    tournamentRules?: {
        scoreMultipliers?: Record<Mod, number>; // 如 EZ 分数翻倍
        teamOptions: 'Head To Head' | 'Tag Co-op' | 'Team Vs' | 'Tag Team Vs'; // 队伍模式
        winCondition: "score" | "accuracy" | "combo" | "scoreV2"; // 胜利条件
        bestOf: number;   // BO几
        options: string;  // 其他规则说明
    };
    status: "scheduled" | "ongoing" | "completed" | "cancelled";
    scheduledTime: Date;   // 预定比赛时间
    actualStartTime?: Date;// 实际开始时间
    actualEndTime?: Date;  // 实际结束时间
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
    id: number,
    red: Team,
    blue: Team,
    winner: "red" | "blue" | "pending",
    status: "scheduled" | "ongoing" | "completed" | "cancelled";
    scheduledTime: Date;   // 预定比赛时间
    actualStartTime?: Date;// 实际开始时间
    actualEndTime?: Date;  // 实际结束时间
    referee?: Staff;    // 裁判
    streamers?: Staff[];// 直播员
    commentators?: Staff[];// 解说
    action: PlayAction[],
    scores: PlayScores[],
    warmupLimit?: number; // 热身图限制数量
    // 以及中间有选手退出导致重赛
} // 单场对局

interface MatchInterruption {
    id: number;
    matchId: number;
    type: "player_disconnect" | "referee_call" | "technical_issue";
    timestamp: Date;
    description: string;
    resolution: string;
    affectedScores?: playerScore[];  // 受影响的分数
}

interface Command {
    id: number,
    name: string,
    cmd: string,
}

interface Team {
    id: number,
    name: string,
    players: Player[],
    captain: number;      // 队长ID
}

interface PlayAction {
    id: number,
    team: "red" | "blue",
    type: "ban" | "pick" | "warmup",
    target: CategorySlot,
    scores?: [],
} // 对局中单次动作

interface PlayScores {
    team: "red" | "blue",
    score: number,
    playerScores: playerScore[],
}

interface playerScore {
    player: Player,
    score: number
}

/*************************************************/
// 比赛结束
