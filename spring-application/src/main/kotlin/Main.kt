package org.spring.application

import org.spring.core.applicationRun
import org.spring.web.WebServer

@Suppress("unused")
class Main

fun main() = applicationRun {
    WebServer.initServer(false)
}