package com.turnin.domain.announcement.di

import com.turnin.domain.announcement.application.usecase.AnnouncementAdminUseCases
import com.turnin.domain.announcement.application.usecase.AnnouncementUseCases
import com.turnin.domain.announcement.application.usecase.CreateAnnouncementUseCase
import com.turnin.domain.announcement.application.usecase.DeleteAnnouncementUseCase
import com.turnin.domain.announcement.application.usecase.GetAnnouncementsUseCase
import com.turnin.domain.announcement.application.usecase.MarkAnnouncementAsReadUseCase
import com.turnin.domain.announcement.application.usecase.UpdateAnnouncementStatusUseCase
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import com.turnin.domain.announcement.infrastructure.repository.AnnouncementRepositoryImpl
import org.koin.dsl.module

val announcementModule = module {
    // Repository
    single<AnnouncementRepository> { AnnouncementRepositoryImpl() }

    // Usecase
    single { CreateAnnouncementUseCase(get()) }
    single { DeleteAnnouncementUseCase(get()) }
    single { MarkAnnouncementAsReadUseCase(get()) }
    single { GetAnnouncementsUseCase(get()) }
    single { UpdateAnnouncementStatusUseCase(get()) }
    single {
        AnnouncementUseCases(get(), get())
    }
    single {
        AnnouncementAdminUseCases(get(), get(), get())
    }
}
