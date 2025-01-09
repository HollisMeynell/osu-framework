// src/types/invites.ts
/**
 * 邀请系统相关的类型定义
 * 包含所有类型的邀请：工作人员邀请、队伍邀请、比赛邀请等
 */

import { Timestamps } from './common';
import { GameRole } from './staff';

// 邀请类型
export enum InviteType {
    "team",             // 队伍邀请
    "tournament",       // 比赛邀请
    "staff"            // 工作人员邀请
}

// 邀请状态
export enum InviteStatus {
    "pending",         // 待处理
    "accepted",        // 已接受
    "rejected",        // 已拒绝
    "cancelled",       // 已取消
    "expired"          // 已过期
}

// 基础邀请接口
export interface GameInvite extends Timestamps {
    id: number;
    type: InviteType;
    gameId: number;        // 比赛ID
    targetId: number;      // 被邀请者ID
    inviterId: number;     // 邀请者ID
    status: InviteStatus;
    
    // 邀请信息
    message?: string;      // 邀请信息
    expiresAt: Date;       // 过期时间
    
    // 处理信息
    respondedAt?: Date;    // 响应时间
    response?: string;     // 回复信息

    // 根据不同类型的邀请，包含不同的具体信息
    details: StaffInviteDetails | TeamInviteDetails | TournamentInviteDetails;
}

// 工作人员邀请详情
export interface StaffInviteDetails {
    roles: GameRole[];     // 邀请的角色
}

// 队伍邀请详情
export interface TeamInviteDetails {
    teamId: number;        // 队伍ID
    teamName: string;      // 队伍名称
    role?: "captain" | "member";  // 队伍中的角色
}

// 比赛邀请详情
export interface TournamentInviteDetails {
    tournamentRole: "player" | "substitute";  // 比赛中的角色
}

// 邀请记录
export interface InviteHistory extends Timestamps {
    inviteId: number;
    status: InviteStatus;
    actor: number;         // 操作者
    reason?: string;       // 原因
}

// 邀请权限
export interface InvitePermissions {
    canInviteStaff: boolean;    // 是否可以邀请工作人员
    canInviteToTeam: boolean;   // 是否可以邀请加入队伍
    canInviteToTournament: boolean;  // 是否可以邀请参加比赛
}