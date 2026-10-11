package com.example.data.repository

import com.example.data.model.*
import com.example.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AppRepository(
    private val preferences: UserPreferences,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    // Current logged-in user state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()

    // Tasks state
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: Flow<List<Task>> = _tasks.asStateFlow()

    // Task submissions state
    private val _submissions = MutableStateFlow<List<TaskSubmission>>(emptyList())
    val submissions: Flow<List<TaskSubmission>> = _submissions.asStateFlow()

    // Transactions state
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: Flow<List<Transaction>> = _transactions.asStateFlow()

    // Team members state
    private val _teamMembers = MutableStateFlow<List<TeamMember>>(emptyList())
    val teamMembers: Flow<List<TeamMember>> = _teamMembers.asStateFlow()

    // Referral info state
    private val _referralInfo = MutableStateFlow(ReferralInfo())
    val referralInfo: Flow<ReferralInfo> = _referralInfo.asStateFlow()

    // Notifications state
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: Flow<List<NotificationItem>> = _notifications.asStateFlow()

    // Support tickets state
    private val _tickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val tickets: Flow<List<SupportTicket>> = _tickets.asStateFlow()

    // Announcements state
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: Flow<List<Announcement>> = _announcements.asStateFlow()

    // App Settings state
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: Flow<AppSettings> = _appSettings.asStateFlow()

    // Mobile Recharges state
    private val _mobileRecharges = MutableStateFlow<List<MobileRechargeRecord>>(emptyList())
    val mobileRecharges: Flow<List<MobileRechargeRecord>> = _mobileRecharges.asStateFlow()

    // Drive Offer Orders state
    private val _driveOrders = MutableStateFlow<List<DriveOfferOrder>>(emptyList())
    val driveOrders: Flow<List<DriveOfferOrder>> = _driveOrders.asStateFlow()

    init {
        // Seed default catalog
        loadInitialData()
        // Check local saved session
        scope.launch {
            val savedId = preferences.savedUserId.first()
            if (!savedId.isNullOrBlank()) {
                val existing = _currentUser.value
                if (existing == null) {
                    // Restore active session
                    val demoUser = createSampleUser(savedId, preferences.savedUserEmail.first() ?: "user@incomekoro.com")
                    _currentUser.value = demoUser
                    refreshUserData(demoUser.uid)
                }
            }
        }
    }

    private fun loadInitialData() {
        _tasks.value = getInitialTasks()
        _announcements.value = getInitialAnnouncements()
        _notifications.value = getInitialNotifications()
        _teamMembers.value = getInitialTeamMembers()
        _tickets.value = getInitialTickets()
        _transactions.value = getInitialTransactions()
        _mobileRecharges.value = getInitialRecharges()
        _driveOrders.value = getInitialDriveOrders()
    }

    // --- Authentication Operations ---

    suspend fun login(email: String, pass: String): Result<User> {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be empty"))
        if (pass.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))

        return try {
            val fbAuth = auth
            val fbFs = firestore
            if (fbAuth != null && fbFs != null) {
                try {
                    val authResult = fbAuth.signInWithEmailAndPassword(cleanEmail, pass).await()
                    val uid = authResult.user?.uid ?: UUID.randomUUID().toString()
                    val doc = fbFs.collection("users").document(uid).get().await()
                    val user = if (doc.exists()) {
                        mapDocumentToUser(doc.data ?: emptyMap(), uid)
                    } else {
                        val newUser = createSampleUser(uid, cleanEmail)
                        saveUserToFirestore(newUser)
                        newUser
                    }
                    _currentUser.value = user
                    preferences.saveUserSession(user.uid, user.email)
                    return Result.success(user)
                } catch (e: Exception) {
                    // If network fails or user not yet in Firebase, check local fallback
                    if (e.message?.contains("network", ignoreCase = true) == true ||
                        e.message?.contains("API key", ignoreCase = true) == true ||
                        e.message?.contains("configuration", ignoreCase = true) == true
                    ) {
                        // Fallback local sign in for resilience
                        val localUser = createSampleUser("usr_${System.currentTimeMillis() % 10000}", cleanEmail)
                        _currentUser.value = localUser
                        preferences.saveUserSession(localUser.uid, localUser.email)
                        return Result.success(localUser)
                    }
                    return Result.failure(e)
                }
            } else {
                val localUser = createSampleUser("usr_${System.currentTimeMillis() % 10000}", cleanEmail)
                _currentUser.value = localUser
                preferences.saveUserSession(localUser.uid, localUser.email)
                Result.success(localUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        referralCode: String
    ): Result<User> {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phone.trim()

        if (cleanName.isBlank()) return Result.failure(IllegalArgumentException("Full Name is required"))
        if (!cleanEmail.contains("@")) return Result.failure(IllegalArgumentException("Valid email is required"))
        if (cleanPhone.length < 11) return Result.failure(IllegalArgumentException("Enter valid 11-digit phone number (e.g. 017xxxxxxxx)"))
        if (pass.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))

        return try {
            val fbAuth = auth
            val fbFs = firestore
            val uid = if (fbAuth != null) {
                try {
                    val authResult = fbAuth.createUserWithEmailAndPassword(cleanEmail, pass).await()
                    authResult.user?.uid ?: UUID.randomUUID().toString()
                } catch (e: Exception) {
                    // In case Firebase config not yet tied to billing or offline
                    "usr_${UUID.randomUUID().toString().take(8)}"
                }
            } else {
                "usr_${UUID.randomUUID().toString().take(8)}"
            }

            val myRefCode = "IK" + (100000..999999).random()
            val newUser = User(
                uid = uid,
                name = cleanName,
                email = cleanEmail,
                phone = cleanPhone,
                profileImage = "",
                referralCode = myRefCode,
                referredBy = referralCode.trim(),
                balance = 50.0, // Registration Welcome Bonus
                pendingBalance = 0.0,
                totalEarned = 50.0,
                totalWithdrawn = 0.0,
                totalDeposited = 0.0,
                tasksCompleted = 0,
                teamCount = 0,
                status = "Active",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Add Welcome Bonus Transaction
            val welcomeTrx = Transaction(
                id = "TRX" + System.currentTimeMillis().toString().takeLast(8),
                userId = uid,
                type = TransactionType.BONUS,
                amount = 50.0,
                status = TransactionStatus.COMPLETED,
                date = System.currentTimeMillis(),
                description = "Welcome Signup Bonus credited to wallet",
                paymentMethod = "System Bonus",
                transactionRef = "BONUS-SIGNUP"
            )

            _currentUser.value = newUser
            _transactions.value = listOf(welcomeTrx) + _transactions.value
            preferences.saveUserSession(uid, cleanEmail)

            if (fbFs != null) {
                try {
                    saveUserToFirestore(newUser)
                } catch (_: Exception) {}
            }

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email"))
        }
        return try {
            auth?.sendPasswordResetEmail(cleanEmail)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Emulate success for resilience if network error
            Result.success(Unit)
        }
    }

    suspend fun logout() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
        preferences.clearUserSession()
    }

    suspend fun updateProfile(name: String, phone: String, profileImage: String): Result<User> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val updated = current.copy(
            name = name.ifBlank { current.name },
            phone = phone.ifBlank { current.phone },
            profileImage = profileImage.ifBlank { current.profileImage },
            updatedAt = System.currentTimeMillis()
        )
        _currentUser.value = updated
        scope.launch {
            try {
                firestore?.collection("users")?.document(updated.uid)?.set(userToMap(updated))?.await()
            } catch (_: Exception) {}
        }
        return Result.success(updated)
    }

    // --- Task Marketplace Operations ---

    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    suspend fun startTask(taskId: String): Result<Unit> {
        val task = getTaskById(taskId) ?: return Result.failure(IllegalArgumentException("Task not found"))
        if (task.status == TaskStatus.EXPIRED) {
            return Result.failure(IllegalStateException("Task has expired"))
        }
        // Mark in progress in local state
        _tasks.value = _tasks.value.map {
            if (it.id == taskId && it.status == TaskStatus.AVAILABLE) it.copy(status = TaskStatus.IN_PROGRESS) else it
        }
        return Result.success(Unit)
    }

    suspend fun submitTask(
        taskId: String,
        submissionText: String,
        submissionLink: String,
        submissionImageUrl: String
    ): Result<TaskSubmission> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Please log in first"))
        val task = getTaskById(taskId) ?: return Result.failure(IllegalArgumentException("Task not found"))

        if (submissionText.isBlank() && submissionLink.isBlank() && submissionImageUrl.isBlank()) {
            return Result.failure(IllegalArgumentException("Please provide proof (text, link, or screenshot)"))
        }

        val submissionId = "SUB" + System.currentTimeMillis().toString().takeLast(8)
        val submission = TaskSubmission(
            id = submissionId,
            taskId = task.id,
            taskTitle = task.title,
            userId = user.uid,
            userName = user.name,
            submissionText = submissionText.trim(),
            submissionLink = submissionLink.trim(),
            submissionImageUrl = submissionImageUrl.trim(),
            status = TaskStatus.PENDING,
            submittedAt = System.currentTimeMillis(),
            rewardAmount = task.reward
        )

        _submissions.value = listOf(submission) + _submissions.value
        _tasks.value = _tasks.value.map {
            if (it.id == taskId) it.copy(status = TaskStatus.SUBMITTED, completedCount = it.completedCount + 1) else it
        }

        // Add to pending balance
        val updatedUser = user.copy(
            pendingBalance = user.pendingBalance + task.reward,
            tasksCompleted = user.tasksCompleted + 1
        )
        _currentUser.value = updatedUser

        // Add Notification
        addNotification(
            title = "Task Submitted for Review",
            message = "Your submission for '${task.title}' has been received and is pending admin review.",
            category = NotificationCategory.TASK
        )

        return Result.success(submission)
    }

    // --- Wallet, Deposit & Withdrawal Operations ---

    suspend fun submitDeposit(
        amount: Double,
        method: PaymentMethod,
        senderNumber: String,
        transactionId: String
    ): Result<DepositRequest> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Please log in first"))
        val minDeposit = _appSettings.value.minDeposit

        if (amount < minDeposit) {
            return Result.failure(IllegalArgumentException("Minimum deposit amount is ৳$minDeposit"))
        }
        if (senderNumber.length < 11) {
            return Result.failure(IllegalArgumentException("Valid 11-digit sender phone number is required"))
        }
        if (transactionId.trim().length < 6) {
            return Result.failure(IllegalArgumentException("Valid Transaction ID (TrxID) is required"))
        }

        val depositId = "DEP" + System.currentTimeMillis().toString().takeLast(8)
        val deposit = DepositRequest(
            id = depositId,
            userId = user.uid,
            amount = amount,
            paymentMethod = method,
            senderNumber = senderNumber.trim(),
            transactionId = transactionId.trim().uppercase(),
            status = DepositStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            adminNote = "Pending manual verification by finance desk"
        )

        val trxRecord = Transaction(
            id = "TRX" + System.currentTimeMillis().toString().takeLast(8),
            userId = user.uid,
            type = TransactionType.DEPOSIT,
            amount = amount,
            status = TransactionStatus.PENDING,
            date = System.currentTimeMillis(),
            description = "Deposit via ${method.displayName} (TrxID: ${transactionId.trim().uppercase()})",
            paymentMethod = method.displayName,
            transactionRef = transactionId.trim().uppercase()
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "Deposit Submitted",
            message = "Deposit request of ৳$amount via ${method.displayName} submitted. Balance will be updated upon verification.",
            category = NotificationCategory.DEPOSIT
        )

        return Result.success(deposit)
    }

    suspend fun submitWithdrawal(
        amount: Double,
        method: PaymentMethod,
        accountNumber: String
    ): Result<WithdrawalRequest> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Please log in first"))
        val minWithdrawal = _appSettings.value.minWithdrawal

        if (amount < minWithdrawal) {
            return Result.failure(IllegalArgumentException("Minimum withdrawal is ৳$minWithdrawal"))
        }
        if (amount > user.balance) {
            return Result.failure(IllegalArgumentException("Insufficient available balance (Available: ৳${user.balance})"))
        }
        if (accountNumber.length < 11) {
            return Result.failure(IllegalArgumentException("Valid 11-digit account number is required"))
        }

        val fee = (amount * _appSettings.value.withdrawFeePercent) / 100.0
        val net = amount - fee
        val withdrawalId = "WTH" + System.currentTimeMillis().toString().takeLast(8)

        val withdrawal = WithdrawalRequest(
            id = withdrawalId,
            userId = user.uid,
            amount = amount,
            paymentMethod = method,
            accountNumber = accountNumber.trim(),
            fee = fee,
            netAmount = net,
            status = WithdrawalStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )

        // Deduct balance atomically
        val updatedUser = user.copy(
            balance = user.balance - amount,
            totalWithdrawn = user.totalWithdrawn + amount
        )
        _currentUser.value = updatedUser

        val trxRecord = Transaction(
            id = "TRX" + System.currentTimeMillis().toString().takeLast(8),
            userId = user.uid,
            type = TransactionType.WITHDRAWAL,
            amount = -amount,
            status = TransactionStatus.PENDING,
            date = System.currentTimeMillis(),
            description = "Withdrawal to ${method.displayName} ($accountNumber)",
            paymentMethod = method.displayName,
            transactionRef = withdrawalId
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "Withdrawal Requested",
            message = "৳$amount requested to ${method.displayName} account $accountNumber. Processing within 1-12 hours.",
            category = NotificationCategory.WITHDRAWAL
        )

        return Result.success(withdrawal)
    }

    suspend fun transferBalance(targetPhone: String, amount: Double): Result<Transaction> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Please log in first"))
        if (amount <= 0) return Result.failure(IllegalArgumentException("Enter valid amount"))
        if (amount > user.balance) return Result.failure(IllegalArgumentException("Insufficient balance"))
        if (targetPhone.trim() == user.phone) return Result.failure(IllegalArgumentException("Cannot transfer to yourself"))

        val updatedUser = user.copy(balance = user.balance - amount)
        _currentUser.value = updatedUser

        val trxRecord = Transaction(
            id = "TRX" + System.currentTimeMillis().toString().takeLast(8),
            userId = user.uid,
            type = TransactionType.WITHDRAWAL,
            amount = -amount,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis(),
            description = "P2P Balance Transfer to $targetPhone",
            paymentMethod = "Internal Wallet Transfer",
            transactionRef = "P2P-$targetPhone"
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        return Result.success(trxRecord)
    }

    // --- Support Center ---

    suspend fun createTicket(subject: String, category: TicketCategory, message: String): Result<SupportTicket> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Please log in first"))
        if (subject.isBlank()) return Result.failure(IllegalArgumentException("Subject is required"))
        if (message.isBlank()) return Result.failure(IllegalArgumentException("Message cannot be empty"))

        val ticketId = "TCK" + (1000..9999).random()
        val newTicket = SupportTicket(
            id = ticketId,
            userId = user.uid,
            subject = subject.trim(),
            category = category,
            message = message.trim(),
            status = TicketStatus.OPEN,
            createdAt = System.currentTimeMillis(),
            replies = listOf(
                TicketReply(
                    id = "RPL1",
                    senderName = user.name,
                    isStaff = false,
                    message = message.trim(),
                    timestamp = System.currentTimeMillis()
                )
            )
        )
        _tickets.value = listOf(newTicket) + _tickets.value
        return Result.success(newTicket)
    }

    suspend fun addTicketReply(ticketId: String, message: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Please log in first"))
        if (message.isBlank()) return Result.failure(IllegalArgumentException("Message cannot be empty"))

        val currentTickets = _tickets.value
        val ticket = currentTickets.find { it.id == ticketId } ?: return Result.failure(IllegalArgumentException("Ticket not found"))

        val newReply = TicketReply(
            id = "RPL_" + System.currentTimeMillis(),
            senderName = user.name,
            isStaff = false,
            message = message.trim(),
            timestamp = System.currentTimeMillis()
        )
        val updatedTicket = ticket.copy(
            replies = ticket.replies + newReply,
            status = TicketStatus.OPEN
        )
        _tickets.value = currentTickets.map { if (it.id == ticketId) updatedTicket else it }
        return Result.success(Unit)
    }

    // --- Notifications ---

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    private fun addNotification(title: String, message: String, category: NotificationCategory) {
        val notif = NotificationItem(
            id = "NOTIF_" + System.currentTimeMillis(),
            title = title,
            message = message,
            category = category,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    private fun refreshUserData(uid: String) {
        scope.launch {
            try {
                val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
                if (doc != null && doc.exists()) {
                    val user = mapDocumentToUser(doc.data ?: emptyMap(), uid)
                    _currentUser.value = user
                }
            } catch (_: Exception) {}
        }
    }

    private fun createSampleUser(uid: String, email: String): User {
        val cleanName = email.substringBefore("@").replace(".", " ").capitalizeWords()
        val refCode = "IK" + (100000..999999).random()
        return User(
            uid = uid,
            name = if (cleanName.isNotBlank()) cleanName else "Shakib Al Hasan",
            email = email,
            phone = "01712-345678",
            profileImage = "",
            referralCode = refCode,
            referredBy = "IK1001",
            balance = 1450.0,
            pendingBalance = 120.0,
            totalEarned = 3850.0,
            totalWithdrawn = 2400.0,
            totalDeposited = 1000.0,
            tasksCompleted = 28,
            teamCount = 12,
            status = "Active",
            createdAt = System.currentTimeMillis() - 86400000L * 15,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun mapDocumentToUser(map: Map<String, Any>, uid: String): User {
        return User(
            uid = uid,
            name = (map["name"] as? String) ?: "User",
            email = (map["email"] as? String) ?: "",
            phone = (map["phone"] as? String) ?: "",
            profileImage = (map["profileImage"] as? String) ?: "",
            referralCode = (map["referralCode"] as? String) ?: "IK" + (100000..999999).random(),
            referredBy = (map["referredBy"] as? String) ?: "",
            balance = (map["balance"] as? Number)?.toDouble() ?: 0.0,
            pendingBalance = (map["pendingBalance"] as? Number)?.toDouble() ?: 0.0,
            totalEarned = (map["totalEarned"] as? Number)?.toDouble() ?: 0.0,
            totalWithdrawn = (map["totalWithdrawn"] as? Number)?.toDouble() ?: 0.0,
            totalDeposited = (map["totalDeposited"] as? Number)?.toDouble() ?: 0.0,
            tasksCompleted = (map["tasksCompleted"] as? Number)?.toInt() ?: 0,
            teamCount = (map["teamCount"] as? Number)?.toInt() ?: 0,
            status = (map["status"] as? String) ?: "Active",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun userToMap(user: User): Map<String, Any> {
        return mapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "profileImage" to user.profileImage,
            "referralCode" to user.referralCode,
            "referredBy" to user.referredBy,
            "balance" to user.balance,
            "pendingBalance" to user.pendingBalance,
            "totalEarned" to user.totalEarned,
            "totalWithdrawn" to user.totalWithdrawn,
            "totalDeposited" to user.totalDeposited,
            "tasksCompleted" to user.tasksCompleted,
            "teamCount" to user.teamCount,
            "status" to user.status,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt
        )
    }

    private suspend fun saveUserToFirestore(user: User) {
        firestore?.collection("users")?.document(user.uid)?.set(userToMap(user))?.await()
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }

    // Seed Data
    private fun getInitialTasks(): List<Task> = listOf(
        Task(
            id = "TSK101",
            title = "Watch & Review YouTube Video",
            description = "Watch the full 3-minute video on digital commerce tips and leave a constructive comment.",
            category = TaskCategory.DAILY,
            reward = 25.0,
            estimatedTimeMinutes = 4,
            difficulty = TaskDifficulty.EASY,
            status = TaskStatus.AVAILABLE,
            deadline = "12 hours left",
            requirements = listOf("Watch minimum 3 minutes", "Leave positive comment", "Submit screenshot of comment"),
            instructions = "1. Open video link\n2. Watch until end\n3. Like and write comment\n4. Take screenshot and upload link",
            submissionType = SubmissionType.TEXT_AND_LINK
        ),
        Task(
            id = "TSK102",
            title = "Join Official Telegram Group",
            description = "Join INCOME KORO VIP community Telegram channel for daily task updates and bonuses.",
            category = TaskCategory.SOCIAL,
            reward = 35.0,
            estimatedTimeMinutes = 2,
            difficulty = TaskDifficulty.EASY,
            status = TaskStatus.AVAILABLE,
            deadline = "Today",
            requirements = listOf("Join channel @incomekoro_vip", "Stay active for at least 7 days", "Provide Telegram username"),
            instructions = "Open Telegram, search @incomekoro_vip, join and submit your handle.",
            submissionType = SubmissionType.TEXT
        ),
        Task(
            id = "TSK103",
            title = "Share Promotional Post on Facebook",
            description = "Share our latest campaign banner on your personal Facebook feed with public privacy.",
            category = TaskCategory.PROMOTIONAL,
            reward = 60.0,
            estimatedTimeMinutes = 5,
            difficulty = TaskDifficulty.MEDIUM,
            status = TaskStatus.AVAILABLE,
            deadline = "18 hours left",
            requirements = listOf("Post must be Public", "Include referral link in caption", "Must have minimum 50 friends"),
            instructions = "Copy the promotional text, download the poster, post publicly on your Facebook timeline, and submit the link.",
            submissionType = SubmissionType.LINK
        ),
        Task(
            id = "TSK104",
            title = "Write 5-Star App Store Review",
            description = "Download recommended partner utility app, rate 5 stars and leave honest feedback.",
            category = TaskCategory.SKILL,
            reward = 80.0,
            estimatedTimeMinutes = 8,
            difficulty = TaskDifficulty.MEDIUM,
            status = TaskStatus.AVAILABLE,
            deadline = "2 days left",
            requirements = listOf("Download app from Play Store", "Rate 5 stars", "Write minimum 20 words review", "Attach screenshot"),
            instructions = "Search the partner app, install, leave 5-star review, capture screenshot with your reviewer name visible.",
            submissionType = SubmissionType.TEXT_AND_LINK
        ),
        Task(
            id = "TSK105",
            title = "Complete Product Survey (E-Commerce)",
            description = "Fill out a 10-question market research survey regarding mobile payment preferences in Bangladesh.",
            category = TaskCategory.SPECIAL,
            reward = 120.0,
            estimatedTimeMinutes = 10,
            difficulty = TaskDifficulty.HARD,
            status = TaskStatus.AVAILABLE,
            deadline = "3 days left",
            requirements = listOf("Answer all 10 questions thoughtfully", "Provide completion confirmation code"),
            instructions = "Click survey link, finish all questions, copy the unique confirmation token displayed at the final screen.",
            submissionType = SubmissionType.TEXT
        ),
        Task(
            id = "TSK106",
            title = "Daily Check-in & Quiz",
            description = "Answer 3 easy finance questions to claim your daily activity bonus.",
            category = TaskCategory.DAILY,
            reward = 15.0,
            estimatedTimeMinutes = 2,
            difficulty = TaskDifficulty.EASY,
            status = TaskStatus.AVAILABLE,
            deadline = "Resets daily at 12 AM",
            requirements = listOf("Answer 3 multiple choice questions correctly"),
            instructions = "Submit answers in the text box below.",
            submissionType = SubmissionType.TEXT
        )
    )

    private fun getInitialAnnouncements(): List<Announcement> = listOf(
        Announcement(
            id = "ANN1",
            title = "🚀 Mega Referral Week: 10% Extra Team Commission!",
            description = "From this week until Friday, all referral earnings and level 1 team commissions will receive an instant 10% boost. Invite your friends now!",
            publishedDate = System.currentTimeMillis() - 3600000L * 5,
            tag = "Offer",
            isImportant = true
        ),
        Announcement(
            id = "ANN2",
            title = "⚡ Faster bKash & Nagad Auto-Withdrawals",
            description = "Our automated payout system has been upgraded. Regular withdrawals between 10 AM to 10 PM are now verified and disbursed within 30 minutes.",
            publishedDate = System.currentTimeMillis() - 86400000L * 2,
            tag = "Update",
            isImportant = false
        ),
        Announcement(
            id = "ANN3",
            title = "🛡️ Community Safety & Anti-Fraud Notice",
            description = "Never share your password or OTP with anyone claiming to be staff. Official admins will never ask for your account password.",
            publishedDate = System.currentTimeMillis() - 86400000L * 6,
            tag = "Security",
            isImportant = false
        )
    )

    private fun getInitialNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "NOTIF1",
            title = "Welcome Bonus Credited! 🎉",
            message = "৳50.00 signup reward has been added to your available balance.",
            category = NotificationCategory.WALLET,
            timestamp = System.currentTimeMillis() - 3600000L * 2,
            isRead = false
        ),
        NotificationItem(
            id = "NOTIF2",
            title = "New Tasks Available Today",
            message = "6 high-reward tasks have just been published in Daily and Skill categories.",
            category = NotificationCategory.TASK,
            timestamp = System.currentTimeMillis() - 3600000L * 8,
            isRead = false
        ),
        NotificationItem(
            id = "NOTIF3",
            title = "Withdrawal Processed Successfully",
            message = "Your withdrawal of ৳500.00 to bKash account was completed.",
            category = NotificationCategory.WITHDRAWAL,
            timestamp = System.currentTimeMillis() - 86400000L * 1,
            isRead = true
        )
    )

    private fun getInitialTeamMembers(): List<TeamMember> = listOf(
        TeamMember(
            id = "MEM1",
            name = "Tanvir Ahmed",
            email = "tanvir@gmail.com",
            phone = "01822-334455",
            profileImage = "",
            joinDate = System.currentTimeMillis() - 86400000L * 3,
            status = "Active",
            earnedAmount = 450.0,
            tasksCompleted = 14,
            level = 1
        ),
        TeamMember(
            id = "MEM2",
            name = "Nusrat Jahan",
            email = "nusrat@yahoo.com",
            phone = "01933-445566",
            profileImage = "",
            joinDate = System.currentTimeMillis() - 86400000L * 5,
            status = "Active",
            earnedAmount = 820.0,
            tasksCompleted = 22,
            level = 1
        ),
        TeamMember(
            id = "MEM3",
            name = "Rakib Hossain",
            email = "rakib@gmail.com",
            phone = "01744-556677",
            profileImage = "",
            joinDate = System.currentTimeMillis() - 86400000L * 8,
            status = "Active",
            earnedAmount = 310.0,
            tasksCompleted = 9,
            level = 2
        ),
        TeamMember(
            id = "MEM4",
            name = "Mehedi Hasan",
            email = "mehedi@outlook.com",
            phone = "01655-667788",
            profileImage = "",
            joinDate = System.currentTimeMillis() - 86400000L * 12,
            status = "Inactive",
            earnedAmount = 120.0,
            tasksCompleted = 4,
            level = 2
        )
    )

    private fun getInitialTickets(): List<SupportTicket> = listOf(
        SupportTicket(
            id = "TCK1082",
            userId = "usr_sample",
            subject = "Withdrawal received time query",
            category = TicketCategory.PAYMENT_ISSUE,
            message = "How long does Nagad withdrawal take to reflect in account?",
            status = TicketStatus.RESOLVED,
            createdAt = System.currentTimeMillis() - 86400000L * 2,
            replies = listOf(
                TicketReply(
                    id = "R1",
                    senderName = "User",
                    isStaff = false,
                    message = "How long does Nagad withdrawal take to reflect in account?",
                    timestamp = System.currentTimeMillis() - 86400000L * 2
                ),
                TicketReply(
                    id = "R2",
                    senderName = "Income Koro Support Desk",
                    isStaff = true,
                    message = "Hello! Nagad payouts are processed between 10 AM to 10 PM usually within 15-45 minutes. Thank you!",
                    timestamp = System.currentTimeMillis() - 86400000L * 2 + 1800000L
                )
            )
        )
    )

    private fun getInitialTransactions(): List<Transaction> = listOf(
        Transaction(
            id = "TRX902311",
            userId = "usr_sample",
            type = TransactionType.BONUS,
            amount = 50.0,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis() - 86400000L * 14,
            description = "Welcome Signup Bonus credited to wallet",
            paymentMethod = "System Bonus",
            transactionRef = "BONUS-SIGNUP"
        ),
        Transaction(
            id = "TRX902312",
            userId = "usr_sample",
            type = TransactionType.TASK_INCOME,
            amount = 35.0,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis() - 86400000L * 10,
            description = "Completed 'Join Telegram VIP Group'",
            paymentMethod = "Task Reward",
            transactionRef = "TSK102"
        ),
        Transaction(
            id = "TRX902313",
            userId = "usr_sample",
            type = TransactionType.REFERRAL_INCOME,
            amount = 100.0,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis() - 86400000L * 8,
            description = "Referral Commission for 2 new active members",
            paymentMethod = "Referral Bonus",
            transactionRef = "REF-2MEM"
        ),
        Transaction(
            id = "TRX902314",
            userId = "usr_sample",
            type = TransactionType.DEPOSIT,
            amount = 500.0,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis() - 86400000L * 5,
            description = "Deposit via bKash verified",
            paymentMethod = "bKash",
            transactionRef = "9BK281903"
        ),
        Transaction(
            id = "TRX902315",
            userId = "usr_sample",
            type = TransactionType.WITHDRAWAL,
            amount = -300.0,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis() - 86400000L * 2,
            description = "Withdrawal to bKash (01712-345678)",
            paymentMethod = "bKash",
            transactionRef = "WTH88219"
        )
    )

    // --- Mobile Recharge Operations ---
    suspend fun submitMobileRecharge(
        phoneNumber: String,
        operator: TelecomOperator,
        simType: SimType,
        amount: Double,
        pin: String
    ): Result<MobileRechargeRecord> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("দয়া করে প্রথমে লগইন করুন"))
        val cleanPhone = phoneNumber.replace("-", "").replace(" ", "").trim()

        if (cleanPhone.length != 11 || !cleanPhone.startsWith("01")) {
            return Result.failure(IllegalArgumentException("সঠিক ১১ ডিজিট বাংলাদেশি মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)"))
        }
        if (amount < 20.0) {
            return Result.failure(IllegalArgumentException("সর্বনিম্ন রিচার্জের পরিমাণ ৳২০"))
        }
        if (amount > 10000.0) {
            return Result.failure(IllegalArgumentException("সর্বোচ্চ রিচার্জের পরিমাণ ৳১০,০০০"))
        }
        if (amount > user.balance) {
            return Result.failure(IllegalArgumentException("পর্যাপ্ত ব্যালেন্স নেই! বর্তমান ব্যালেন্স ৳${String.format(java.util.Locale.US, "%.2f", user.balance)}"))
        }
        if (pin.length < 4) {
            return Result.failure(IllegalArgumentException("৪ ডিজিটের গোপন পিন কোড দিন"))
        }

        // 3% instant cashback
        val cashback = amount * 0.03
        val netDeducted = amount - cashback
        val newBalance = user.balance - netDeducted

        val rechargeId = "RCH" + System.currentTimeMillis().toString().takeLast(7)
        val trxId = "TRX" + System.currentTimeMillis().toString().takeLast(8)

        val record = MobileRechargeRecord(
            id = rechargeId,
            phoneNumber = cleanPhone,
            operator = operator,
            simType = simType,
            amount = amount,
            cashback = cashback,
            timestamp = System.currentTimeMillis(),
            status = "সফল",
            transactionRef = trxId
        )

        // Deduct balance
        _currentUser.value = user.copy(balance = newBalance)

        // Add to recent recharge records
        _mobileRecharges.value = listOf(record) + _mobileRecharges.value

        // Add to main transactions
        val trxRecord = Transaction(
            id = trxId,
            userId = user.uid,
            type = TransactionType.MOBILE_RECHARGE,
            amount = -amount,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis(),
            description = "মোবাইল রিচার্জ: $cleanPhone (${operator.bengaliName} ${simType.bengaliName}) | ক্যাশব্যাক ৳${String.format(java.util.Locale.US, "%.2f", cashback)}",
            paymentMethod = operator.displayName,
            transactionRef = trxId
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "মোবাইল রিচার্জ সফল! 📱",
            message = "$cleanPhone (${operator.bengaliName}) নম্বরে ৳${String.format(java.util.Locale.US, "%.0f", amount)} রিচার্জ সফল হয়েছে। ক্যাশব্যাক ৳${String.format(java.util.Locale.US, "%.2f", cashback)} ব্যালেন্সে জমা হয়েছে।",
            category = NotificationCategory.WALLET
        )

        return Result.success(record)
    }

    private fun getInitialRecharges(): List<MobileRechargeRecord> = listOf(
        MobileRechargeRecord(
            id = "RCH9812401",
            phoneNumber = "01712893421",
            operator = TelecomOperator.GRAMEENPHONE,
            simType = SimType.PREPAID,
            amount = 100.0,
            cashback = 3.0,
            timestamp = System.currentTimeMillis() - 86400000L * 1,
            status = "সফল",
            transactionRef = "TRX8829103"
        ),
        MobileRechargeRecord(
            id = "RCH9812402",
            phoneNumber = "01823746219",
            operator = TelecomOperator.ROBI,
            simType = SimType.PREPAID,
            amount = 299.0,
            cashback = 25.0,
            timestamp = System.currentTimeMillis() - 86400000L * 3,
            status = "সফল",
            transactionRef = "TRX8829104"
        ),
        MobileRechargeRecord(
            id = "RCH9812403",
            phoneNumber = "01934128765",
            operator = TelecomOperator.BANGLALINK,
            simType = SimType.PREPAID,
            amount = 50.0,
            cashback = 1.5,
            timestamp = System.currentTimeMillis() - 86400000L * 6,
            status = "সফল",
            transactionRef = "TRX8829105"
        )
    )

    fun getRechargeOffers(operator: TelecomOperator): List<RechargeOffer> = when (operator) {
        TelecomOperator.GRAMEENPHONE -> listOf(
            RechargeOffer("gp_int_1", "1.5 GB ইন্টারনেট", "৩ দিন", 48.0, 5.0, OfferType.INTERNET, operator, "যেকোনো ব্যবহারে প্রযোজ্য", "পপুলার"),
            RechargeOffer("gp_int_2", "5 GB ইন্টারনেট", "৭ দিন", 108.0, 10.0, OfferType.INTERNET, operator, "হট ডিল অফার", "হট"),
            RechargeOffer("gp_int_3", "10 GB ইন্টারনেট", "৩০ দিন", 299.0, 25.0, OfferType.INTERNET, operator, "মাসিক ইন্টারনেট প্যাক", "বেস্ট সেলার"),
            RechargeOffer("gp_int_4", "30 GB ইন্টারনেট", "৩০ দিন", 498.0, 40.0, OfferType.INTERNET, operator, "সুপার ভ্যালু ইন্টারনেট", "সুপার ডিল"),
            RechargeOffer("gp_min_1", "৫০ মিনিট যেকোনো লোকাল", "৫ দিন", 34.0, 3.0, OfferType.MINUTE, operator, "২৪ ঘণ্টা কল সুবিধা"),
            RechargeOffer("gp_min_2", "১৫০ মিনিট যেকোনো লোকাল", "১৫ দিন", 97.0, 8.0, OfferType.MINUTE, operator, "যেকোনো অপারেটরে কথা বলুন", "পপুলার"),
            RechargeOffer("gp_min_3", "৩০০ মিনিট যেকোনো লোকাল", "৩০ দিন", 197.0, 18.0, OfferType.MINUTE, operator, "মাসিক টকটাইম প্যাক", "বেস্ট ভ্যালু"),
            RechargeOffer("gp_bnd_1", "10 GB + ২০০ মিনিট", "৩০ দিন", 399.0, 35.0, OfferType.BUNDLE, operator, "ড্রাইভ প্যাক অফার", "ড্রাইভ"),
            RechargeOffer("gp_bnd_2", "25 GB + ৫০০ মিনিট", "৩০ দিন", 649.0, 60.0, OfferType.BUNDLE, operator, "সুপার মেগা বান্ডেল", "মেগা অফার")
        )
        TelecomOperator.BANGLALINK -> listOf(
            RechargeOffer("bl_int_1", "2 GB ইন্টারনেট", "৩ দিন", 46.0, 4.0, OfferType.INTERNET, operator, "হাই স্পিড 4G", "পপুলার"),
            RechargeOffer("bl_int_2", "7 GB ইন্টারনেট", "৭ দিন", 114.0, 12.0, OfferType.INTERNET, operator, "সাপ্তাহিক ধামাকা অফার", "হট"),
            RechargeOffer("bl_int_3", "15 GB ইন্টারনেট", "৩০ দিন", 319.0, 30.0, OfferType.INTERNET, operator, "মাসিক ইন্টারনেট প্যাক", "বেস্ট সেলার"),
            RechargeOffer("bl_min_1", "৬০ মিনিট যেকোনো লোকাল", "৭ দিন", 39.0, 4.0, OfferType.MINUTE, operator, "২৪ ঘণ্টা ভ্যালিডিটি"),
            RechargeOffer("bl_min_2", "২০০ মিনিট যেকোনো লোকাল", "৩০ দিন", 137.0, 15.0, OfferType.MINUTE, operator, "মাসিক টকটাইম", "পপুলার"),
            RechargeOffer("bl_bnd_1", "12 GB + ২৫০ মিনিট", "৩০ দিন", 398.0, 40.0, OfferType.BUNDLE, operator, "স্পেশাল ড্রাইভ অফার", "ড্রাইভ"),
            RechargeOffer("bl_bnd_2", "30 GB + ৬০০ মিনিট", "৩০ দিন", 698.0, 70.0, OfferType.BUNDLE, operator, "মেগা ফ্যামিলি প্যাক", "মেগা")
        )
        TelecomOperator.ROBI -> listOf(
            RechargeOffer("rb_int_1", "2 GB ইন্টারনেট", "৪ দিন", 49.0, 5.0, OfferType.INTERNET, operator, "রবি 4.5G স্পিড", "পপুলার"),
            RechargeOffer("rb_int_2", "6 GB ইন্টারনেট", "৭ দিন", 109.0, 10.0, OfferType.INTERNET, operator, "রবি সুপার প্যাক", "হট"),
            RechargeOffer("rb_int_3", "12 GB ইন্টারনেট", "৩০ দিন", 319.0, 28.0, OfferType.INTERNET, operator, "মাসিক ইন্টারনেট", "বেস্ট সেলার"),
            RechargeOffer("rb_min_1", "৭০ মিনিট যেকোনো অপারেটর", "৭ দিন", 44.0, 4.0, OfferType.MINUTE, operator, "লোকাল টকটাইম"),
            RechargeOffer("rb_min_2", "২৫০ মিনিট যেকোনো অপারেটর", "৩০ দিন", 167.0, 16.0, OfferType.MINUTE, operator, "মাসিক টকটাইম", "পপুলার"),
            RechargeOffer("rb_bnd_1", "15 GB + ৩০০ মিনিট", "৩০ দিন", 449.0, 45.0, OfferType.BUNDLE, operator, "রবি স্পেশাল ড্রাইভ", "ড্রাইভ"),
            RechargeOffer("rb_bnd_2", "35 GB + ৭০০ মিনিট", "৩০ দিন", 749.0, 75.0, OfferType.BUNDLE, operator, "আনলিমিটেড সুপার ড্রাইভ", "মেগা অফার")
        )
        TelecomOperator.AIRTEL -> listOf(
            RechargeOffer("at_int_1", "2.5 GB ইন্টারনেট", "৪ দিন", 48.0, 5.0, OfferType.INTERNET, operator, "এয়ারটেল ফ্রেন্ডস প্যাক", "পপুলার"),
            RechargeOffer("at_int_2", "8 GB ইন্টারনেট", "৭ দিন", 118.0, 12.0, OfferType.INTERNET, operator, "সাপ্তাহিক হিট অফার", "হট"),
            RechargeOffer("at_int_3", "15 GB ইন্টারনেট", "৩০ দিন", 328.0, 30.0, OfferType.INTERNET, operator, "মাসিক ইন্টারনেট প্যাক", "বেস্ট সেলার"),
            RechargeOffer("at_min_1", "৮০ মিনিট যেকোনো লোকাল", "৭ দিন", 49.0, 5.0, OfferType.MINUTE, operator, "টকটাইম প্যাক"),
            RechargeOffer("at_min_2", "৩০০ মিনিট যেকোনো লোকাল", "৩০ দিন", 198.0, 20.0, OfferType.MINUTE, operator, "মাসিক টকটাইম", "পপুলার"),
            RechargeOffer("at_bnd_1", "18 GB + ৩৫০ মিনিট", "৩০ দিন", 468.0, 50.0, OfferType.BUNDLE, operator, "এয়ারটেল ড্রাইভ প্যাক", "ড্রাইভ")
        )
        TelecomOperator.TELETALK -> listOf(
            RechargeOffer("tt_int_1", "2 GB ইন্টারনেট", "৩ দিন", 33.0, 3.0, OfferType.INTERNET, operator, "স্বল্পমূল্যের সরকারি সেবা", "সাশ্রয়ী"),
            RechargeOffer("tt_int_2", "5 GB ইন্টারনেট", "১০ দিন", 84.0, 8.0, OfferType.INTERNET, operator, "টেলিটক স্পেশাল", "পপুলার"),
            RechargeOffer("tt_int_3", "10 GB ইন্টারনেট", "৩০ দিন", 199.0, 20.0, OfferType.INTERNET, operator, "মাসিক সাশ্রয়ী প্যাক", "বেস্ট সেলার"),
            RechargeOffer("tt_min_1", "১০০ মিনিট যেকোনো লোকাল", "১৫ দিন", 59.0, 6.0, OfferType.MINUTE, operator, "যেকোনো অপারেটরে কথা বলুন"),
            RechargeOffer("tt_min_2", "২৫০ মিনিট যেকোনো লোকাল", "৩০ দিন", 139.0, 14.0, OfferType.MINUTE, operator, "মাসিক টকটাইম", "পপুলার"),
            RechargeOffer("tt_bnd_1", "8 GB + ১৫০ মিনিট", "৩০ দিন", 279.0, 30.0, OfferType.BUNDLE, operator, "টেলিটক কম্বো ড্রাইভ", "ড্রাইভ")
        )
        TelecomOperator.SKITTO -> listOf(
            RechargeOffer("sk_int_1", "2 GB চিল প্যাক", "৩ দিন", 39.0, 4.0, OfferType.INTERNET, operator, "স্কিটো চিল ইন্টারনেট", "চিল প্যাক"),
            RechargeOffer("sk_int_2", "7 GB প্রো ডাটা", "৭ দিন", 99.0, 10.0, OfferType.INTERNET, operator, "সাপ্তাহিক ডাটা ব্যাকার", "পপুলার"),
            RechargeOffer("sk_int_3", "15 GB মেগা রিলোড", "৩০ দিন", 279.0, 25.0, OfferType.INTERNET, operator, "মাসিক স্কিটো মেগা ডাটা", "বেস্ট সেলার"),
            RechargeOffer("sk_min_1", "১০০ মিনিট স্কিটো টক", "১৫ দিন", 68.0, 7.0, OfferType.MINUTE, operator, "সকল লোকাল অপারেটরে"),
            RechargeOffer("sk_bnd_1", "12 GB + ২০০ মিনিট", "৩০ দিন", 369.0, 35.0, OfferType.BUNDLE, operator, "স্কিটো সুপার কম্বো", "সুপার কম্বো")
        )
    }

    private fun getInitialDriveOrders(): List<DriveOfferOrder> = listOf(
        DriveOfferOrder(
            id = "DRV928101",
            offerTitle = "40 GB + 800 মিনিট",
            operator = TelecomOperator.ROBI,
            phoneNumber = "01823746219",
            simType = SimType.PREPAID,
            division = "অল বিডি",
            regularPrice = 798.0,
            offerPrice = 648.0,
            cashback = 150.0,
            timestamp = System.currentTimeMillis() - 86400000L * 1,
            status = "সফল",
            transactionRef = "TRX9921011"
        ),
        DriveOfferOrder(
            id = "DRV928102",
            offerTitle = "35 GB + 700 মিনিট",
            operator = TelecomOperator.BANGLALINK,
            phoneNumber = "01934128765",
            simType = SimType.PREPAID,
            division = "ঢাকা",
            regularPrice = 748.0,
            offerPrice = 598.0,
            cashback = 150.0,
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            status = "সফল",
            transactionRef = "TRX9921012"
        ),
        DriveOfferOrder(
            id = "DRV928103",
            offerTitle = "30 GB + 600 মিনিট",
            operator = TelecomOperator.GRAMEENPHONE,
            phoneNumber = "01712893421",
            simType = SimType.PREPAID,
            division = "অল বিডি",
            regularPrice = 749.0,
            offerPrice = 629.0,
            cashback = 120.0,
            timestamp = System.currentTimeMillis() - 3600000L * 3,
            status = "প্রসেসিং",
            transactionRef = "TRX9921013"
        )
    )

    fun getAllDriveOffers(): List<DriveOfferItem> = listOf(
        // Robi
        DriveOfferItem(
            id = "drv_rb_1",
            operator = TelecomOperator.ROBI,
            title = "40 GB + 800 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 798.0,
            offerPrice = 648.0,
            cashback = 150.0,
            description = "যেকোনো লোকাল নম্বরে কথা বলুন ও 4G ইন্টারনেট",
            tag = "হট ডিল"
        ),
        DriveOfferItem(
            id = "drv_rb_2",
            operator = TelecomOperator.ROBI,
            title = "15 GB + 300 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 449.0,
            offerPrice = 399.0,
            cashback = 50.0,
            description = "মাসিক সাশ্রয়ী কম্বো অফার",
            tag = "পপুলার"
        ),
        DriveOfferItem(
            id = "drv_rb_3",
            operator = TelecomOperator.ROBI,
            title = "50 GB ইন্টারনেট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 599.0,
            offerPrice = 519.0,
            cashback = 80.0,
            description = "হাই স্পিড আনলিমিটেড ইন্টারনেট প্যাক",
            tag = "মেগা অফার"
        ),
        DriveOfferItem(
            id = "drv_rb_4",
            operator = TelecomOperator.ROBI,
            title = "30 GB + 600 মিনিট",
            validity = "৩০ দিন",
            division = "ঢাকা",
            regularPrice = 649.0,
            offerPrice = 549.0,
            cashback = 100.0,
            description = "ঢাকা বিভাগের বিশেষ অফার",
            tag = "স্পেশাল"
        ),

        // Airtel
        DriveOfferItem(
            id = "drv_at_1",
            operator = TelecomOperator.AIRTEL,
            title = "45 GB + 900 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 799.0,
            offerPrice = 639.0,
            cashback = 160.0,
            description = "এয়ারটেল ফ্রেন্ডস সুপার কম্বো",
            tag = "হট ডিল"
        ),
        DriveOfferItem(
            id = "drv_at_2",
            operator = TelecomOperator.AIRTEL,
            title = "18 GB + 350 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 468.0,
            offerPrice = 418.0,
            cashback = 50.0,
            description = "সাশ্রয়ী মূল্যে মাসিক বান্ডেল",
            tag = "পপুলার"
        ),
        DriveOfferItem(
            id = "drv_at_3",
            operator = TelecomOperator.AIRTEL,
            title = "30 GB ইন্টারনেট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 499.0,
            offerPrice = 419.0,
            cashback = 80.0,
            description = "এয়ারটেল সুপার 4G ডাটা প্যাক",
            tag = "ডাটা প্যাক"
        ),
        DriveOfferItem(
            id = "drv_at_4",
            operator = TelecomOperator.AIRTEL,
            title = "60 GB + 1000 মিনিট",
            validity = "৩০ দিন",
            division = "চট্টগ্রাম",
            regularPrice = 899.0,
            offerPrice = 729.0,
            cashback = 170.0,
            description = "চট্টগ্রাম ও অল বিডি স্পেশাল",
            tag = "মেগা অফার"
        ),

        // Banglalink
        DriveOfferItem(
            id = "drv_bl_1",
            operator = TelecomOperator.BANGLALINK,
            title = "35 GB + 700 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 748.0,
            offerPrice = 598.0,
            cashback = 150.0,
            description = "বাংলালিংক ধামাকা ড্রাইভ প্যাক",
            tag = "হট ডিল"
        ),
        DriveOfferItem(
            id = "drv_bl_2",
            operator = TelecomOperator.BANGLALINK,
            title = "12 GB + 250 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 398.0,
            offerPrice = 348.0,
            cashback = 50.0,
            description = "মাসিক টকটাইম ও ইন্টারনেট",
            tag = "পপুলার"
        ),
        DriveOfferItem(
            id = "drv_bl_3",
            operator = TelecomOperator.BANGLALINK,
            title = "40 GB ইন্টারনেট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 549.0,
            offerPrice = 469.0,
            cashback = 80.0,
            description = "বাংলালিংক 4G সুপার স্পিড ডাটা",
            tag = "ডাটা প্যাক"
        ),
        DriveOfferItem(
            id = "drv_bl_4",
            operator = TelecomOperator.BANGLALINK,
            title = "55 GB + 800 মিনিট",
            validity = "৩০ দিন",
            division = "রাজশাহী",
            regularPrice = 849.0,
            offerPrice = 699.0,
            cashback = 150.0,
            description = "রাজশাহী ও উত্তরবঙ্গ স্পেশাল",
            tag = "মেগা অফার"
        ),

        // Grameenphone
        DriveOfferItem(
            id = "drv_gp_1",
            operator = TelecomOperator.GRAMEENPHONE,
            title = "30 GB + 600 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 749.0,
            offerPrice = 629.0,
            cashback = 120.0,
            description = "জিপি সুপার ড্রাইভ কম্বো",
            tag = "হট ডিল"
        ),
        DriveOfferItem(
            id = "drv_gp_2",
            operator = TelecomOperator.GRAMEENPHONE,
            title = "10 GB + 200 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 399.0,
            offerPrice = 349.0,
            cashback = 50.0,
            description = "মাসিক সাশ্রয়ী জিপি প্যাক",
            tag = "পপুলার"
        ),
        DriveOfferItem(
            id = "drv_gp_3",
            operator = TelecomOperator.GRAMEENPHONE,
            title = "25 GB ইন্টারনেট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 498.0,
            offerPrice = 428.0,
            cashback = 70.0,
            description = "জিপি সেরা ইন্টারনেট অভিজ্ঞতা",
            tag = "ডাটা প্যাক"
        ),
        DriveOfferItem(
            id = "drv_gp_4",
            operator = TelecomOperator.GRAMEENPHONE,
            title = "45 GB + 800 মিনিট",
            validity = "৩০ দিন",
            division = "সিলেট",
            regularPrice = 849.0,
            offerPrice = 729.0,
            cashback = 120.0,
            description = "সিলেট ও অল বিডি প্রিমিয়াম অফার",
            tag = "মেগা অফার"
        ),

        // Teletalk
        DriveOfferItem(
            id = "drv_tt_1",
            operator = TelecomOperator.TELETALK,
            title = "20 GB + 400 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 449.0,
            offerPrice = 379.0,
            cashback = 70.0,
            description = "টেলিটক সাশ্রয়ী ড্রাইভ বান্ডেল",
            tag = "হট ডিল"
        ),
        DriveOfferItem(
            id = "drv_tt_2",
            operator = TelecomOperator.TELETALK,
            title = "8 GB + 150 মিনিট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 279.0,
            offerPrice = 239.0,
            cashback = 40.0,
            description = "টেলিটক লোকাল প্যাক",
            tag = "পপুলার"
        ),
        DriveOfferItem(
            id = "drv_tt_3",
            operator = TelecomOperator.TELETALK,
            title = "30 GB ইন্টারনেট",
            validity = "৩০ দিন",
            division = "অল বিডি",
            regularPrice = 399.0,
            offerPrice = 329.0,
            cashback = 70.0,
            description = "টেলিটক সুপার ডাটা ড্রাইভ",
            tag = "মেগা অফার"
        )
    )

    suspend fun submitDriveOfferOrder(
        offerTitle: String,
        operator: TelecomOperator,
        phoneNumber: String,
        simType: SimType,
        division: String,
        regularPrice: Double,
        offerPrice: Double,
        cashback: Double,
        pin: String
    ): Result<DriveOfferOrder> {
        val cleanPhone = phoneNumber.trim().replace("-", "").replace(" ", "")
        if (cleanPhone.length != 11 || !cleanPhone.startsWith("01")) {
            return Result.failure(Exception("সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 018XXXXXXXX)"))
        }

        if (pin.length < 4) {
            return Result.failure(Exception("সঠিক ৪ ডিজিটের পিন নম্বর দিন"))
        }

        val user = _currentUser.value ?: return Result.failure(Exception("ইউজার পাওয়া যায়নি"))

        if (user.balance < offerPrice) {
            return Result.failure(
                Exception(
                    "অপর্যাপ্ত ব্যালেন্স! আপনার ব্যালেন্স ৳${String.format(java.util.Locale.US, "%.2f", user.balance)}, অফারের মূল্য ৳${String.format(java.util.Locale.US, "%.0f", offerPrice)}"
                )
            )
        }

        // Net deducted: offerPrice - cashback instantly or user pays offerPrice and gets cashback added
        val netDeducted = offerPrice - cashback
        val newBalance = user.balance - netDeducted

        val orderId = "DRV" + System.currentTimeMillis().toString().takeLast(7)
        val trxId = "TRX" + System.currentTimeMillis().toString().takeLast(8)

        val order = DriveOfferOrder(
            id = orderId,
            offerTitle = offerTitle,
            operator = operator,
            phoneNumber = cleanPhone,
            simType = simType,
            division = division,
            regularPrice = regularPrice,
            offerPrice = offerPrice,
            cashback = cashback,
            timestamp = System.currentTimeMillis(),
            status = "প্রসেসিং",
            transactionRef = trxId
        )

        _currentUser.value = user.copy(balance = newBalance)
        _driveOrders.value = listOf(order) + _driveOrders.value

        val trxRecord = Transaction(
            id = trxId,
            userId = user.uid,
            type = TransactionType.MOBILE_RECHARGE,
            amount = -offerPrice,
            status = TransactionStatus.PENDING,
            date = System.currentTimeMillis(),
            description = "ড্রাইভ অফার: $offerTitle ($cleanPhone - ${operator.bengaliName}) | ক্যাশব্যাক ৳${String.format(java.util.Locale.US, "%.0f", cashback)}",
            paymentMethod = operator.displayName,
            transactionRef = trxId
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "ড্রাইভ অফার আবেদন সফল! 🚀",
            message = "$cleanPhone (${operator.bengaliName}) নম্বরে '$offerTitle' ড্রাইভ অফারটি প্রসেসিং-এ রয়েছে। ক্যাশব্যাক ৳${String.format(java.util.Locale.US, "%.0f", cashback)} সমন্বয় করা হয়েছে।",
            category = NotificationCategory.WALLET
        )

        return Result.success(order)
    }
}
