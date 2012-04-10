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
  def getVarType(obj: AnyRef, varName: String): Option[Class[_]] = {
    val method = getMethod(obj, varName)
    method map (_.getReturnType)
  }
  
  private def getMethod(obj: AnyRef, varName: String): Option[java.lang.reflect.Method] =
    (for{
      m <- obj.getClass.getMethods
      if m.getName == varName
    } yield m).headOption

  private def getVarSetMethod(obj : AnyRef, varName : String) : Option[java.lang.reflect.Method] =
    getMethod(obj, varName + "_$eq")

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
  def injectIntoVar[A <: AnyRef](obj: AnyRef, varName: String, value: A): Unit = {
    getVarSetMethod(obj, varName) match {
      case Some(method) =>
        val varType = method.getParameterTypes.head
        val realClass = value.getClass

        if(!isSubType(realClass,varType)) {
          throw new IllegalArgumentException("Can not coerce: " + realClass + " into a " + varType);
        }

        method.invoke(obj, value);
      case _ => throw new IllegalArgumentException("Invalid var for injection: " + varName + " on: " + obj);
    }
  }

  private def isSubType( source: Class[_], target:Class[_]): Boolean = {

    def isPrimitive[P](implicit m: Manifest[P]): Boolean =
      Class.forName(m.toString).isAssignableFrom(source)

    def isClass: Boolean =
      target.isAssignableFrom(source)

    target.toString match {
      case "boolean" => isPrimitive[java.lang.Boolean]
      case "int"     => isPrimitive[java.lang.Integer]
      case "short"   => isPrimitive[java.lang.Short]
      case "long"    => isPrimitive[java.lang.Long]
      case "float"   => isPrimitive[java.lang.Float]
      case "double"  => isPrimitive[java.lang.Double]
      case "char"    => isPrimitive[java.lang.Character]
      case "byte"    => isPrimitive[java.lang.Byte]
      case _         => isClass
    }

  }

}
