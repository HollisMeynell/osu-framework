// src/types/common.ts
/**
 * 通用类型定义
 * 包含跨模块使用的基础枚举和接口
 */

// 游戏模式
export enum GameMode {
    "osu!",
    "Taiko",
    "Catch The Beat",
    "osu!mania",
}

// Mod类型
export enum Mod {
    "HD",  // Hidden
    "HR",  // Hard Rock
    "DT",  // Double Time
    "FM",  // Free Mod
    "EZ",  // Easy
    "HT",  // Half Time
    "NC",  // Nightcore
    "NF",  // No Fail
    "SD",  // Sudden Death
    "PF",  // Perfect
    "SO",  // Spin Out
    "RX",  // Relax
    "AP",  // Auto Pilot
    "FL",  // Flashlight
    "AT",  // Auto
    "CN",  // Cinema
    "TB",  // Target Break
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