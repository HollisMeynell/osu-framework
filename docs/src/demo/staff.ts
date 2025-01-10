// src/types/staff.ts
/**
 * 工作人员系统相关的类型定义
 * 包含工作人员角色、权限、排班等
 */

import { Timestamps, ApprovalStatus } from './common';

// 比赛中的角色
export enum GameRole {
    "host",              // 主办方
    "manager",           // 管理员
    "beatmap_selector",  // 选图人
    "beatmap_tester",    // 测图人
    "referee",           // 裁判
    "streamer",          // 直播员
    "commentator",       // 解说
}

// 角色权限配置
// 角色权限配置
export interface RolePermissions {
    [GameRole.host]: {
        // 管理权限
        manageStaff: true,       // 管理工作人员
        manageRules: true,       // 管理规则
        managePool: true,        // 管理图池
        manageRegistration: true, // 管理报名
        manageSchedule: true,     // 管理赛程

        // 邀请权限
        inviteStaff: true,       // 邀请工作人员
        invitePlayer: true,      // 邀请选手参赛
        inviteTeam: true,        // 邀请队伍参赛

        all: true                 // 所有权限
    };
    [GameRole.manager]: {
        // 管理权限
        managePool: true,
        manageRegistration: true,
        manageSchedule: true,
        inviteStaff: true,
        invitePlayer: true,
        // ... 其他权限
    };
    [GameRole.beatmap_selector]: {
        // 管理权限
        managePool: true,        // 管理图池
        inviteTester: true,
    };
    [GameRole.beatmap_tester]: {
        // 仅有基本权限，无邀请权限
    };
    [GameRole.referee]: {
        manageMatch: true,       // 管理比赛
        inviteReferee: true,     // 可以邀请其他裁判（用于替补）
    };
    // ... 其他角色的权限配置
}

// 工作人员信息
export interface Staff extends Timestamps {
    id: number;
    gameId: number;
    userId: number;
    roles: GameRole[];           // 担任的角色
    status: "active" | "inactive";
    joinedAt: Date;              // 加入时间
    leftAt?: Date;               // 离开时间

    // 工作统计
    statistics: {
        matchesHandled: number;  // 处理的比赛数
    };

    // 可用性
    availability: {
        timezone: string;
        preferredTimes: TimePreference[];
        unavailableDates: Date[];
    };
}

// 时间偏好
export interface TimePreference {
    dayOfWeek: number;           // 0-6，0代表周日
    startTime: string;           // "HH:mm" 格式
    endTime: string;             // "HH:mm" 格式
}

// 工作人员排班
export interface StaffSchedule extends Timestamps {
    id: number;
    matchId: number;
    staffId: number;
    role: GameRole;
    status: ScheduleStatus;

    // 时间安排
    scheduledStart: Date;
    scheduledEnd: Date;
    actualStart?: Date;
    actualEnd?: Date;

    // 替换记录
    replacedBy?: number;        // 被谁替换
    replacementReason?: string; // 替换原因
}

// 排班状态
export enum ScheduleStatus {
    "pending",       // 待确认
    "confirmed",     // 已确认
    "completed",     // 已完成
    "cancelled",     // 已取消
    "replaced"       // 已替换
}

// 工作记录
export interface StaffWork extends Timestamps {
    id: number;
    staffId: number;
    type: WorkType;
    matchId?: number;        // 相关比赛
    poolId?: number;         // 相关图池
    description: string;
    duration: number;        // 工作时长（分钟）

    // 评价
    feedback?: {
        rating: number;      // 评分
        comment: string;     // 评语
        from: number;        // 评价人
    };
}

// 工作类型
export enum WorkType {
    "match_referee",     // 比赛裁判
    "match_stream",      // 比赛直播
    "match_commentary",  // 比赛解说
    "pool_selection",    // 图池选图
    "pool_testing",      // 图池测试
    "admin_work",        // 行政工作
    "other"             // 其他工作
}

// 工作申请
export interface StaffApplication extends Timestamps {
    id: number;
    gameId: number;
    userId: number;
    roles: GameRole[];        // 申请的角色

    // 申请信息
    experience: {
        role: GameRole;
        description: string;
        tournaments: string[];
    }[];

    availability: {
        timezone: string;
        weeklyHours: number;
        preferences: TimePreference[];
    };

    // 申请状态
    status: ApprovalStatus;
    reviewedBy?: number;     // 审核人
    reviewNote?: string;     // 审核备注
}

// 工作评价记录
export interface StaffEvaluation extends Timestamps {
    id: number;
    staffId: number;
    evaluatorId: number;     // 评价人
    period: {                // 评价周期
        start: Date;
        end: Date;
    };
    metrics: {               // 评价指标
        reliability: number; // 可靠性
        efficiency: number;  // 效率
        teamwork: number;    // 团队协作
        communication: number;// 沟通能力
    };
    strengths: string[];     // 优点
    improvements: string[];  // 改进建议
    overallRating: number;   // 总体评分
    comments: string;        // 评语
}