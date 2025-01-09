// src/types/tournament.ts
/**
 * 比赛进程相关的类型定义
 * 包含比赛赛程、对局、分数等
 */

import { Timestamps } from './common';
import { Team } from './game';
import { Pool, CategorySlot } from './pool';
import { Staff } from './staff';

// 比赛规则配置
export interface TournamentRules {
    teamMode: TeamMode;         // 比赛模式
    winCondition: WinCondition; // 胜利条件
    bestOf: number;             // BO几
    banCount: number;           // 禁用图数量
    pickCount: number;          // 选择图数量
    warmupCount?: number;       // 热身图数量
    scoreMultipliers?: Record<string, number>; // 分数倍率(如 EZ 2倍)
    forceNF?: boolean;         // 是否强制开启 NF
    allowLateJoin?: boolean;   // 是否允许迟到加入
}

// 队伍模式
export enum TeamMode {
    "HeadToHead",    // 1v1
    "TeamVS",        // 队伍对抗
    "TagCoop",       // 标签合作
    "TagTeamVS"      // 标签队伍对抗
}

// 胜利条件
export enum WinCondition {
    "score",         // 总分
    "accuracy",      // 准确率
    "combo",         // 连击数
    "scoreV2"        // V2分数
}

// 比赛主体
export interface Tournament extends Timestamps {
    id: number;
    gameId: number;
    title: string;
    rules: TournamentRules;
    stages: Stage[];         // 比赛阶段
    status: TournamentStatus;
    currentStageId?: number; // 当前阶段ID
}

// 比赛状态
export enum TournamentStatus {
    "scheduled",     // 已安排
    "ongoing",       // 进行中
    "completed",     // 已完成
    "cancelled"      // 已取消
}

// 比赛阶段（如小组赛、淘汰赛等）
export interface Stage extends Timestamps {
    id: number;
    tournamentId: number;
    name: string;           // 阶段名称
    type: StageType;        // 阶段类型
    order: number;          // 顺序
    pool: Pool;             // 使用的图池
    matches: Match[];       // 对局列表
    startDate: Date;
    endDate: Date;
    settings?: any;         // 阶段特殊设置
}

// 阶段类型
export enum StageType {
    "qualifiers",     // 资格赛
    "groups",         // 小组赛
    "doubleElim",     // 双败淘汰
    "singleElim",     // 单败淘汰
    "roundRobin",     // 循环赛
    "swiss"           // 瑞士轮
}

// 比赛对局
export interface Match extends Timestamps {
    id: number;
    stageId: number;
    number: number;         // 对局编号
    red: Team;              // 红队
    blue: Team;             // 蓝队
    winner?: "red" | "blue";// 获胜方
    status: MatchStatus;    // 对局状态
    
    // 时间安排
    scheduledTime: Date;    // 计划时间
    actualStartTime?: Date; // 实际开始时间
    actualEndTime?: Date;   // 实际结束时间
    
    // 工作人员
    referee?: Staff;        // 裁判
    streamers?: Staff[];    // 直播员
    commentators?: Staff[]; // 解说
    
    // 对局内容
    games: Play[];         // 每个图的记录
    events: MatchEvent[];  // 对局事件记录
    chatLog?: ChatMessage[]; // 聊天记录
    
    // 对局统计
    redScore: number;      // 红队总分
    blueScore: number;     // 蓝队总分
    warmupCount: number;   // 已用热身图数量
}

// 对局状态
export enum MatchStatus {
    "scheduled",     // 已安排
    "preparing",     // 准备中
    "ongoing",       // 进行中
    "completed",     // 已完成
    "cancelled",     // 已取消
    "postponed"      // 已推迟
}

// 单张谱面的对局记录
export interface Play extends Timestamps {
    id: number;
    matchId: number;
    number: number;         // 第几张图
    map: CategorySlot;      // 使用的谱面
    type: "warmup" | "pick" | "ban";  // 图的类型
    pickedBy?: "red" | "blue";        // 谁选的
    scores: Score[];        // 所有玩家的成绩
    redScore: number;       // 红队总分
    blueScore: number;      // 蓝队总分
    winner: "red" | "blue"; // 这张图的获胜方
}

// 玩家成绩
export interface Score extends Timestamps {
    id: number;
    gameId: number;
    userId: number;
    team: "red" | "blue";
    score: number;          // 分数
    accuracy: number;       // 准确率
    combo: number;          // 最大连击
    mods: string[];        // 使用的mod
    passed: boolean;       // 是否通过
    // 准确率详情
    counts: {
        300: number;
        100: number;
        50: number;
        miss: number;
    };
}

// 对局事件
export interface MatchEvent extends Timestamps {
    id: number;
    matchId: number;
    type: MatchEventType;
    actor?: number;        // 触发者ID
    description: string;
    data?: any;           // 额外数据
}

// 对局事件类型
export enum MatchEventType {
    "match_start",        // 对局开始
    "match_end",          // 对局结束
    "game_start",         // 单图开始
    "game_end",           // 单图结束
    "player_join",        // 玩家加入
    "player_leave",       // 玩家离开
    "ref_timeout",        // 裁判暂停
    "player_timeout",     // 玩家暂停
    "abort",              // 中止
    "remake",             // 重赛
    "all_ready",          // 所有人准备
    "host_change",        // 主机更换
    "system"              // 系统事件
}

// 聊天消息
export interface ChatMessage extends Timestamps {
    id: number;
    matchId: number;
    userId: number;
    team?: "red" | "blue";
    message: string;
    type: "all" | "team" | "staff";  // 消息类型
}