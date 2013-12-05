package org.sbuild.jenkins.plugin;

import java.util.Iterator;
import java.util.LinkedList;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SBuildTest extends Assert {

	@DataProvider
	public Iterator<Object[]> dataSplitArgs() {
		/** Simple helper. */
		class Data extends LinkedList<Object[]> {
			private static final long serialVersionUID = 1L;

			public void data(String toSplit, String... results) {
				add(new Object[] { toSplit, results });
			}
		}
		Data data = new Data();

		data.data("test", "test");
		data.data("test ", "test");
		data.data(" test", "test");
		data.data("test 2", "test", "2");
		data.data("test  2test", "test", "2test");
		data.data("\"a b c\" d \"e f\" g", "a b c", "d", "e f", "g");
		data.data("\"a b c' d' e\" \" f\" g", "a b c' d' e", " f", "g");
		data.data("\"a b c' d e\" \" 'f\" g", "a b c' d e", " 'f", "g");

		return data.iterator();
	}

	@Test(dataProvider = "dataSplitArgs")
	public void testSplitArgs(String toString, String[] expected) {
		SBuild sBuild = new SBuild("sbuildVersion", "targets", "buildFiles", "options");
		assertEquals(sBuild.splitArgs(toString).toArray(new String[0]), expected);
	}

}
