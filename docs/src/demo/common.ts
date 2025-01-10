// src/types/common.ts
/**
 * 通用类型定义
 * 包含跨模块使用的基础枚举和接口
 */

// 游戏模式
export enum GameMode {
    // 来自 osu api 给定的模式名 (https://osu.ppy.sh/docs/#ruleset)
    "osu",
    "taiko",
    "fruits",
    "mania",
}

// Mod类型
export enum Mod {
    // 表示真实的mod, 并非 category, 然后移除了大部分不可能上比赛的 mod
    NoFail = 1,
    Easy = 2,
    TouchDevice = 4,
    Hidden = 8,
    HardRock = 16,
    SuddenDeath = 1 << 5,
    DoubleTime = 1 << 6,
    Relax = 1 << 7,
    HalfTime = 1 << 8,
    Nightcore = 1 << 9,
    Flashlight = 1 << 10,
    SpunOut = 1 << 12,
    Autopilot = 1 << 13,
    Key4 = 1 << 15,
    FadeIn = 1 << 20,
    Random = 1 << 21,
    ScoreV2 = 1 << 29,
    Mirror = 1 << 30,
}

const ModShortNameMap  = {
    NoFail: "NF",
    Easy: "EZ",
    TouchDevice: "TD",
    Hidden: "HD",
    HardRock: "HR",
    SuddenDeath: "SD",
    DoubleTime: "DT",
    Relax: "RX",
    HalfTime: "HT",
    Nightcore: "NC",
    Flashlight: "FL",
    SpunOut: "SO",
    Autopilot: "AT",
    Key4: "K4",
    FadeIn: "FI",
    Random: "RD",
    ScoreV2: "V2",
    Mirror: "MR",
}

const ModNumberMap = [
    {key: 1, mod: Mod.NoFail},
    {key: 2, mod: Mod.Easy},
    {key: 4, mod: Mod.TouchDevice},
    {key: 8, mod: Mod.Hidden},
    {key: 16, mod: Mod.HardRock},
    {key: 32, mod: Mod.SuddenDeath},
    {key: 64, mod: Mod.DoubleTime},
    {key: 128, mod: Mod.Relax},
    {key: 256, mod: Mod.HalfTime},
    {key: 512, mod: Mod.Nightcore},
    {key: 1024, mod: Mod.Flashlight},
    {key: 4096, mod: Mod.SpunOut},
    {key: 8192, mod: Mod.Autopilot},
    {key: 32768, mod: Mod.Key4},
    {key: 1048576, mod: Mod.FadeIn},
    {key: 2097152, mod: Mod.Random},
    {key: 536870912, mod: Mod.ScoreV2},
    {key: 1073741824, mod: Mod.Mirror},
]

const ConflictModList = [
    18, // ez + hr
    33, // nf + sd
    320, // ht + dt/nc
    1032, // hd + fl (骂娘禁)
]

class Mods {
    modsNumber: number;

    constructor(mods: number) {
        this.modsNumber = mods;
    }

    tryAdd(otherMods: number): boolean {
        const result = this.modsNumber | otherMods;
        if (this.checkConflictMods(result)) {
            return false;
        }
        this.modsNumber = result;
        return true;
    }

    add(otherMods: number): void {
        this.modsNumber |= otherMods;
    }

    getModsEnumList(): Mod[] {
        const mods: Mod[] = [];
        for (const modsValue of ModNumberMap) {
            if ((this.modsNumber & modsValue.key) === modsValue.key) {
                mods.push(modsValue.mod);
            }
        }
        return mods
    }

    checkConflictMods(mods: number): boolean {
        for (const m of ConflictModList) {
            if ((mods & m) === m) {
                return true;
            }
        }
        return false;
    }
}

// 谱面信息
export interface BeatmapInfo {
    id: number;           // 谱面ID
    title: string;        // 歌曲标题
    artist: string;       // 艺术家
    creator: string;      // 谱面作者
    difficulty: string;   // 难度名
    starRating: number;   // 星级
    bpm: number;         // BPM
    length: number;      // 长度(秒)
    maxCombo: number;    // 最大连击数
    cs: number;          // CS
    od: number;          // OD
    ar: number;          // AR
    hp: number;          // HP
    // 添加可选的mod后的数值
    modifiedStats?: {
        starRating: number;
        bpm: number;
        ar: number;
        od: number;
        cs: number;
        hp: number;
    }
}

// 通用的时间范围
export interface TimeRange {
    start: Date;
    end: Date;
}

// 通用的分页参数
export interface PaginationParams {
    page: number;
    pageSize: number;
}

// 通用的响应格式
export interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

// 通用的创建/更新时间
export interface Timestamps {
    createdAt: Date;
    updatedAt: Date;
}

// 通用的审核状态
export enum ApprovalStatus {
    "pending",   // 待审核
    "approved",  // 已通过
    "rejected"   // 已拒绝
}
