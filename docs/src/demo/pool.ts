// src/types/pool.ts
/**
 * 图池系统相关的类型定义
 * 包含图池、模组组、谱面槽位等
 */

import { Mod, BeatmapInfo, Timestamps, ApprovalStatus } from './common';

// 图池状态
export enum PoolStatus {
    "drafting",      // 制作中
    "testing",       // 测试中
    "completed",     // 已完成
    "published",     // 已发布
    "archived"       // 已存档
}

// 图池
export interface Pool extends Timestamps {
    id: number;
    gameId: number;
    name: string;           // 图池名称
    status: PoolStatus;
    categories: Category[]; // 模组组列表
    
    // 时间安排
    testingDeadline?: Date;  // 测试截止时间
    publishDate?: Date;      // 发布时间
    
    // 统计信息
    totalMaps: number;       // 总谱面数
    confirmedMaps: number;   // 已确认谱面数
    
    // 版本控制
    version: number;         // 版本号
    history: PoolVersion[];  // 历史版本
}

// 图池历史版本
export interface PoolVersion extends Timestamps {
    id: number;
    poolId: number;
    version: number;
    changes: {
        type: "add" | "remove" | "modify";
        categoryId: number;
        slotId: number;
        description: string;
    }[];
    changedBy: number;     // 修改人ID
}

// 模组组
export interface Category extends Timestamps {
    id: number;
    poolId: number;
    info: CategoryInfo;
    slots: CategorySlot[];
}

// 模组组信息
export interface CategoryInfo {
    name: string;           // 组名
    mustMod: Mod;          // 必选mod
    optionalMods: Mod[];   // 可选mod
    order: number;         // 顺序
    color?: string;        // 显示颜色
    description?: string;  // 描述
}

// 谱面槽位（如 HD1、HD2）
export interface CategorySlot extends Timestamps {
    id: number;
    categoryId: number;
    position: string;       // 位置标识（如 "HD1"）
    beatmapInfo: BeatmapInfo;
    suggestedBy: number;    // 推荐者ID
    testers: number[];      // 测试者ID列表
    status: CategorySlotStatus;
    feedback: CategorySlotFeedback[];  // 测试反馈
    
    // 确认信息
    confirmedBy?: number;   // 确认者ID
    confirmedAt?: Date;     // 确认时间
    
    // 元数据
    tags?: string[];       // 标签（如 "stream"、"tech"）
    difficulty?: {         // 难度评估
        aim: number;
        speed: number;
        reading: number;
        overall: number;
    };
}

// 谱面槽位状态
export enum CategorySlotStatus {
    "pending",       // 待测试
    "testing",       // 测试中
    "confirmed",     // 已确认
    "rejected"       // 已拒绝
}

// 测图反馈
export interface CategorySlotFeedback extends Timestamps {
    id: number;
    slotId: number;
    testerId: number;      // 测试者ID
    rating: number;        // 评分（e.g. 1-10）
    feedback: string;      // 文字反馈
    
    // 详细评分
    scores?: {
        difficulty: number;   // 难度合适度
        quality: number;      // 谱面质量
        suitability: number;  // 适合度
    };
    
    // 具体问题
    issues?: {
        type: "pattern" | "timing" | "difficulty" | "other";
        description: string;
        severity: "low" | "medium" | "high";
    }[];
    
    recommendation: "accept" | "modify" | "reject";
    visibility: "public" | "staff_only";  // 反馈可见性
}

// 图池预览（用于展示给玩家）
export interface PoolView {
    id: number;
    name: string;
    categories: {
        name: string;
        mustMod: Mod;
        maps: {
            position: string;
            beatmapInfo: BeatmapInfo;
            difficulty: number;
            tags: string[];
        }[];
    }[];
    statistics: {
        totalLength: number;    // 总长度
        averageStar: number;    // 平均星数
        modDistribution: Record<Mod, number>;  // mod分布
    };
}

// 图池下载信息
export interface PoolDownload extends Timestamps {
    id: number;
    poolId: number;
    userId: number;
    downloadedAt: Date;
    format: "direct" | "collection" | "zip";
    // 下载统计
    mapCount: number;
    totalSize: number;
}