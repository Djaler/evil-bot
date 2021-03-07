package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.User
import com.github.djaler.evilbot.entity.UserChatStatistic
import com.github.djaler.evilbot.enums.UserGender
import com.github.djaler.evilbot.repository.UserRepository
import com.github.djaler.evilbot.repository.UserStatisticRepository
import com.github.djaler.evilbot.utils.usernameOrName
import dev.inmo.tgbotapi.types.ChatId
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.time.Month

class UserServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerTest

    val userRepository = mockk<UserRepository>()
    val userStatisticRepository = mockk<UserStatisticRepository>()
    val timeService = mockk<TimeService>()
    val userService = UserService(userRepository, userStatisticRepository, timeService)

    Given("getOrCreateUserFrom method") {
        val userId = 1
        val username = "username"

        val telegramUser = mockk<dev.inmo.tgbotapi.types.User> {
            every { id } returns ChatId(userId.toLong())
            every { usernameOrName } returns username
        }

        And("user already exists") {
            val user = mockk<User>()
            every { userRepository.findByTelegramId(userId) } returns user

            When("called") {
                val (resultUser, created) = userService.getOrCreateUserFrom(telegramUser)

                Then("should return the user") {
                    resultUser shouldBe user
                }

                Then("should mark result as not created") {
                    created shouldBe false
                }
            }
        }

        And("user not exists") {
            every { userRepository.findByTelegramId(any()) } returns null
            every { userRepository.save(any()) } returnsArgument 0

            When("called") {
                val (resultUser, created) = userService.getOrCreateUserFrom(telegramUser)

                Then("should return new user") {
                    resultUser shouldBe User(username, userId, UserGender.IT)
                }

                Then("should save new user") {
                    verify { userRepository.save(User(username, userId, UserGender.IT)) }
                }

                Then("should mark result as not created") {
                    created shouldBe true
                }
            }
        }
    }

    Given("registerMessageInStatistic method") {
        val userId = 1
        val chatId = 1.toShort()
        val currentTime = LocalDateTime.of(2021, Month.MARCH, 15, 21, 20)
        val user = mockk<User> {
            every { id } returns userId
        }
        val chat = mockk<Chat> {
            every { id } returns chatId
        }
        every { userStatisticRepository.save(any()) } returns mockk()
        every { timeService.getServerTime() } returns currentTime

        And("statistic already exists") {
            val statistic = UserChatStatistic(
                chatId,
                mockk(),
                10,
                LocalDateTime.now(),
                1
            )
            every { userStatisticRepository.findByChatAndUser(chatId, userId) } returns statistic

            When("called") {
                userService.registerMessageInStatistic(user, chat)

                Then("should increment statistic messages count") {
                    verify {
                        userStatisticRepository.save(withArg {
                            it.messagesCount shouldBe 11
                        })
                    }
                }

                Then("should update last activity to current time") {
                    verify {
                        userStatisticRepository.save(withArg {
                            it.lastActivity shouldBe currentTime
                        })
                    }
                }

                Then("should not update id, chat id and user") {
                    verify {
                        userStatisticRepository.save(withArg {
                            it.shouldBeEqualToIgnoringFields(
                                statistic,
                                UserChatStatistic::messagesCount,
                                UserChatStatistic::lastActivity,
                            )
                        })
                    }
                }
            }
        }

        And("statistic not already exists") {
            every { userStatisticRepository.findByChatAndUser(chatId, userId) } returns null

            When("called") {
                userService.registerMessageInStatistic(user, chat)

                Then("should save new statistic") {
                    val expectedStatistic = UserChatStatistic(chat.id, user, 1, currentTime)
                    verify { userStatisticRepository.save(expectedStatistic) }
                }
            }
        }
    }
})
