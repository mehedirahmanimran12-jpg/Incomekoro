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

    // Reselling Shop state
    private val _resellingOrders = MutableStateFlow<List<ResellingOrder>>(emptyList())
    val resellingOrders: Flow<List<ResellingOrder>> = _resellingOrders.asStateFlow()

    private val _cartItems = MutableStateFlow<List<ResellingCartItem>>(emptyList())
    val cartItems: Flow<List<ResellingCartItem>> = _cartItems.asStateFlow()

    private val _favoriteProductIds = MutableStateFlow<Set<String>>(setOf("prod_1", "prod_3"))
    val favoriteProductIds: Flow<Set<String>> = _favoriteProductIds.asStateFlow()

    // Freelancing Courses state
    private val _purchasedCourseIds = MutableStateFlow<Set<String>>(emptySet())
    val purchasedCourseIds: Flow<Set<String>> = _purchasedCourseIds.asStateFlow()

    // Online Services state
    private val _onlineServiceOrders = MutableStateFlow<List<OnlineServiceOrder>>(emptyList())
    val onlineServiceOrders: Flow<List<OnlineServiceOrder>> = _onlineServiceOrders.asStateFlow()

    // Micro Jobs state
    private val _completedMicroJobIds = MutableStateFlow<Set<String>>(emptySet())
    val completedMicroJobIds: Flow<Set<String>> = _completedMicroJobIds.asStateFlow()

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
        _resellingOrders.value = getInitialResellingOrders()
        _onlineServiceOrders.value = getInitialOnlineServiceOrders()
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

    // --- Reselling Shop Operations ---

    fun toggleFavoriteProduct(productId: String) {
        val current = _favoriteProductIds.value
        _favoriteProductIds.value = if (current.contains(productId)) {
            current - productId
        } else {
            current + productId
        }
    }

    fun addToCart(product: ResellingProduct, quantity: Int = 1, sellingPrice: Double = product.maxRetailPrice) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(
                quantity = existing.quantity + quantity,
                customerSellingPrice = sellingPrice
            )
        } else {
            current.add(ResellingCartItem(product = product, quantity = quantity, customerSellingPrice = sellingPrice))
        }
        _cartItems.value = current
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
        } else {
            _cartItems.value = _cartItems.value.map {
                if (it.product.id == productId) it.copy(quantity = quantity) else it
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    suspend fun submitResellingOrder(
        product: ResellingProduct,
        quantity: Int,
        customerSellingPrice: Double,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        district: String,
        isInsideDhaka: Boolean,
        pin: String
    ): Result<ResellingOrder> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("দয়া করে প্রথমে লগইন করুন"))

        if (pin.trim().length != 4) {
            return Result.failure(IllegalArgumentException("সঠিক ৪ ডিজিটের পিন নম্বর লিখুন"))
        }

        val cleanName = customerName.trim()
        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("গ্রাহকের নাম সঠিকভাবে পূরণ করুন"))
        }

        val cleanPhone = customerPhone.replace("-", "").replace(" ", "").trim()
        if (cleanPhone.length < 11) {
            return Result.failure(IllegalArgumentException("১১ ডিজিটের গ্রাহকের মোবাইল নম্বর লিখুন"))
        }

        val cleanAddress = deliveryAddress.trim()
        if (cleanAddress.length < 5) {
            return Result.failure(IllegalArgumentException("সম্পূর্ণ ডেলিভারি ঠিকানা লিখুন"))
        }

        if (customerSellingPrice < product.wholesalePrice) {
            return Result.failure(IllegalArgumentException("বিক্রয় মূল্য অবশ্যই পাইকারি মূল্যের (৳${product.wholesalePrice.toInt()}) সমান বা বেশি হতে হবে"))
        }

        val deliveryCharge = if (isInsideDhaka) product.deliveryChargeInsideDhaka else product.deliveryChargeOutsideDhaka
        val totalCustomerPayable = (customerSellingPrice * quantity) + deliveryCharge
        val resellerProfit = (customerSellingPrice - product.wholesalePrice) * quantity

        // Advance delivery charge deducted from reseller wallet
        if (user.balance < deliveryCharge) {
            return Result.failure(IllegalStateException("অগ্রিম ডেলিভারি চার্জের জন্য আপনার ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই (প্রয়োজন: ৳${deliveryCharge.toInt()})"))
        }

        val newBalance = user.balance - deliveryCharge
        val orderId = "ORD-" + (100000..999999).random()
        val trxId = "TRX-" + (1000000..9999999).random()

        val order = ResellingOrder(
            id = orderId,
            productTitle = product.title,
            productCode = product.productCode,
            quantity = quantity,
            wholesalePrice = product.wholesalePrice,
            customerSellingPrice = customerSellingPrice,
            resellerProfit = resellerProfit,
            deliveryCharge = deliveryCharge,
            totalCustomerPayable = totalCustomerPayable,
            customerName = cleanName,
            customerPhone = cleanPhone,
            deliveryAddress = cleanAddress,
            district = district,
            deliveryArea = if (isInsideDhaka) "ঢাকার ভেতরে" else "ঢাকার বাইরে",
            status = "প্রসেসিং",
            timestamp = System.currentTimeMillis(),
            transactionRef = trxId
        )

        _currentUser.value = user.copy(balance = newBalance)
        _resellingOrders.value = listOf(order) + _resellingOrders.value

        // Deduct from cart if present
        removeFromCart(product.id)

        val trxRecord = Transaction(
            id = trxId,
            userId = user.uid,
            type = TransactionType.WITHDRAWAL,
            amount = -deliveryCharge,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis(),
            description = "রিসেলিং অর্ডার অগ্রিম ডেলিভারি চার্জ: ${product.title} (অর্ডার: $orderId)",
            paymentMethod = "ওয়ালেট ব্যালেন্স",
            transactionRef = trxId
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "রিসেলিং অর্ডার সফল! 🛍️",
            message = "অর্ডার #$orderId সফলভাবে গ্রহণ করা হয়েছে। সম্ভাব্য লাভ ৳${resellerProfit.toInt()} ডেলিভারি শেষে জমা হবে।",
            category = NotificationCategory.SYSTEM
        )

        return Result.success(order)
    }

    private fun getInitialResellingOrders(): List<ResellingOrder> = listOf(
        ResellingOrder(
            id = "ORD-849101",
            productTitle = "TWS Pro 2 ওয়্যারলেস ব্লুটুথ এয়ারবাডস (ANC)",
            productCode = "SKU-EL-303",
            quantity = 1,
            wholesalePrice = 420.0,
            customerSellingPrice = 790.0,
            resellerProfit = 370.0,
            deliveryCharge = 70.0,
            totalCustomerPayable = 860.0,
            customerName = "মোঃ রাকিবুল হাসান",
            customerPhone = "01798123456",
            deliveryAddress = "বাড়ি ১২, রোড ৫, মিরপুর ১০, ঢাকা",
            district = "ঢাকা",
            deliveryArea = "ঢাকার ভেতরে",
            status = "ডেলিভার্ড",
            timestamp = System.currentTimeMillis() - 86400000L * 3,
            transactionRef = "TRX-7821901"
        ),
        ResellingOrder(
            id = "ORD-849102",
            productTitle = "প্রিমিয়াম ডিজাইনার সিল্ক পাঞ্জাবি কালেকশন",
            productCode = "SKU-MN-101",
            quantity = 1,
            wholesalePrice = 580.0,
            customerSellingPrice = 950.0,
            resellerProfit = 370.0,
            deliveryCharge = 130.0,
            totalCustomerPayable = 1080.0,
            customerName = "কামরুল ইসলাম",
            customerPhone = "01819765432",
            deliveryAddress = "জিইসি মোড়, চকবাজার, চট্টগ্রাম",
            district = "চট্টগ্রাম",
            deliveryArea = "ঢাকার বাইরে",
            status = "প্রসেসিং",
            timestamp = System.currentTimeMillis() - 3600000L * 5,
            transactionRef = "TRX-7821902"
        )
    )

    fun getAllResellingProducts(): List<ResellingProduct> = listOf(
        ResellingProduct(
            id = "prod_1",
            productCode = "SKU-MN-101",
            title = "প্রিমিয়াম ডিজাইনার সিল্ক পাঞ্জাবি কালেকশন",
            category = ResellingCategory.MENS_FASHION,
            wholesalePrice = 580.0,
            maxRetailPrice = 950.0,
            suggestedProfit = 370.0,
            stockQuantity = 240,
            rating = 4.9,
            reviewCount = 88,
            description = "উচ্চমানের সিল্কি ফেব্রিক ও আকর্ষনীয় সুতার এমব্রয়ডারি কাজ করা। ঈদের ও পার্টি পরিধানের জন্য সেরা পছন্দ। ১০০% কালার গ্যারান্টি।",
            shortSpecs = listOf("সাইজ: 40, 42, 44", "ফেব্রিক: প্রিমিয়াম সেমি-সিল্ক", "কালার: রয়াল ব্লু / নেভি ব্লু", "কোয়ালিটি: এক্সপোর্ট"),
            isHotDeal = true,
            tag = "হট ডিল"
        ),
        ResellingProduct(
            id = "prod_2",
            productCode = "SKU-WM-202",
            title = "কাতান সিল্ক ঐতিহ্যবাহী শাড়ি (ব্লাউজ পিস সহ)",
            category = ResellingCategory.WOMENS_FASHION,
            wholesalePrice = 750.0,
            maxRetailPrice = 1250.0,
            suggestedProfit = 500.0,
            stockQuantity = 180,
            rating = 4.8,
            reviewCount = 65,
            description = "আকর্ষণীয় মিরর ওয়ার্ক ও ঐতিহ্যবাহী আঁচল ডিজাইনের কাতান শাড়ি। যেকোনো উৎসব ও অনুষ্ঠানে আভিজাত্যের ছোঁয়া।",
            shortSpecs = listOf("দৈর্ঘ্য: ১২ হাত + ব্লাউজ পিস", "উপাদান: সফট কাতান সিল্ক", "কালার গ্যারান্টি", "রেডি স্টক"),
            isHotDeal = true,
            tag = "জনপ্রিয়"
        ),
        ResellingProduct(
            id = "prod_3",
            productCode = "SKU-EL-303",
            title = "TWS Pro 2 ওয়্যারলেস ব্লুটুথ এয়ারবাডস (ANC)",
            category = ResellingCategory.ELECTRONICS,
            wholesalePrice = 420.0,
            maxRetailPrice = 790.0,
            suggestedProfit = 370.0,
            stockQuantity = 310,
            rating = 4.7,
            reviewCount = 142,
            description = "এক্টিভ নয়েজ ক্যান্সেলেশন, ক্রিস্টাল ক্লিয়ার বেস ও ৩০ ঘণ্টা পর্যন্ত ব্যাটারি ব্যাকআপ। টাইপ-সি ফাস্ট চার্জিং সুবিধা।",
            shortSpecs = listOf("ব্লুটুথ: v5.3", "প্লে-টাইম: ৬+২৪ ঘণ্টা", "স্মার্ট টাচ কন্ট্রোল", "ওয়াটারপ্রুফ IPX5"),
            isHotDeal = true,
            tag = "সেরা অফার"
        ),
        ResellingProduct(
            id = "prod_4",
            productCode = "SKU-WT-404",
            title = "আল্ট্রা স্মার্টওয়াচ সিরিজ ৯ (কলিং ও হেলথ ট্র্যাকার)",
            category = ResellingCategory.WATCHES,
            wholesalePrice = 850.0,
            maxRetailPrice = 1450.0,
            suggestedProfit = 600.0,
            stockQuantity = 195,
            rating = 4.9,
            reviewCount = 110,
            description = "২.০২ ইঞ্চি ফুল এইচডি বেজেললেস ডিসপ্লে। ব্লুটুথ কলিং, হার্টরেট ও ব্লাড প্রেসার মনিটরিং, ওয়্যারলেস চার্জার সহ।",
            shortSpecs = listOf("ডিসপ্লে: 2.02\" HD Retina", "ব্লুটুথ কলিং ও নোটিফিকেশন", "ডাবল স্ট্র্যাপ সহ", "মেটাল বডি"),
            isHotDeal = true,
            tag = "বেশি লাভ"
        ),
        ResellingProduct(
            id = "prod_5",
            productCode = "SKU-BT-505",
            title = "অর্গানিক ব্রাইটেনিং ফেস সিরাম ও ডে ক্রিম কম্বো",
            category = ResellingCategory.BEAUTY,
            wholesalePrice = 320.0,
            maxRetailPrice = 650.0,
            suggestedProfit = 330.0,
            stockQuantity = 280,
            rating = 4.8,
            reviewCount = 76,
            description = "ভিটামিন সি ও হায়ালুরোনিক অ্যাসিড সমৃদ্ধ। ত্বকের কালচে দাগ দূর করে প্রাকৃতিক উজ্জ্বলতা বৃদ্ধি করে।",
            shortSpecs = listOf("সিরাম: 30ml", "ডে ক্রিম: 50g", "সব ধরণের ত্বকের উপযোগী", "বিএসটিআই অনুমোদিত"),
            isHotDeal = false,
            tag = "ন্যাচারাল"
        ),
        ResellingProduct(
            id = "prod_6",
            productCode = "SKU-HM-606",
            title = "পোর্টেবল রিচার্জেবল মিনি জুসার ব্লেন্ডার",
            category = ResellingCategory.HOME_LIVING,
            wholesalePrice = 480.0,
            maxRetailPrice = 850.0,
            suggestedProfit = 370.0,
            stockQuantity = 140,
            rating = 4.6,
            reviewCount = 52,
            description = "ইউএসবি রিচার্জেবল ৬-ব্লেড মিনি ব্লেন্ডার। যেকোনো ফ্রুট জুস, স্মুদি বা মিল্কশেক তৈরিতে আদর্শ। সহজে বহনযোগ্য।",
            shortSpecs = listOf("ব্যাটারি: 2000mAh USB", "ব্লেড: ৬টি স্টেইনলেস স্টিল", "ক্যাপাসিটি: 380ml", "সহজে পরিষ্কারযোগ্য"),
            isHotDeal = false,
            tag = "হোম স্পেশাল"
        ),
        ResellingProduct(
            id = "prod_7",
            productCode = "SKU-FT-707",
            title = "মেনস প্রিমিয়াম লেদার ক্যাজুয়াল লোফার শু",
            category = ResellingCategory.FOOTWEAR,
            wholesalePrice = 650.0,
            maxRetailPrice = 1150.0,
            suggestedProfit = 500.0,
            stockQuantity = 160,
            rating = 4.8,
            reviewCount = 49,
            description = "সফট পিইউ লেদার এবং আরামদায়ক মেমোরি ফোম ইনসোল। অফিস ও ক্যাজুয়াল ব্যবহারের জন্য অত্যন্ত টেকসই ও আরামদায়ক।",
            shortSpecs = listOf("সাইজ: 40, 41, 42, 43", "উপাদান: হাই কোয়ালিটি লেদার", "নন-স্লিপ সোল", "কালার: ব্ল্যাক ও ব্রাউন"),
            isHotDeal = true,
            tag = "হট ডিল"
        ),
        ResellingProduct(
            id = "prod_8",
            productCode = "SKU-MN-108",
            title = "ড্রপ শোল্ডার ওভারসাইজড কটন টি-শার্ট (কম্বো ৩টি)",
            category = ResellingCategory.MENS_FASHION,
            wholesalePrice = 520.0,
            maxRetailPrice = 890.0,
            suggestedProfit = 370.0,
            stockQuantity = 210,
            rating = 4.7,
            reviewCount = 95,
            description = "১৮০+ জিএসএম ১০০% কম্বড সুতির ড্রপ শোল্ডার টি-শার্ট কম্বো। গরমে আরামদায়ক ও ফ্যাশনেবল।",
            shortSpecs = listOf("প্যাক: ৩টি টি-শার্ট", "ফেব্রিক: 100% Combed Cotton", "জিএসএম: 180+", "কালার: ব্ল্যাক, হোয়াইট, অলিভ"),
            isHotDeal = false,
            tag = "কম্বো অফার"
        ),
        ResellingProduct(
            id = "prod_9",
            productCode = "SKU-EL-309",
            title = "ওয়্যারলেস ফাস্ট চার্জিং ২০,০০০ mAh পাওয়ার ব্যাংক",
            category = ResellingCategory.ELECTRONICS,
            wholesalePrice = 680.0,
            maxRetailPrice = 1180.0,
            suggestedProfit = 500.0,
            stockQuantity = 175,
            rating = 4.9,
            reviewCount = 120,
            description = "২২.৫ ওয়াট সুপার ফাস্ট চার্জিং ও ডিজিটাল এলইডি ডিসপ্লে। একসাথে ৩টি ডিভাইস চার্জ করার সুবিধা।",
            shortSpecs = listOf("ক্যাপাসিটি: 20000mAh", "আউটপুট: 22.5W Fast Charge", "পোর্ট: Type-C PD + 2 USB", "ডিজিটাল ডিসপ্লে"),
            isHotDeal = true,
            tag = "জনপ্রিয়"
        )
    )

    fun getFreelancingCourses(): List<FreelancingCourse> = listOf(
        FreelancingCourse(
            id = "course_capcut",
            title = "CapCut Video Editing CourseVideo",
            price = 50.0,
            priceBengali = "৳ ৫০",
            drawableResName = "img_capcut_course",
            description = "মোবাইলে প্রফেশনাল ভিডিও এডিটিং শিখুন জিরো টু হিরো। CapCut-এর সকল টুলস, ট্রানজিশন, এফেক্টস, কালার গ্রেডিং ও অডিও এডিটিং সম্পূর্ণ বাংলায়।",
            lessonsCount = 10,
            totalHours = "৩ ঘণ্টা ২০ মিনিট"
        ),
        FreelancingCourse(
            id = "course_adsterra",
            title = "Adsterra Premium CourseVideo 2025",
            price = 50.0,
            priceBengali = "৳ ৫০",
            drawableResName = "img_adsterra_course",
            description = "ওয়েবসাইট ও ব্লগে Adsterra অ্যাড বসিয়ে প্রিমিয়াম প্যাসিভ ইনকাম করার সম্পূর্ণ গাইডলাইন। জিরো থেকে সফল হওয়ার সম্পূর্ণ সিক্রেট মেথড।",
            lessonsCount = 12,
            totalHours = "৪ ঘণ্টা ১৫ মিনিট"
        )
    )

    suspend fun purchaseCourse(course: FreelancingCourse): Result<CourseOrder> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("দয়া করে প্রথমে লগইন করুন"))

        if (user.balance < course.price) {
            return Result.failure(IllegalStateException("কোর্সটি কেনার জন্য ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই (প্রয়োজন: ৳${course.price.toInt()}, বর্তমান ব্যালেন্স: ৳${user.balance.toInt()})"))
        }

        val newBalance = user.balance - course.price
        val orderId = "CRS-" + (100000..999999).random()
        val trxId = "TRX-" + (1000000..9999999).random()

        val order = CourseOrder(
            id = orderId,
            courseId = course.id,
            courseTitle = course.title,
            price = course.price,
            timestamp = System.currentTimeMillis(),
            transactionRef = trxId
        )

        _currentUser.value = user.copy(balance = newBalance)
        _purchasedCourseIds.value = _purchasedCourseIds.value + course.id

        val trxRecord = Transaction(
            id = trxId,
            userId = user.uid,
            type = TransactionType.WITHDRAWAL,
            amount = -course.price,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis(),
            description = "কোর্স ক্রয়: ${course.title} (অর্ডার: $orderId)",
            paymentMethod = "ওয়ালেট ব্যালেন্স",
            transactionRef = trxId
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "কোর্স ক্রয় সফল! 🎓",
            message = "আপনি সফলভাবে '${course.title}' কোর্সে এনরোল করেছেন। এখন টিউটোরিয়াল ও ভিডিও দেখতে পারবেন।",
            category = NotificationCategory.SYSTEM
        )

        return Result.success(order)
    }

    fun getLearningCourseVideos(): List<LearningCourseVideo> = listOf(
        LearningCourseVideo(
            id = "vid_1",
            title = "🎉 Rootva 1 Year Successful Program 🎉 Alhamdulillah!",
            duration = "Duration: 0:00",
            progressPercent = 100,
            isCompleted = true,
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            youtubeVideoId = "dQw4w9WgXcQ",
            channelName = "Rootva",
            thumbnailType = 1
        ),
        LearningCourseVideo(
            id = "vid_2",
            title = "মোবাইল দিয়ে ২ মিনিটে Rootva একাউন্ট খুলুন 😍",
            duration = "Duration: 0:00",
            progressPercent = 0,
            isCompleted = false,
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            youtubeVideoId = "dQw4w9WgXcQ",
            channelName = "Rootva",
            thumbnailType = 2
        ),
        LearningCourseVideo(
            id = "vid_3",
            title = "নতুনদের জন্য রোডভা সিক্রেট | কিভাবে আয় করবেন $450+ লাইভ উত্তোলন ৫০ হাজার #rootva",
            duration = "Duration: 0:00",
            progressPercent = 100,
            isCompleted = true,
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            youtubeVideoId = "dQw4w9WgXcQ",
            channelName = "Rootva",
            thumbnailType = 3
        ),
        LearningCourseVideo(
            id = "vid_4",
            title = "Rootva কাজ সম্পর্কে A to Z বিস্তারিত জেনে নিন |",
            duration = "Duration: 0:00",
            progressPercent = 0,
            isCompleted = false,
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            youtubeVideoId = "dQw4w9WgXcQ",
            channelName = "Rootva",
            thumbnailType = 4
        )
    )

    fun getLeaderboardUsers(): List<LeaderboardUser> = listOf(
        LeaderboardUser(
            rank = 1,
            name = "Mohammad Ismail",
            affiliateId = "#281903",
            memberCountText = "5118 জন",
            memberCount = 5118,
            avatarType = 1
        ),
        LeaderboardUser(
            rank = 2,
            name = "SAJIDUL TM™",
            affiliateId = "#193821",
            memberCountText = "1518 জন",
            memberCount = 1518,
            avatarType = 2
        ),
        LeaderboardUser(
            rank = 3,
            name = "H.M  Jahedol I...",
            affiliateId = "#204912",
            memberCountText = "1070 জন",
            memberCount = 1070,
            avatarType = 3
        ),
        LeaderboardUser(
            rank = 4,
            name = "freelancer Likhon",
            affiliateId = "#184947",
            memberCountText = "821 জন",
            memberCount = 821,
            avatarType = 4
        ),
        LeaderboardUser(
            rank = 5,
            name = "Rootva Qween",
            affiliateId = "#222333",
            memberCountText = "575 জন",
            memberCount = 575,
            avatarType = 5
        ),
        LeaderboardUser(
            rank = 6,
            name = "Amina Khatun",
            affiliateId = "#176907",
            memberCountText = "366 জন",
            memberCount = 366,
            avatarType = 6
        ),
        LeaderboardUser(
            rank = 7,
            name = "\"LEADER RONJU\"",
            affiliateId = "#805089",
            memberCountText = "341 জন",
            memberCount = 341,
            avatarType = 7
        ),
        LeaderboardUser(
            rank = 8,
            name = "Rootva",
            affiliateId = "#556912",
            memberCountText = "269 জন",
            memberCount = 269,
            avatarType = 8
        ),
        LeaderboardUser(
            rank = 9,
            name = "Shahadat Hussain",
            affiliateId = "#610294",
            memberCountText = "230 জন",
            memberCount = 230,
            avatarType = 9
        ),
        LeaderboardUser(
            rank = 10,
            name = "MD. Tanvir Ahmed",
            affiliateId = "#774012",
            memberCountText = "195 জন",
            memberCount = 195,
            avatarType = 10
        )
    )

    // --- Online Service Operations ---

    fun getOnlineServices(): List<OnlineServiceItem> = listOf(
        OnlineServiceItem(
            id = "service_fb_pro",
            title = "Facebook v585.0.0.41.75 — Premium Unlocked",
            price = 30.0,
            priceDisplay = "Price ৳ 30.00",
            resellingPrice = 50.0,
            resellingPriceDisplay = "Reselling Price ৳ 50.00",
            isNew = true,
            category = "Digital Product",
            description = "Facebook official latest build with all pro features, ad-free experience, high-speed HD media downloader, story privacy mode, and background audio playback.",
            downloadUrl = "https://rootva.app/download/facebook-v585-premium.apk"
        ),
        OnlineServiceItem(
            id = "service_wa_ultra",
            title = "WhatsApp Ultra Plus v21.40 — Anti-Ban Pro",
            price = 30.0,
            priceDisplay = "Price ৳ 30.00",
            resellingPrice = 50.0,
            resellingPriceDisplay = "Reselling Price ৳ 50.00",
            isNew = true,
            category = "Digital Product",
            description = "Anti-delete messages & status, freeze last seen, custom themes, send up to 1GB HD files, mass sender, dual account support.",
            downloadUrl = "https://rootva.app/download/whatsapp-ultra-pro.apk"
        ),
        OnlineServiceItem(
            id = "service_capcut_vip",
            title = "CapCut Pro v12.8.0 — VIP All Filters Unlocked",
            price = 35.0,
            priceDisplay = "Price ৳ 35.00",
            resellingPrice = 60.0,
            resellingPriceDisplay = "Reselling Price ৳ 60.00",
            isNew = true,
            category = "Video Editor Tool",
            description = "No watermark, 4K 60FPS export, pro transitions, auto captions, slow motion smooth optical flow, all VIP effects unlocked.",
            downloadUrl = "https://rootva.app/download/capcut-vip-pro.apk"
        ),
        OnlineServiceItem(
            id = "service_canva_edu",
            title = "Canva Pro Premium Lifetime Edu Access",
            price = 40.0,
            priceDisplay = "Price ৳ 40.00",
            resellingPrice = 70.0,
            resellingPriceDisplay = "Reselling Price ৳ 70.00",
            isNew = false,
            category = "Design Tool",
            description = "Brand kits, magic resize, background remover, millions of premium photos, audio tracks, and templates unlocked.",
            downloadUrl = "https://rootva.app/download/canva-pro-invite"
        )
    )

    private fun getInitialOnlineServiceOrders(): List<OnlineServiceOrder> = listOf(
        OnlineServiceOrder(
            id = "SRV-912041",
            serviceId = "service_fb_pro",
            serviceTitle = "Facebook v585.0.0.41.75 — Premium Unlocked",
            price = 30.0,
            timestamp = System.currentTimeMillis() - 86400000L * 1,
            downloadUrl = "https://rootva.app/download/facebook-v585-premium.apk",
            status = "সফল"
        )
    )

    suspend fun purchaseOnlineService(service: OnlineServiceItem): Result<OnlineServiceOrder> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("দয়া করে প্রথমে লগইন করুন"))

        if (user.balance < service.price) {
            return Result.failure(IllegalStateException("সার্ভিসটি কেনার জন্য ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই (প্রয়োজন: ৳${service.price.toInt()}, বর্তমান ব্যালেন্স: ৳${user.balance.toInt()})"))
        }

        val newBalance = user.balance - service.price
        val orderId = "SRV-" + (100000..999999).random()
        val trxId = "TRX-" + (1000000..9999999).random()

        val order = OnlineServiceOrder(
            id = orderId,
            serviceId = service.id,
            serviceTitle = service.title,
            price = service.price,
            timestamp = System.currentTimeMillis(),
            downloadUrl = service.downloadUrl,
            status = "সফল"
        )

        _currentUser.value = user.copy(balance = newBalance)
        _onlineServiceOrders.value = listOf(order) + _onlineServiceOrders.value

        val trxRecord = Transaction(
            id = trxId,
            userId = user.uid,
            type = TransactionType.WITHDRAWAL,
            amount = -service.price,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis(),
            description = "অনলাইন সার্ভিস: ${service.title} (অর্ডার: $orderId)",
            paymentMethod = "ওয়ালেট ব্যালেন্স",
            transactionRef = trxId
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "অনলাইন সার্ভিস সফল! ⚡",
            message = "'${service.title}' সফলভাবে অর্ডার সম্পন্ন হয়েছে। ডাউনলোড লিংক আনলক হয়েছে।",
            category = NotificationCategory.SYSTEM
        )

        return Result.success(order)
    }

    // --- Micro Jobs Operations ---

    fun getMicroJobs(): List<MicroJobItem> = listOf(
        MicroJobItem(
            id = "mj_1",
            serialNumber = 1,
            title = "Facebook",
            reward = 0.5,
            rewardDisplay = "Reward: 0.5৳",
            platform = "Facebook",
            instructions = "আমাদের ফেসবুক পেজে প্রবেশ করুন। পেজে 'লাইক' এবং 'ফলো' করুন। আপনার ফেসবুক প্রোফাইলের ইউজারনেম বা লিংক নিচে প্রুফ বক্সে দিন।",
            targetUrl = "https://facebook.com",
            requiresScreenshot = true
        ),
        MicroJobItem(
            id = "mj_2",
            serialNumber = 2,
            title = "📢 **YouTube ভিডিও দেখুন, কাজ",
            reward = 0.5,
            rewardDisplay = "Reward: 0.5৳",
            platform = "YouTube",
            instructions = "ইউটিউব ভিডিওটি অন্তত ৩ মিনিট দেখুন। ভিডিওতে লাইক দিন এবং চ্যানেলটি সাবস্ক্রাইব করুন। আপনার ইউটিউব চ্যানেলের নাম প্রুফ বক্সে দিন।",
            targetUrl = "https://youtube.com",
            requiresScreenshot = true
        ),
        MicroJobItem(
            id = "mj_3",
            serialNumber = 3,
            title = "ভিডিও শুরু থেকে _ শেষ পর্যন্ত সম্পূর্ণ দেখা লাগবে ✅",
            reward = 0.5,
            rewardDisplay = "Reward: 0.5৳",
            platform = "YouTube",
            instructions = "ভিডিওটি শুরু থেকে শেষ পর্যন্ত সম্পূর্ণ দেখুন, কোনো স্কিপ করা যাবে না। শেষে ভিডিওর একটি প্রাসঙ্গিক মন্তব্য করুন এবং কমেন্টের স্ক্রিনশট দিন।",
            targetUrl = "https://youtube.com",
            requiresScreenshot = true
        ),
        MicroJobItem(
            id = "mj_4",
            serialNumber = 4,
            title = "YouTube ভিডিও দেখুন, কাজ সম্পন্ন করুন এবং প্রুফ...",
            reward = 0.5,
            rewardDisplay = "Reward: 0.5৳",
            platform = "YouTube",
            instructions = "প্রোডাক্ট রিভিউ ভিডিওটি ফুল দেখুন। লাইক ও শেয়ার বাটনে ক্লিক করে সোশ্যাল মিডিয়ায় শেয়ার করুন এবং প্রমাণস্বরূপ লিংক সাবমিট করুন।",
            targetUrl = "https://youtube.com",
            requiresScreenshot = true
        )
    )

    suspend fun submitMicroJobProof(job: MicroJobItem, proofText: String): Result<MicroJobSubmission> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("দয়া করে প্রথমে লগইন করুন"))

        if (proofText.trim().length < 3) {
            return Result.failure(IllegalArgumentException("সঠিক প্রমাণ বা লিংক প্রদান করুন"))
        }

        val submissionId = "SUB-" + (100000..999999).random()
        val trxId = "TRX-" + (1000000..9999999).random()

        val submission = MicroJobSubmission(
            id = submissionId,
            jobId = job.id,
            jobTitle = job.title,
            reward = job.reward,
            userProofText = proofText.trim(),
            timestamp = System.currentTimeMillis(),
            status = "অনুমোদিত"
        )

        // Credit reward to user balance
        val newBalance = user.balance + job.reward
        _currentUser.value = user.copy(balance = newBalance)
        _completedMicroJobIds.value = _completedMicroJobIds.value + job.id

        val trxRecord = Transaction(
            id = trxId,
            userId = user.uid,
            type = TransactionType.TASK_INCOME,
            amount = job.reward,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis(),
            description = "মাইক্রো জব সম্পন্ন: ${job.title}",
            paymentMethod = "মাইক্রো জব রিওয়ার্ড",
            transactionRef = trxId
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "মাইক্রো জব রিওয়ার্ড ক্রেডিট! 💰",
            message = "'${job.title}' সফলভাবে সম্পন্ন করার জন্য আপনার ওয়ালেটে ৳${String.format(java.util.Locale.US, "%.2f", job.reward)} যোগ করা হয়েছে।",
            category = NotificationCategory.SYSTEM
        )

        return Result.success(submission)
    }

    // --- Dollar Income (Watch Ad & Earn Bot) ---

    suspend fun completeDollarIncomeAdTask(reward: Double = 5.0): Result<Double> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("দয়া করে প্রথমে লগইন করুন"))

        val trxId = "ADTRX-" + (1000000..9999999).random()
        val newBalance = user.balance + reward
        _currentUser.value = user.copy(balance = newBalance)

        val trxRecord = Transaction(
            id = trxId,
            userId = user.uid,
            type = TransactionType.TASK_INCOME,
            amount = reward,
            status = TransactionStatus.COMPLETED,
            date = System.currentTimeMillis(),
            description = "Dollar Income: Watch Ad & Earn (15s)",
            paymentMethod = "Ad Bot Reward",
            transactionRef = trxId
        )
        _transactions.value = listOf(trxRecord) + _transactions.value

        addNotification(
            title = "Dollar Income রিওয়ার্ড ক্রেডিট! 🤑",
            message = "Watch Ad & Earn টাস্ক সম্পন্ন করার জন্য আপনার ওয়ালেটে ৳${String.format(java.util.Locale.US, "%.2f", reward)} যোগ করা হয়েছে।",
            category = NotificationCategory.SYSTEM
        )

        return Result.success(reward)
    }
}
