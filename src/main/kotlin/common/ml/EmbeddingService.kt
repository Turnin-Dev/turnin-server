package com.peekr.common.ml

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.peekr.common.util.AppLoggerFactory
import java.io.File
import java.nio.LongBuffer
import java.nio.file.Paths
import kotlin.math.sqrt

/**
 * 임베딩 서비스
 *
 * @property modelPath ONNX 모델 파일 경로
 * @property tokenizerPath 토큰나이저 파일 경로
 */
class EmbeddingService(
    private val modelPath: String,
    private val tokenizerPath: String,
) {
    private lateinit var env: OrtEnvironment
    private lateinit var session: OrtSession
    private lateinit var tokenizer: HuggingFaceTokenizer

    @Volatile
    private var initialized = false

    /**
     * ONNX, Tokenizer 초기화
     */
    @Synchronized
    fun init() {
        if (initialized) return

        try {
            env = OrtEnvironment.getEnvironment()
            initOnnx(modelPath)
            initTokenizer(tokenizerPath)
            initialized = true
        } catch (e: Exception) {
            LOGGER.error("embedding service initialization failed: ${e.message}")
            if (::session.isInitialized) {
                session.close()
            }
            throw EmbeddingServiceException.InitializationFailed()
        }
    }

    private fun initOnnx(modelPath: String) {
        val modelFile = File(modelPath)
        if (!modelFile.exists()) error("ONNX model file not found at ${modelFile.absolutePath}")
        session = env.createSession(
            modelFile.absolutePath,
            OrtSession.SessionOptions().apply {
                setInterOpNumThreads(1)
                setIntraOpNumThreads(1)
            },
        )
    }

    private fun initTokenizer(tokenizerPath: String) {
        val modelFile = File(tokenizerPath)
        if (!modelFile.exists()) error("Tokenizer file not found at ${modelFile.absolutePath}")
        tokenizer = HuggingFaceTokenizer.newInstance(Paths.get(modelFile.absolutePath))
    }

    /**
     * 입력받은 텍스트를 ONNX 모델을 통해 추론하여 임베딩 벡터를 추출한다.
     *
     * **[kotlinx.coroutines.Dispatchers.Default]에서 실행하는 것을 권장한다.**
     *
     * @param text 벡터를 생성할 텍스트
     *
     * @throws EmbeddingServiceException.TokenizationFailed 토큰화 실패시 예외가 발생한다.
     * @throws EmbeddingServiceException.InferenceException 임베딩 과정에서 실패시 예외가 발생한다.
     */
    fun embed(text: String): String {
        check(initialized) { "EmbeddingService is not initialized." }

        val encoding = try {
            if (text.isEmpty() || text.isBlank()) throw EmbeddingServiceException.TokenizationFailed()
            tokenizer.encode(text)
        } catch (e: Exception) {
            LOGGER.error("text tokenization failed: $text, cause: ${e.message}")
            throw EmbeddingServiceException.TokenizationFailed()
        }

        val inputIds = encoding.ids
        val attentionMask = encoding.attentionMask
        val tokenTypeIds = encoding.typeIds

        val closeables = mutableListOf<OnnxTensor>()

        return try {
            val inputIdsTensor = OnnxTensor
                .createTensor(
                    env,
                    LongBuffer.wrap(inputIds),
                    longArrayOf(1, inputIds.size.toLong()),
                ).also { closeables.add(it) }

            val maskTensor = OnnxTensor
                .createTensor(
                    env,
                    LongBuffer.wrap(attentionMask),
                    longArrayOf(1, attentionMask.size.toLong()),
                ).also { closeables.add(it) }

            val typeIdsTensor = OnnxTensor
                .createTensor(
                    env,
                    LongBuffer.wrap(tokenTypeIds),
                    longArrayOf(1, tokenTypeIds.size.toLong()),
                ).also { closeables.add(it) }

            val inputs = mapOf(
                "input_ids" to inputIdsTensor,
                "attention_mask" to maskTensor,
                "token_type_ids" to typeIdsTensor,
            )

            session.run(inputs).use { results ->
                // results[0]은 보통 last_hidden_state (Batch, Seq_Len, Hidden_Dim)
                @Suppress("UNCHECKED_CAST")
                val outputs = results[0].value as Array<Array<FloatArray>>

                // 1) 평균 내기
                val sentenceEmbedding = meanPooling(outputs[0], attentionMask)

                // 2) 길이 1로 맞추기 (정규화)
                val normalizedSentenceEmbedding = normalize(sentenceEmbedding)

                // 3) 문자열 변환
                normalizedSentenceEmbedding.joinToString(prefix = "[", postfix = "]", separator = ",")
            }
        } catch (e: Exception) {
            LOGGER.error("embedding inference failed: $text, cause: ${e.message}")
            throw EmbeddingServiceException.InferenceException()
        } finally {
            closeables.forEach { it.close() }
        }
    }

    /**
     * 토큰별 벡터들을 평균내어 문장 전체의 의미를 대표하는 하나의 벡터를 만든다.
     * (Batch Size가 1인 경우에 최적화됨)
     */
    private fun meanPooling(
        outputs: Array<FloatArray>,
        attentionMask: LongArray,
    ): FloatArray {
        val dimension = outputs[0].size
        val result = FloatArray(dimension)
        var activeTokenCount = 0

        // 실제 단어 토큰이 존재하는 위치(Attention Mask가 1인 곳)만 합산
        for (i in outputs.indices) {
            if (i < attentionMask.size && attentionMask[i] == 1L) {
                for (d in 0 until dimension) {
                    result[d] += outputs[i][d]
                }
                activeTokenCount++
            }
        }

        // 0으로 나누기 방지
        val divisor = if (activeTokenCount > 0) activeTokenCount.toFloat() else 1.0f
        for (d in 0 until dimension) {
            result[d] /= divisor
        }

        return result
    }

    private fun normalize(vector: FloatArray): FloatArray {
        var sum = 0.0f
        for (v in vector) sum += v * v
        val length = sqrt(sum.toDouble()).toFloat()

        val divisor = if (length > 1e-6f) length else 1.0f
        for (i in vector.indices) {
            vector[i] /= divisor
        }
        return vector
    }

    @Synchronized
    fun close() {
        if (!initialized) return

        try {
            if (::session.isInitialized) session.close()
            if (::tokenizer.isInitialized) tokenizer.close()
            env.close()
        } finally {
            initialized = false
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger<EmbeddingService>()
