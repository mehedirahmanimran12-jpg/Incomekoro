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
    TEAM_COMMISSION("Team Commission"),
    MOBILE_RECHARGE("Mobile Recharge")
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

enum class TelecomOperator(
    val displayName: String,
    val bengaliName: String,
    val brandColor: Long,
    val prefixes: List<String>
) {
    GRAMEENPHONE("Grameenphone", "গ্রামীনফোন", 0xFF0072CE, listOf("017", "013")),
    BANGLALINK("Banglalink", "বাংলালিংক", 0xFFFF6801, listOf("019", "014")),
    ROBI("Robi", "রবি", 0xFFED1C24, listOf("018")),
    AIRTEL("Airtel", "এয়ারটেল", 0xFFE40000, listOf("016")),
    TELETALK("Teletalk", "টেলিটক", 0xFF009A44, listOf("015")),
    SKITTO("Skitto", "স্কিটো", 0xFFFFB703, listOf("017", "013"))
}

enum class SimType(val displayName: String, val bengaliName: String) {
    PREPAID("Prepaid", "প্রিপেইড"),
    POSTPAID("Postpaid", "পোস্টপেইড"),
    SKITTO("Skitto", "স্কিটো")
}

enum class OfferType(val displayName: String) {
    REGULAR("সরাসরি"),
    INTERNET("ইন্টারনেট"),
    MINUTE("মিনিট"),
    BUNDLE("বান্ডেল / ড্রাইভ")
}

data class RechargeOffer(
    val id: String,
    val title: String,
    val validity: String,
    val price: Double,
    val cashback: Double,
    val type: OfferType,
    val operator: TelecomOperator,
    val description: String = "",
    val badge: String = ""
)

data class MobileRechargeRecord(
    val id: String = "",
    val phoneNumber: String = "",
    val operator: TelecomOperator = TelecomOperator.GRAMEENPHONE,
    val simType: SimType = SimType.PREPAID,
    val amount: Double = 0.0,
    val cashback: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "সফল",
    val transactionRef: String = ""
)

data class DriveOfferItem(
    val id: String,
    val operator: TelecomOperator,
    val title: String,
    val validity: String = "৩০ দিন",
    val division: String = "অল বিডি",
    val regularPrice: Double,
    val offerPrice: Double,
    val cashback: Double,
    val description: String = "যেকোনো লোকাল নম্বরে কথা বলুন ও 4G ইন্টারনেট",
    val tag: String = "হট ডিল"
)

data class DriveOfferOrder(
    val id: String = "",
    val offerTitle: String = "",
    val operator: TelecomOperator = TelecomOperator.ROBI,
    val phoneNumber: String = "",
    val simType: SimType = SimType.PREPAID,
    val division: String = "সকল বিভাগ",
    val regularPrice: Double = 0.0,
    val offerPrice: Double = 0.0,
    val cashback: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "প্রসেসিং",
    val transactionRef: String = ""
)

enum class ResellingCategory(val displayName: String) {
    ALL("সকল পণ্য"),
    MENS_FASHION("মেনস ফ্যাশন"),
    WOMENS_FASHION("লেডিস ফ্যাশন"),
    ELECTRONICS("ইলেকট্রনিক্স ও গ্যাজেট"),
    BEAUTY("কসমেটিকস ও বিউটি"),
    WATCHES("ঘড়ি ও অ্যাক্সেসরিজ"),
    HOME_LIVING("হোম অ্যান্ড লিভিং"),
    FOOTWEAR("জুতো ও ব্যাগ")
}

data class ResellingProduct(
    val id: String,
    val productCode: String,
    val title: String,
    val category: ResellingCategory,
    val wholesalePrice: Double,
    val maxRetailPrice: Double,
    val suggestedProfit: Double,
    val stockQuantity: Int = 150,
    val rating: Double = 4.8,
    val reviewCount: Int = 42,
    val description: String = "",
    val shortSpecs: List<String> = emptyList(),
    val deliveryChargeInsideDhaka: Double = 70.0,
    val deliveryChargeOutsideDhaka: Double = 130.0,
    val isHotDeal: Boolean = false,
    val tag: String = "হট ডিল"
)

data class ResellingOrder(
    val id: String = "",
    val productTitle: String = "",
    val productCode: String = "",
    val quantity: Int = 1,
    val wholesalePrice: Double = 0.0,
    val customerSellingPrice: Double = 0.0,
    val resellerProfit: Double = 0.0,
    val deliveryCharge: Double = 70.0,
    val totalCustomerPayable: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val district: String = "ঢাকা",
    val deliveryArea: String = "ঢাকার ভেতরে",
    val status: String = "প্রসেসিং",
    val timestamp: Long = System.currentTimeMillis(),
    val transactionRef: String = ""
)

data class ResellingCartItem(
    val product: ResellingProduct,
    val quantity: Int = 1,
    val customerSellingPrice: Double = product.maxRetailPrice
)

data class FreelancingCourse(
    val id: String,
    val title: String,
    val price: Double = 50.0,
    val priceBengali: String = "৳ ৫০",
    val drawableResName: String,
    val description: String = "",
    val lessonsCount: Int = 12,
    val totalHours: String = "৪ ঘণ্টা ৩০ মিনিট",
    val instructor: String = "Rootva Academy"
)

data class CourseOrder(
    val id: String = "",
    val courseId: String = "",
    val courseTitle: String = "",
    val price: Double = 50.0,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionRef: String = ""
)

data class LearningCourseVideo(
    val id: String,
    val title: String,
    val duration: String = "Duration: 0:00",
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false,
    val videoUrl: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    val youtubeVideoId: String = "dQw4w9WgXcQ",
    val channelName: String = "Rootva",
    val thumbnailType: Int = 1
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val affiliateId: String,
    val memberCountText: String,
    val memberCount: Int,
    val avatarType: Int = 1
)

data class OnlineServiceItem(
    val id: String,
    val title: String,
    val price: Double = 30.0,
    val priceDisplay: String = "Price ৳ 30.00",
    val resellingPrice: Double = 50.0,
    val resellingPriceDisplay: String = "Reselling Price ৳ 50.00",
    val isNew: Boolean = true,
    val category: String = "Digital Product",
    val description: String = "Facebook official latest build with all pro features, ad-free experience, media downloader, and background play unlocked.",
    val downloadUrl: String = "https://rootva.app/download/facebook-v585-premium.apk"
)

data class OnlineServiceOrder(
    val id: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val price: Double = 30.0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadUrl: String = "",
    val status: String = "সফল"
)

data class MicroJobItem(
    val id: String,
    val serialNumber: Int,
    val title: String,
    val reward: Double = 0.5,
    val rewardDisplay: String = "Reward: 0.5৳",
    val platform: String = "Facebook",
    val instructions: String = "",
    val targetUrl: String = "https://facebook.com",
    val requiresScreenshot: Boolean = true
)

data class MicroJobSubmission(
    val id: String = "",
    val jobId: String = "",
    val jobTitle: String = "",
    val reward: Double = 0.5,
    val userProofText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "পেন্ডিং রিভিউ"
)

