/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.developer.lsp.debug.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.java.agent.host.MethodContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

/**
 * Session maintained for the debug.
 * Contains the information about the breakpoints.
 */
public class DebugSession {

    private static final Log log = LogFactory.getLog(DebugSession.class);

    private Session session;
    private String tenantDomain;
    private Map<String, BreakpointInfo> breakpointInfoMap = new HashMap<>();
    private MethodContext currentMethodContext;
    private List<Object> calledObjectStack = new ArrayList<>();
    private final Object waitObject = new Object();
    private static final long MAX_THREAD_SUSPEND_TIME_MILLIS = 5000;

    /**
     * This is the getter which gets the  Websocket session.
     *
     * @return The websocket session initiated.
     */
    public Session getSession() {

        return session;
    }

    /**
     * This is the setter which sets the  Websocket session.
     *
     * @param session The websocket session initiated.
     */
    public void setSession(Session session) {

        this.session = session;
    }

    /**
     * This is the setter which adds breakpointInfo.
     *
     * @param resource    The name of the resource.
     * @param breakpoints The information about the breakpoints.
     */
    public void setBreakpoints(String resource, int[] breakpoints) {

        BreakpointInfo breakpointInfo = breakpointInfoMap.get(resource);
        if (breakpointInfo == null) {
            breakpointInfo = new BreakpointInfo();
            breakpointInfoMap.put(resource, breakpointInfo);
        }

        breakpointInfo.setBreakpoints(breakpoints);
    }

    /**
     * Gets Breakpoint Info.
     *
     * @param source The name of the source.
     * @return The information about the breakpoints.
     */
    public BreakpointInfo getBreakpointInfo(String source) {

        BreakpointInfo result = breakpointInfoMap.get(source);
        return result;
    }

    /**
     * This is temporary method, needs to be removed once created correct breakpoint identification logic.
     *
     * @return The BreakpointInfo from the breakpointInfoMap.
     */
    @Deprecated
    public BreakpointInfo[] getBreakpointInfos() {

        BreakpointInfo[] result = new BreakpointInfo[breakpointInfoMap.size()];
        int i = 0;
        for (BreakpointInfo breakpointInfo : breakpointInfoMap.values()) {
            result[i] = breakpointInfo;
            i++;
        }
        return result;
    }

    /**
     * Gets the currentMethodContext.
     *
     * @return The currently instrumented method context.
     */
    public MethodContext getCurrentMethodContext() {

        return currentMethodContext;
    }

    /**
     * Sets the currentMethodContext.
     *
     * @param currentMethodContext The currently instrumented method context.
     */
    public void setCurrentMethodContext(MethodContext currentMethodContext) {

        this.currentMethodContext = currentMethodContext;
    }

    /**
     * @param methodContext Instrumented method details.
     * @return The DebugProcessingResult if breakpointInfo else null .
     */
    public DebugProcessingResult processMethodEntry(MethodContext methodContext) {

        BreakpointInfo breakpointInfo = findAnyStoppableBreakpoint(methodContext);
        if (breakpointInfo != null) {
            if (breakpointInfo.getBreakpointLocations() != null && breakpointInfo.getBreakpointLocations().length > 0) {
                DebugProcessingResult result = new DebugProcessingResult(DebugProcessingResult.InstructionType.STOP);
                result.setBreakpointInfo(breakpointInfo);
                return result;
            }
        }
        return null;
    }

    /**
     * This method is to get the breakpointInfoMap.
     *
     * @param methodContext The instrumented method details.
     * @return The information about the break point.
     */
    private BreakpointInfo findAnyStoppableBreakpoint(MethodContext methodContext) {

        if (breakpointInfoMap == null || breakpointInfoMap.isEmpty()) {
            return null;
        }
        return breakpointInfoMap.values().stream().findFirst().get();
    }

    /**
     * This method is to suspend the current Thread.
     */
    public void suspendCurrentThread() {

        try {
            synchronized (this.waitObject) {
                this.waitObject.wait(MAX_THREAD_SUSPEND_TIME_MILLIS);
            }
        } catch (InterruptedException e) {
            log.warn("Thread was resumed, which was suspended due to breakpoint. " +
                    "There was no instruction received from remote debug client for " + MAX_THREAD_SUSPEND_TIME_MILLIS +
                    " milliseconds. Remote client was: " + getSessionInfo());
        }
    }

    /**
     * This method is to get the  Websocket session ID.
     *
     * @return The id of the session.
     */
    private String getSessionInfo() {

        return session.getId();
    }

    /**
     * This method is to resume the current Thread.
     */
    public void resumeSuspendedThread() {

        synchronized (this.waitObject) {
            this.waitObject.notify();
        }
    }

    /**
     * get current Tenant Domain.
     *
     * @return current Tenant Domain.
     */
    public String getTenantDomain() {

        return tenantDomain;
    }

    /**
     * set current Tenant Domain.
     *
     * @param tenantDomain current Tenant Domain.
     */
    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }
}
