package org.spring.application

import org.spring.core.applicationRun
import org.spring.web.WebServer

fun main() = applicationRun {
    WebServer.initServer(true)
}