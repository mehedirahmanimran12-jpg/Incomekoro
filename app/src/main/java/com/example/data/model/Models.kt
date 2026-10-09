package com.example.data.model

enum class SubmissionType {
    TEXT,
    LINK,
    IMAGE,
    TEXT_AND_LINK,
    TEXT_AND_IMAGE
}

enum class TaskDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class TaskCategory(val displayName: String) {
    DAILY("Daily Tasks"),
    PROMOTIONAL("Promotional Tasks"),
    SOCIAL("Social Tasks"),
    SKILL("Skill Tasks"),
    SPECIAL("Special Tasks")
}

enum class TaskStatus(val displayName: String) {
    AVAILABLE("Available"),
    IN_PROGRESS("In Progress"),
    SUBMITTED("Submitted"),
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    EXPIRED("Expired")
}

enum class TransactionType(val displayName: String) {
    TASK_INCOME("Task Income"),
    REFERRAL_INCOME("Referral Income"),
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    BONUS("Bonus"),
    TEAM_COMMISSION("Team Commission")
}

enum class TransactionStatus(val displayName: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled")
}

enum class PaymentMethod(val displayName: String, val hexColor: Long) {
    BKASH("bKash", 0xFFE2136E),
    NAGAD("Nagad", 0xFFF7931E),
    ROCKET("Rocket", 0xFF8C3494)
}

enum class DepositStatus(val displayName: String) {
    PENDING("Pending Verification"),
    VERIFIED("Verified & Credited"),
    REJECTED("Rejected")
}

enum class WithdrawalStatus(val displayName: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    REJECTED("Rejected")
}

enum class NotificationCategory(val displayName: String) {
    SYSTEM("System"),
    TASK("Task"),
    WALLET("Wallet"),
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    REFERRAL("Referral"),
    PROMOTION("Promotion")
}

enum class TicketCategory(val displayName: String) {
    FAQ("FAQ"),
    PAYMENT_ISSUE("Payment Issue"),
    ACCOUNT_ISSUE("Account Issue"),
    TASK_ISSUE("Task Issue"),
    GENERAL("General Query")
}

enum class TicketStatus(val displayName: String) {
    OPEN("Open"),
    PENDING("Pending"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImage: String = "",
    val referralCode: String = "",
    val referredBy: String = "",
    val balance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val totalEarned: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val totalDeposited: Double = 0.0,
    val tasksCompleted: Int = 0,
    val teamCount: Int = 0,
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.DAILY,
    val reward: Double = 0.0,
    val estimatedTimeMinutes: Int = 5,
    val difficulty: TaskDifficulty = TaskDifficulty.EASY,
    val status: TaskStatus = TaskStatus.AVAILABLE,
    val deadline: String = "24h left",
    val requirements: List<String> = emptyList(),
    val instructions: String = "",
    val submissionType: SubmissionType = SubmissionType.TEXT_AND_LINK,
    val maxCompletions: Int = 100,
    val completedCount: Int = 0,
    val repeatAllowed: Boolean = false
)

data class TaskSubmission(
    val id: String = "",
    val taskId: String = "",
    val taskTitle: String = "",
    val userId: String = "",
    val userName: String = "",
    val submissionText: String = "",
    val submissionLink: String = "",
    val submissionImageUrl: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val feedback: String? = null,
    val rewardAmount: Double = 0.0
)

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val type: TransactionType = TransactionType.TASK_INCOME,
    val amount: Double = 0.0,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val paymentMethod: String? = null,
    val transactionRef: String? = null
)

data class DepositRequest(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.BKASH,
    val senderNumber: String = "",
    val transactionId: String = "",
    val status: DepositStatus = DepositStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val adminNote: String? = null
)

data class WithdrawalRequest(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.BKASH,
    val accountNumber: String = "",
    val fee: Double = 0.0,
    val netAmount: Double = 0.0,
    val status: WithdrawalStatus = WithdrawalStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val adminNote: String? = null
)

data class TeamMember(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImage: String = "",
    val joinDate: Long = System.currentTimeMillis(),
    val status: String = "Active",
    val earnedAmount: Double = 0.0,
    val tasksCompleted: Int = 0,
    val level: Int = 1
)

data class ReferralLevelInfo(
    val level: Int,
    val count: Int,
    val commissionPercent: Int,
    val earnings: Double
)

data class ReferralInfo(
    val myCode: String = "",
    val referralLink: String = "",
    val totalReferrals: Int = 0,
    val activeReferrals: Int = 0,
    val referralEarnings: Double = 0.0,
    val levels: List<ReferralLevelInfo> = emptyList()
)

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: NotificationCategory = NotificationCategory.SYSTEM,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetRoute: String? = null
)

data class TicketReply(
    val id: String = "",
    val senderName: String = "",
    val isStaff: Boolean = false,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SupportTicket(
    val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val category: TicketCategory = TicketCategory.GENERAL,
    val message: String = "",
    val status: TicketStatus = TicketStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val replies: List<TicketReply> = emptyList()
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val publishedDate: Long = System.currentTimeMillis(),
    val tag: String = "Announcement",
    val isImportant: Boolean = false
)

data class AppSettings(
    val minDeposit: Double = 100.0,
    val minWithdrawal: Double = 200.0,
    val withdrawFeePercent: Double = 2.0,
    val referralBonusPerInvite: Double = 50.0,
    val dailyTaskLimit: Int = 20,
    val supportEmail: String = "support@incomekoro.com",
    val supportWhatsapp: String = "+8801700000000",
    val bkashNumber: String = "01711-223344",
    val nagadNumber: String = "01811-223344",
    val rocketNumber: String = "01911-223344",
    val noticeTicker: String = "🔥 Welcome to INCOME KORO! Complete Daily Tasks, build your team, and withdraw instantly to bKash/Nagad/Rocket."
)
