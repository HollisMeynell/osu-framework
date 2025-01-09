// src/types/user.ts
/**
 * 用户系统相关的类型定义
 * 包含用户信息、权限、徽章等
 */

import { Timestamps, GameMode } from './common';

// 系统角色
export enum SystemRole {
    "admin",           // 系统管理员
    "moderator",       // 版主
    "verified_host",   // 认证比赛主办方
    "regular_user"     // 普通用户
}

// 用户基础信息
export interface User extends Timestamps {
    uid: number;          // 用户ID
    name: string;         // 用户名
    osuId: number;        // osu个人页面ID
    email: string;        // 邮箱
    avatar: string;       // 头像URL
    country: string;      // 国家/地区
    timezone: string;     // 时区
    roles: SystemRole[];  // 系统角色
    badges: Badge[];      // 徽章
    verified: boolean;    // 是否已验证osu账号
}

// 玩家信息（继承用户基础信息，添加游戏相关属性）
export interface Player extends User {
    rank: {               // 不同模式下的rank
        [key in GameMode]?: {
            global: number;    // 全球排名
            country: number;   // 国家排名
        }
    };
    pp: {                 // PP值
        [key in GameMode]?: number;
    };
    badges: PlayerBadge[]; // 玩家专属徽章
    statistics: {         // 游戏统计
        rankedScore: number;
        playCount: number;
        accuracy: number;
        // ... 其他统计信息
    };
}

// 用户设置
export interface UserSettings {
    uid: number;
    preferences: {
        language: string;
        notifications: {
            email: boolean;
            browser: boolean;
            discord?: string;  // Discord通知
        };
        timezone: string;
        // ... 其他偏好设置
    };
}

// 徽章基础接口
export interface Badge {
    id: number;
    name: string;
    description: string;
    icon: string;         // 徽章图标URL
    type: BadgeType;
    createdAt: Date;
}

// 玩家专属徽章（继承基础徽章，添加获得条件等信息）
export interface PlayerBadge extends Badge {
    achievedAt: Date;     // 获得时间
    tournament?: {        // 如果是比赛奖励
        id: number;
        name: string;
        placement: string;
    };
}

// 徽章类型
export enum BadgeType {
    "tournament_winner",    // 比赛冠军
    "tournament_staff",     // 比赛工作人员
    "verified_host",        // 认证主办方
    "achievement",          // 成就
    "special",             // 特殊徽章
}

// 用户通知
export interface UserNotification extends Timestamps {
    id: number;
    userId: number;
    type: NotificationType;
    title: string;
    content: string;
    read: boolean;
    link?: string;        // 相关链接
}

// 通知类型
export enum NotificationType {
    "tournament_invite",     // 比赛邀请
    "match_reminder",        // 比赛提醒
    "staff_assignment",      // 工作人员安排
    "announcement",          // 公告
    "mention",              // 被提及
    "system",               // 系统通知
}

// 用户活动日志
export interface UserActivityLog extends Timestamps {
    id: number;
    userId: number;
    type: UserActivityType;
    details: any;         // 活动详情
    ip?: string;          // IP地址
    userAgent?: string;   // 用户代理
}

// 用户活动类型
export enum UserActivityType {
    "login",               // 登录
    "register",           // 注册
    "update_profile",      // 更新资料
    "join_tournament",     // 参加比赛
    "create_tournament",   // 创建比赛
    "staff_action",        // 工作人员操作
}