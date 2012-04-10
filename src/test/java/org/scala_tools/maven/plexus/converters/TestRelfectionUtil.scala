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

  def isApproxSubType[T: Manifest, U: Manifest] = manifest[T] <:< manifest[U]

  @Test
  def injectBoxedPrimitives(){
    val mojo = new DummyScalaMojo();
    injectIntoVar(mojo, "otherVar", Int.box(100))
    assertEquals(100, mojo.otherVar);

    injectIntoVar(mojo, "aShort", Short.box(2))
    assertEquals(2, mojo.aShort);

    injectIntoVar(mojo, "aLong", Long.box(911))
    assertEquals(911, mojo.aLong);

    injectIntoVar(mojo, "aDouble", Double.box(2.11))
    assertEquals(2.11, mojo.aDouble,0);

    injectIntoVar(mojo, "aChar", Char.box('A'))
    assertEquals('A', mojo.aChar);

    injectIntoVar(mojo, "aByte", Byte.box(1))
    assertEquals(1, mojo.aByte);
  }
}
