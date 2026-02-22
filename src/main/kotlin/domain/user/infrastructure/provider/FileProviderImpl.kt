package com.peekr.domain.user.infrastructure.provider

import com.peekr.domain.file.application.provider.FileProviderApi
import com.peekr.domain.user.domain.provider.FileProvider

class FileProviderImpl(private val fileProviderApi: FileProviderApi) : FileProvider {
    override fun deleteFile(fileName: String) =
        fileProviderApi.deleteFile(fileName)
}
