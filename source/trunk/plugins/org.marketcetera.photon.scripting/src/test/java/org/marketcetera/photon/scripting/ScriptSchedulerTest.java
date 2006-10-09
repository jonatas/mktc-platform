package org.marketcetera.photon.scripting;

import junit.framework.TestCase;

import org.apache.bsf.BSFManager;

public class ScriptSchedulerTest extends TestCase {
	public void testScriptScheduler() throws Exception {
		ScriptScheduler sched = new ScriptScheduler();
		BSFManager manager = new BSFManager();
		
		IScript script = new Script("$foo='bar'; puts 'hello'");
		ScriptRunnable runnable = new ScriptRunnable(script, manager);
		sched.submitScript(script);
		
		// note this should return immediately
		runnable.join();
		Object fooObj = new Script("$foo").eval(manager);
		
		assertEquals(String.class, fooObj.getClass());
		assertEquals("bar", ((String)fooObj));
		sched.shutdown();
	}
}
