/*
 Copyright 2013 Microsoft Open Technologies, Inc.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

namespace MicrosoftOpenTechnologies.Tools.ARRAgent
{
    using System.Threading;

    /// <summary>
    /// Used to synchronize starting of the agent and waiting for IIS configuration to be updated.
    /// </summary>
    internal static class SynchManager
    {
        private const string SuccessEventName = "ArrConfigurationAgent-SucessEvent-B66449BA-E1FC-45FE-9F55-DBC00E0121C6";
        private const string FailureEventName = "ArrConfigurationAgent-FailureEvent-EE9FC714-B2DA-471C-B20F-66BE0DAE0289";

        /// <summary>
        /// Signals the success synch event.
        /// </summary>
        internal static void SignalSuccess()
        {
            GetSuccessWaitHandle().Set();
        }

        /// <summary>
        /// Signals the failure synch event.
        /// </summary>
        internal static void SignalFailure()
        {
            GetFailureWaitHandle().Set();
        }

        /// <summary>
        /// Waits for success or failure events.
        /// </summary>
        internal static bool WaitEvents()
        {
            int index = EventWaitHandle.WaitAny(new[] { GetSuccessWaitHandle(), GetFailureWaitHandle() });
            return index == 0;
        }

        /// <summary>
        /// Gets the success event handle object.
        /// </summary>
        private static EventWaitHandle GetSuccessWaitHandle()
        {
            return new EventWaitHandle(false, EventResetMode.ManualReset, SuccessEventName);
        }

        /// <summary>
        /// Gets the failure event handle object.
        /// </summary>
        private static EventWaitHandle GetFailureWaitHandle()
        {
            return new EventWaitHandle(false, EventResetMode.ManualReset, FailureEventName);
        }
    }
}
