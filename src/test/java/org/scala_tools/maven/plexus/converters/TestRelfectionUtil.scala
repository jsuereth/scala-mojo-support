package org.scala_tools.maven.plexus.converters

import org.junit.Assert._;
import org.junit.Test;
import ReflectionUtil._

/**
 * Tests the reflection API for scala vars.
 */
class TestRelfectionUtil {
  
  @Test
  def mustInjectVars() {
    val mojo = new DummyScalaMojo();
	injectIntoVar(mojo, "dummyVar", "HAI");
	assertEquals("HAI", mojo.dummyVar);
  }
  
  @Test
  def mustFindFields() {
	val mojo = new DummyScalaMojo();
	val varType = getVarType(mojo, "dummyVar");
	assertTrue(varType.isDefined);
	assertEquals(classOf[String], varType.get);

    val varType2 = getVarType(mojo, "dummyVar2");
	assertFalse(varType2.isDefined);
	
	val varType3 = getVarType(mojo, "otherVar");
	assertTrue(varType3.isDefined);
    assertEquals(classOf[Int], varType3.get);
  }
}
