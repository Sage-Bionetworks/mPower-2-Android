package org.sagebionetworks.research.mpower.signup

import org.sagebionetworks.research.mpower.signup.ResponseStatus.*

//
//  Response.kt
//  mPower2
//
//  Copyright © 2018 Sage Bionetworks. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// 1.  Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// 2.  Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// 3.  Neither the name of the copyright holder(s) nor the names of any contributors
// may be used to endorse or promote products derived from this software without
// specific prior written permission. No license is granted to the trademarks of
// the copyright holders even if such marks are included in this software.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/**
 * The Response object will contain a response from the BridgeDataProvider.
 * It will always contain a status .  It may sometimes contain
 * relevant information, either requested or about the action that has a response.
 * or a detailed error describing what went wrong, with what to message the user.
 *
 * @param T the type of a member in this Response.
 * @property status the status of getting the information for the response.
 * @property data information being requested or data about the action that has a response.
 * @property error information about what went wrong with the response, and what to message the user.
 * @constructor Creates a Response.
 */
open class Response<T> (@ResponseStatus status: String, data: T? = null, error: ResponseError? = null) {

    companion object Factory {
        fun <T> success(data: T): Response<T> {
            return Response<T>(SUCCESS, data, null)
        }

        fun <T> error(@ResponseErrorCode code: Int, message: String): Response<T> {
            return Response<T>(ERROR, null, ResponseError(code, message))
        }

        fun <T> unknownError(message: String): Response<T> {
            return Response<T>(ERROR, null, ResponseError(ResponseErrorCode.UNKNOWN, message))
        }

        fun <T> loading(): Response<T> {
            return Response<T>(LOADING, null, null)
        }
    }

    val status: String = status
    val data: T? = data
    val error: ResponseError? = error
}