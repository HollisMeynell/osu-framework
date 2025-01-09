// src/types/game.ts
/**
 * 比赛系统的核心类型定义
 * 包含比赛基础信息、报名系统等
 */

import { GameMode, Mod, TimeRange, Timestamps, ApprovalStatus } from './common';
import { User, Player } from './user';
import { Pool } from './pool';
import { Tournament } from './tournament';
import { Staff } from './staff';

// 比赛状态
export enum GameStatus {
    "registration",    // 报名阶段
    "preparation",     // 准备阶段（已报名结束，比赛未开始）
    "ongoing",         // 进行中
    "completed",       // 已完成
    "cancelled"        // 已取消
}

// 比赛基础信息
export interface Game extends Timestamps {
    id: number;
    title: string;           // 比赛标题
    fullTitle: string;       // 完整标题（可能包含赞助商）
    description: string;     // 比赛描述
    mode: GameMode;          // 游戏模式
    status: GameStatus;      // 比赛状态
    
    // 参与限制
    playerLimit: number;     // 人数上限
    rankRange?: {           // 段位限制
        min?: number;
        max?: number;
    };
    countryRestriction?: string[];  // 地区限制

    // 时间安排
    registrationPeriod: TimeRange;  // 报名时间
    tournamentPeriod: TimeRange;    // 比赛时间
    
    // 相关资源
    poster?: string;         // 海报URL
    discord?: string;        // Discord链接
    spreadsheet?: string;    // 表格链接
    
    // 关联数据
    teams: Team[];          // 队伍列表
    staffs: Staff[];        // 工作人员
    pools: Pool[];          // 图池
    tournament: Tournament;  // 比赛赛程
    
    // 统计信息
    playerCount: number;     // 当前参赛人数
    viewCount: number;       // 浏览量
}

// 队伍
export interface Team extends Timestamps {
    id: number;
    gameId: number;
    name: string;            // 队伍名称
    abbreviation: string;    // 队伍缩写
    icon?: string;          // 队伍图标
    players: Player[];       // 队员列表
    captain: number;         // 队长ID
    seedRank?: number;      // 种子排名----------------------待定
    
    statistics?: {          // 队伍统计
        gamesPlayed: number;
        gamesWinned: number;
        matchesPlayed: number;
        matchesWinned: number;
        averageProgress: number; // 平均晋级进度
    };
}

// 报名表单定义
export interface RegistrationForm {
    id: number;
    gameId: number;
    type: "individual" | "team";  // 个人赛/团队赛
    questions: RegistrationQuestion[];  // 问题列表
}

// 报名问题
export interface RegistrationQuestion {
    id: number;
    type: "text" | "number" | "choice" | "multiple";
    required: boolean;
    question: string;
    options?: string[];     // 选择题选项
    validation?: {          // 验证规则
        min?: number;
        max?: number;
        regex?: string;
    };
}

// 报名记录
export interface Registration extends Timestamps {
    id: number;
    gameId: number;
    userId: number;
    teamId?: number;        // 团队赛时的队伍ID
    status: ApprovalStatus;
    answers: {
        questionId: number;
        answer: string;
    }[];
    comment?: string;       // 审核评语
    reviewedBy?: number;    // 审核人ID
    reviewedAt?: Date;      // 审核时间
}

// 比赛公告
export interface Announcement extends Timestamps {
    id: number;
    gameId: number;
    title: string;
    content: string;
    author: number;         // 发布者ID
    priority: "normal" | "important" | "urgent";
    pinned: boolean;        // 是否置顶
    visible: boolean;       // 是否可见
}

// 比赛统计
export interface GameStatistics {
    gameId: number;
    registrationStats: {
        total: number;
        approved: number;
        pending: number;
        rejected: number;
    };
    matchStats: {
        total: number;
        completed: number;
        pending: number;
        cancelled: number;
    };
    playerStats: {
        averageRank: number;
        countryDistribution: Record<string, number>;
        timezoneDistribution: Record<string, number>;
    };
}