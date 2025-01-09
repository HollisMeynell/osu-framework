// src/types/forum.ts
/**
 * 论坛系统相关的类型定义
 * 包含帖子、回复、分类等
 */

import { Timestamps } from './common';

// 论坛分区
export interface ForumSection extends Timestamps {
    id: number;
    gameId: number;          // 关联的比赛ID
    name: string;            // 分区名称
    description: string;     // 分区描述
    order: number;          // 显示顺序
    type: ForumSectionType;
    moderators: number[];   // 版主ID列表
    rules?: string;         // 分区规则
    isLocked?: boolean;     // 是否锁定
}

// 分区类型
export enum ForumSectionType {
    "announcements",     // 公告
    "general",          // 综合讨论
    "match_discussion", // 比赛讨论
    "pool_discussion",  // 图池讨论
    "help_support",     // 帮助支持
    "feedback",         // 反馈建议
    "off_topic"         // 闲聊
}

// 帖子主题
export interface ForumThread extends Timestamps {
    id: number;
    sectionId: number;
    title: string;
    content: string;
    author: number;         // 作者ID
    
    // 帖子状态
    isSticky: boolean;      // 是否置顶
    isLocked: boolean;      // 是否锁定
    isHidden: boolean;      // 是否隐藏
    
    // 标签
    tags: string[];
    
    // 统计
    viewCount: number;
    replyCount: number;
    likeCount: number;
    lastReplyAt: Date;
    lastReplyBy: number;    // 最后回复者ID
    
    // 关联信息
    matchId?: number;       // 关联比赛
    poolId?: number;        // 关联图池
    
    // 编辑记录
    editHistory?: ThreadEdit[];
}

// 帖子编辑记录
export interface ThreadEdit extends Timestamps {
    threadId: number;
    editorId: number;      // 编辑者ID
    oldContent: string;
    newContent: string;
    reason?: string;       // 编辑原因
}

// 回复
export interface ForumReply extends Timestamps {
    id: number;
    threadId: number;
    author: number;
    content: string;
    
    // 回复状态
    isHidden: boolean;
    
    // 引用
    quoteId?: number;      // 引用的回复ID
    
    // 统计
    likeCount: number;
    
    // 编辑记录
    editHistory?: ReplyEdit[];
}

// 回复编辑记录
export interface ReplyEdit extends Timestamps {
    replyId: number;
    editorId: number;
    oldContent: string;
    newContent: string;
    reason?: string;
}

// 点赞记录
export interface ForumLike extends Timestamps {
    userId: number;
    type: "thread" | "reply";
    targetId: number;      // 帖子ID或回复ID
}

// 举报
export interface ForumReport extends Timestamps {
    id: number;
    reporter: number;      // 举报者ID
    type: "thread" | "reply";
    targetId: number;      // 被举报内容ID
    reason: ReportReason;
    description: string;   // 详细说明
    status: ReportStatus;
    handledBy?: number;   // 处理人ID
    resolution?: string;  // 处理结果
}

// 举报原因
export enum ReportReason {
    "spam",              // 垃圾信息
    "inappropriate",     // 不当内容
    "offensive",         // 冒犯性内容
    "rule_violation",    // 违反规则
    "other"             // 其他
}

// 举报状态
export enum ReportStatus {
    "pending",          // 待处理
    "investigating",    // 调查中
    "resolved",         // 已解决
    "rejected"          // 已驳回
}

// 论坛通知
export interface ForumNotification extends Timestamps {
    id: number;
    userId: number;      // 接收者ID
    type: ForumNotificationType;
    threadId?: number;
    replyId?: number;
    fromUser: number;    // 触发者ID
    content: string;
    isRead: boolean;
}

// 通知类型
export enum ForumNotificationType {
    "mention",          // 提及
    "reply",            // 回复
    "quote",           // 引用
    "like",            // 点赞
    "report_handled"    // 举报处理
}

// 论坛权限
export interface ForumPermission {
    sectionId: number;
    roleId: number;      // 用户角色ID
    permissions: {
        viewThreads: boolean;
        createThreads: boolean;
        reply: boolean;
        edit: boolean;
        delete: boolean;
        pin: boolean;    // 置顶
        lock: boolean;   // 锁定
        moderate: boolean;// 管理
    };
}

// 论坛统计
export interface ForumStatistics {
    threadCount: number;
    replyCount: number;
    userCount: number;   // 活跃用户数
    todayPosts: number; // 今日发帖数
    popularTags: {
        tag: string;
        count: number;
    }[];
    topThreads: {
        id: number;
        title: string;
        viewCount: number;
    }[];
}