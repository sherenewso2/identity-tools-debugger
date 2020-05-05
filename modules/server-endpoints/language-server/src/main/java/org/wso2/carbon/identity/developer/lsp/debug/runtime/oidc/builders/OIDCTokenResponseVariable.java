/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.developer.lsp.debug.runtime.oidc.builders;

import org.wso2.carbon.identity.developer.lsp.debug.DAPConstants;

import java.util.HashMap;

/**
 * Variables Holds the necessary Variables for OIDC Token Response.
 */
public class OIDCTokenResponseVariable implements OIDCTokenResponseVariablePlan {
     
    private HashMap<String, Object> oidcTokenResponseVariableHolder = new HashMap<>();

    /**
     * This method is to get the variables after adding the objects.
     *
     * @return The list of variables corresponding to Token Response.
     */
    public HashMap<String, Object> getOidcTokenResponseVariableHolder() {

        return oidcTokenResponseVariableHolder;
    }

    @Override
    public void setTokenResponse(Object tokenResponse) {

        this.oidcTokenResponseVariableHolder.put(DAPConstants.OIDC_TOKEN_RESPONSE, tokenResponse);
    }
}
