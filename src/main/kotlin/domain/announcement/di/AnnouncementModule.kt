package com.turnin.domain.announcement.di

import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import com.turnin.domain.announcement.infrastructure.repository.AnnouncementRepositoryImpl
import org.koin.dsl.module

val announcementModule = module {
    // Repository
    single<AnnouncementRepository> { AnnouncementRepositoryImpl() }
}
