package org.sagebionetworks.research.mpower.signup;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//
//  ResponseErrorCode.java
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
 * This interface defines the various types of actions that are possible to have.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({ResponseErrorCode.UNKNOWN, ResponseErrorCode.NOT_SET_UP_ERROR,
        ResponseErrorCode.INTERNET_NOT_CONNECTED, ResponseErrorCode.SERVER_NOT_REACHABLE,
        ResponseErrorCode.SERVER_UNDER_MAINTENAINCE, ResponseErrorCode.SERVER_NOT_AUTHENTICATED,
        ResponseErrorCode.PRECONDITION_NOT_MET, ResponseErrorCode.NO_CREDENTIALS_AVAILABLE,
        ResponseErrorCode.UNSUPPORTED_APP_VERSION, ResponseErrorCode.SERVER_NOT_AUTHORIZED,
        ResponseErrorCode.SERVER_ACCOUNT_DISABLED, ResponseErrorCode.S3_UPLOAD_ERROR_RESPONSE,
        ResponseErrorCode.NOT_A_FILE_URL, ResponseErrorCode.NOT_EXPECTED_CLASS,
        ResponseErrorCode.TEMP_FILE_ERROR, ResponseErrorCode.FILE_READ_ERROR,
        ResponseErrorCode.OBJECT_NOT_FOUND, ResponseErrorCode.NOT_A_VALID_SURVEY_REF,
        ResponseErrorCode.NOT_A_VALID_JSON_OBJECT, ResponseErrorCode.NOT_REGISTERED_FOR_PUSH_NOTIFICATIONS})
public @interface ResponseErrorCode {
    int UNKNOWN = -1;
    int NOT_SET_UP_ERROR = -2;
    int INTERNET_NOT_CONNECTED = -1000;
    int SERVER_NOT_REACHABLE = -1001;
    int SERVER_UNDER_MAINTENAINCE = -1002;
    int SERVER_NOT_AUTHENTICATED = -1003;
    int PRECONDITION_NOT_MET = -1004;
    int NO_CREDENTIALS_AVAILABLE = -1005;
    int UNSUPPORTED_APP_VERSION = -1006;
    int SERVER_NOT_AUTHORIZED = -1007;
    int SERVER_ACCOUNT_DISABLED = -1008;

    int S3_UPLOAD_ERROR_RESPONSE = -1020;

    int NOT_A_FILE_URL = -1100;
    int NOT_EXPECTED_CLASS = -1101;

    // TODO: mdephillips 8/6/18 Should we make these Room specific database Errors?
    int TEMP_FILE_ERROR = -1102;
    int FILE_READ_ERROR = -1103;
    int OBJECT_NOT_FOUND = -1104;

    int NOT_A_VALID_SURVEY_REF = -1200;
    int NOT_A_VALID_JSON_OBJECT = -1201;
    int NOT_REGISTERED_FOR_PUSH_NOTIFICATIONS = -1202;
}

