package com.skipper.testsensor

import fi.iki.elonen.NanoHTTPD
import java.net.URI

class HookHttpServer(port: Int, private val handlerHttpResponse: (path: String, session: IHTTPSession) -> Response) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession?): Response {
        return session?.let {
            val uri = URI(session.uri)
            handlerHttpResponse(uri.path, session)
        }?: newFixedLengthResponse(Response.Status.BAD_REQUEST,"application/json","{}")
    }
}