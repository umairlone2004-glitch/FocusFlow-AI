package com.focusflow.ai

import com.focusflow.ai.domain.model.UserProfile
import com.focusflow.ai.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory profile store for ViewModel tests. */
class FakeProfileRepository : ProfileRepository {

    private val state = MutableStateFlow<UserProfile?>(null)

    override fun observe(): Flow<UserProfile?> = state

    override suspend fun get(): UserProfile? = state.value

    override suspend fun save(profile: UserProfile) {
        state.value = profile
    }
}
