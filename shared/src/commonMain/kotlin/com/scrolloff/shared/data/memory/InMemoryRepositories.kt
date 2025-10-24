package com.scrolloff.shared.data.memory

import com.scrolloff.shared.domain.model.ContextRule
import com.scrolloff.shared.domain.model.CreditLedger
import com.scrolloff.shared.domain.model.FocusSession
import com.scrolloff.shared.domain.model.InterceptEvent
import com.scrolloff.shared.domain.model.InterceptSensitivity
import com.scrolloff.shared.domain.model.WatchedApp
import com.scrolloff.shared.domain.repository.ContextRuleRepository
import com.scrolloff.shared.domain.repository.CreditRepository
import com.scrolloff.shared.domain.repository.FocusSessionRepository
import com.scrolloff.shared.domain.repository.InsightRepository
import com.scrolloff.shared.domain.repository.InterceptRepository
import com.scrolloff.shared.domain.repository.WatchedAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryWatchedAppRepository : WatchedAppRepository {
    private val apps = MutableStateFlow<List<WatchedApp>>(emptyList())

    override fun observeWatchedApps(): Flow<List<WatchedApp>> = apps

    override suspend fun addApp(app: WatchedApp) {
        apps.value = (apps.value + app).distinctBy { it.packageName }
    }

    override suspend fun removeApp(packageName: String) {
        apps.value = apps.value.filterNot { it.packageName == packageName }
    }

    override suspend fun updateNotificationSafe(packageName: String, isSafe: Boolean) {
        apps.value = apps.value.map {
            if (it.packageName == packageName) it.copy(isNotificationSafe = isSafe) else it
        }
    }

    override suspend fun updateSensitivity(packageName: String, sensitivity: InterceptSensitivity) {
        apps.value = apps.value.map {
            if (it.packageName == packageName) it.copy(sensitivity = sensitivity) else it
        }
    }
}

class InMemoryCreditRepository : CreditRepository {
    private val ledger = MutableStateFlow(CreditLedger.EMPTY)

    override fun observeLedger(): Flow<CreditLedger> = ledger

    override suspend fun setLedger(ledger: CreditLedger) {
        this.ledger.value = ledger
    }
}

class InMemoryInterceptRepository : InterceptRepository {
    private val events = MutableStateFlow<List<InterceptEvent>>(emptyList())

    override fun observeRecent(limit: Int): Flow<List<InterceptEvent>> = events.map { it.takeLast(limit) }

    override suspend fun record(event: InterceptEvent) {
        events.value = (events.value + event).takeLast(MAX_EVENTS)
    }

    override suspend fun clear() {
        events.value = emptyList()
    }

    private companion object {
        private const val MAX_EVENTS = 50
    }
}

class InMemoryFocusSessionRepository : FocusSessionRepository {
    private val sessions = MutableStateFlow<List<FocusSession>>(emptyList())

    override fun observeSessions(): Flow<List<FocusSession>> = sessions

    override suspend fun upsert(session: FocusSession) {
        sessions.value = sessions.value.filterNot { it.id == session.id } + session
    }

    override suspend fun delete(id: String) {
        sessions.value = sessions.value.filterNot { it.id == id }
    }
}

class InMemoryContextRuleRepository : ContextRuleRepository {
    private val rules = MutableStateFlow<List<ContextRule>>(emptyList())

    override fun observeRules(): Flow<List<ContextRule>> = rules

    override suspend fun upsert(rule: ContextRule) {
        rules.value = rules.value.filterNot { it.id == rule.id } + rule
    }

    override suspend fun delete(id: String) {
        rules.value = rules.value.filterNot { it.id == id }
    }
}

class InMemoryInsightRepository : InsightRepository {
    private val insights = MutableStateFlow<List<com.scrolloff.shared.domain.model.PatternInsight>>(emptyList())

    override fun observeInsights(): Flow<List<com.scrolloff.shared.domain.model.PatternInsight>> = insights

    override suspend fun addInsight(insight: com.scrolloff.shared.domain.model.PatternInsight) {
        insights.value = (insights.value + insight).distinctBy { it.id }
    }

    override suspend fun clear() {
        insights.value = emptyList()
    }
}
