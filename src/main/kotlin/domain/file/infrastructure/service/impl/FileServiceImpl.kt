package com.peekr.domain.file.infrastructure.service.impl

import com.peekr.domain.file.domain.service.FileService

class FileServiceImpl : FileService {
    private val r2Service = CloudflareR2Service()

    override fun createPresignedUrl(fileName: String): String {
        val presignedRequest = r2Service.createPresignedRequest(fileName)
        return presignedRequest.url().toString()
    }
}
