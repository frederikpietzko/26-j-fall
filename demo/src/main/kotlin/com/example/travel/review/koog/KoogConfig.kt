package com.example.travel.review.koog

import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLMProvider
import ai.koog.spring.ai.chat.SpringAiLLMClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Koog's PromptExecutor backed by Spring AI's ChatModel (koog-spring-ai).
 *
 * The starter would auto-configure this, but it orders itself after the classic
 * OpenAiChatAutoConfiguration only - Spring AI 2.1's Responses model comes from
 * OpenAiResponsesChatAutoConfiguration, so we wire the bridge explicitly.
 */
@Configuration
class KoogConfig {

    @Bean
    fun koogPromptExecutor(chatModel: ChatModel): PromptExecutor = MultiLLMPromptExecutor(
        SpringAiLLMClient.builder()
            .chatModel(chatModel)
            .provider(LLMProvider.OpenAI)
            .build()
    )
}
