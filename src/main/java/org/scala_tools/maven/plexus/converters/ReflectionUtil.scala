package org.scala_tools.maven.plexus.converters

object ReflectionUtil {
  
  /**
	 * Attempts to determine the "type" of a var on a scala object.
	 * 
	 * TODO - figure out what to do with type erasure + generics...
	 * 
	 * @param obj
	 *             The scala object
	 * @param varName
	 *             The name of the var
	 * @return
	 *             The type of the var, or None if no var is found
	 */
  def getVarType(obj : AnyRef, varName : String) : Option[Class[_]] = {
    val method = getMethod(obj, varName)
    method.map(_.getReturnType)
  }
  
  private def getMethod(obj : AnyRef, varName : String) : Option[java.lang.reflect.Method] = {
    (for{
      m <- obj.getClass.getMethods
      if m.getName == varName
    } yield m).headOption
  }

  private def getVarSetMethod(obj : AnyRef, varName : String) : Option[java.lang.reflect.Method] = {
    getMethod(obj, varName + "_$eq")
  }

  /**
   * A mapping of primitive types to their boxed default values.
   *
   * See the Java Language Specification, Java SE 7 Edition, §4.12.5 "Initial Values of Variables".
   */
  private val boxedDefaultValues = scala.collection.immutable.Map[Class[_], AnyRef](
    java.lang.Byte.TYPE -> ((0: Byte): java.lang.Byte),
    java.lang.Short.TYPE -> ((0: Short): java.lang.Short),
    java.lang.Integer.TYPE -> (0: java.lang.Integer),
    java.lang.Long.TYPE -> (0L: java.lang.Long),
    java.lang.Float.TYPE -> (0f: java.lang.Float),
    java.lang.Double.TYPE -> (0d: java.lang.Double),
    java.lang.Boolean.TYPE -> java.lang.Boolean.FALSE,
    java.lang.Character.TYPE -> ('\u0000': java.lang.Character)
  )

	/**
	 * This method will inject a value into a "var" on a scala object.
	 * @param obj
	 *          The scala object
	 * @param varName
	 *          The name of the var
	 * @param value
	 *          The value to inject
	 * @throws IllegalArgumentException
	 *          This is thrown if any error occurs (i.e. invalid inputs...)
	 */
  def injectIntoVar[A <: AnyRef](obj : AnyRef, varName : String, value : A) {
    getVarSetMethod(obj, varName) match {
      case Some(method) if method.getParameterTypes.head.isPrimitive && value == null =>
        // If it's a primitive type and the input is null, set it to the default value for that primitive type.
        method.invoke(obj, boxedDefaultValues(method.getParameterTypes.head));

      case Some(method) =>
        // Don't bother checking types. Maven knows what the correct type is already, since it's listed in the plugin descriptor, and Java reflection will handle unboxing for us.
        method.invoke(obj, value);
      case _ => throw new IllegalArgumentException("Invalid var for injection: " + varName + " on: " + obj);
    }
  }
}
