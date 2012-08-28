/**
 * Copyright 2011 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.persistent.uiautomation;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ExplorerTest.class,
    WAProjectWizardTest.class,
    WAPropertyTest.class,
    WARemotePageTest.class,
    WARolePageTest.class,
    WADebugConfigurationTest.class,
    WAEnvironmentVariablesTest.class,
    WALocalStorageTest.class,
    WALoadBalancingTest.class,
    WAComponentTest.class,
    WAToolbarTest.class,
    WAServerConfigurationTest.class
    })
public class AllTestRun {
    private static SWTWorkbenchBot wabot;
    @BeforeClass
    public static void beforeClass() throws Exception {
        wabot = new SWTWorkbenchBot();
        try {
            wabot.viewByTitle("Welcome").close();
            wabot.menu("Window").menu("Reset Perspective...").click();
            wabot.shell("Reset Perspective").activate();
            wabot.sleep(1000);
            wabot.button("Yes").click();
        } catch (Exception e) {
        }
    }
}
