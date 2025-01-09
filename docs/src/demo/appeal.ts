// src/types/appeal.ts
/**
 * 申诉系统相关的类型定义
 * 包含申诉、证据、处理流程等
 */

import { Timestamps } from './common';

// 申诉类型
export enum AppealType {
    "score_dispute",     // 分数争议
    "rule_violation",    // 规则违反
    "technical_issue",   // 技术问题
    "player_conduct",    // 选手行为
    "staff_conduct",     // 工作人员行为
    "match_result",      // 比赛结果
    "other"             // 其他
}

// 申诉优先级
export enum AppealPriority {
    "low",              // 低优先级
    "medium",           // 中优先级
    "high",             // 高优先级
    "urgent"            // 紧急
}

// 申诉状态
export enum AppealStatus {
    "pending",          // 待处理
    "investigating",    // 调查中
    "reviewing",        // 审核中
    "resolved",         // 已解决
    "rejected",         // 已驳回
    "cancelled"         // 已取消
}

// 申诉主体
export interface Appeal extends Timestamps {
    id: number;
    gameId: number;
    matchId?: number;         // 相关比赛
    submittedBy: number;      // 提交者ID
    type: AppealType;
    priority: AppealPriority;
    status: AppealStatus;
    
    // 基本信息
    title: string;
    description: string;
    evidence: Evidence[];     // 证据
    
    // 处理信息
    assignedTo?: number;      // 受理人ID
    resolution?: string;      // 处理结果
    action?: string;          // 采取的行动
    
    // 时间节点
    submittedAt: Date;
    respondedAt?: Date;      // 首次响应时间
    resolvedAt?: Date;       // 解决时间
    
    // 关联方
    involvedParties: {
        userId: number;
        role: "appellant" | "defendant" | "witness";
    }[];
    
    // 工作流
    workflow: AppealWorkflow;
}

// 证据
export interface Evidence extends Timestamps {
    id: number;
    appealId: number;
    type: EvidenceType;
    content: string;          // 内容或URL
    description: string;      // 说明
    submittedBy: number;
    verifiedAt?: Date;       // 验证时间
    verifiedBy?: number;     // 验证人
}

// 证据类型
export enum EvidenceType {
    "screenshot",        // 截图
    "replay",           // 回放
    "video",            // 视频
    "chat_log",         // 聊天记录
    "match_data",       // 比赛数据
    "other"             // 其他
}

// 申诉工作流
export interface AppealWorkflow extends Timestamps {
    appealId: number;
    currentStage: WorkflowStage;
    stages: {
        stage: WorkflowStage;
        assignedTo: number;
        startedAt?: Date;
        completedAt?: Date;
        notes?: string;
    }[];
    history: AppealAction[];
}

// 工作流阶段
export enum WorkflowStage {
    "submission",       // 提交
    "initial_review",   // 初审
    "investigation",    // 调查
    "final_review",     // 终审
    "resolution",       // 解决
    "appeal",           // 再申诉
    "closed"           // 结束
}

// 申诉处理动作
export interface AppealAction extends Timestamps {
    id: number;
    appealId: number;
    type: AppealActionType;
    actor: number;          // 执行人
    description: string;
    changes?: {
        field: string;
        oldValue: any;
        newValue: any;
    }[];
}

// 处理动作类型
export enum AppealActionType {
    "status_change",     // 状态变更
    "assign",           // 分配
    "add_evidence",     // 添加证据
    "add_comment",      // 添加评论
    "update_priority",  // 更新优先级
    "request_info",     // 请求信息
    "provide_info"      // 提供信息
}

// 申诉评论
export interface AppealComment extends Timestamps {
    id: number;
    appealId: number;
    author: number;
    content: string;
    visibility: "public" | "staff_only" | "admin_only";
    attachments?: Evidence[];
}

// 申诉模板
export interface AppealTemplate {
    id: number;
    type: AppealType;
    name: string;
    description: string;
    requiredFields: {
        name: string;
        type: "text" | "number" | "date" | "select";
        options?: string[];
        required: boolean;
    }[];
    suggestedEvidence: {
        type: EvidenceType;
        description: string;
    }[];
}

// 申诉统计
export interface AppealStatistics {
    totalAppeals: number;
    resolvedAppeals: number;
    averageResolutionTime: number;  // 平均解决时间（小时）
    byType: Record<AppealType, number>;
    byPriority: Record<AppealPriority, number>;
    byStatus: Record<AppealStatus, number>;
    topHandlers: {
        userId: number;
        resolvedCount: number;
        averageTime: number;
    }[];
}