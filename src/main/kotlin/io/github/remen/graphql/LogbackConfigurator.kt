package io.github.remen.graphql

import ch.qos.logback.classic.BasicConfigurator
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.layout.TTLLLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder

class LogbackConfigurator : BasicConfigurator() {
    override fun configure(lc: LoggerContext) {
        addInfo("Setting up default configuration.")

        lc.getLogger(Logger.ROOT_LOGGER_NAME).apply {
            level = Level.INFO

            addAppender(ConsoleAppender<ILoggingEvent>().apply {
                context = lc
                name = "console"

                encoder = LayoutWrappingEncoder<ILoggingEvent>().apply {
                    context = lc
                    layout = TTLLLayout().apply {
                        context = lc
                        start()
                    }
                }

                start()
            })
        }
    }
}
