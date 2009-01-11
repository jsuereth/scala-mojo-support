package org.scala_tools.maven.mojo.extractor

import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

/**
 * Helper class that can extract a MojoDescriptor from a MojoClassInfo object.
 */
trait MojoExtractionInfo {
  def extractMojoDescriptor(mojoInfo : MojoClassInfo) : MojoDescriptor = {
    val desc = new MojoDescriptor()
    desc.setLanguage("scala");
    desc.setComponentConfigurator("scala");
    desc.setImplementation(mojoInfo.name)
    // Add mojo annotations
    for(annotation <- mojoInfo.annotations) {
      annotation.name.substring("org.scala_tools.maven.mojo.annotations.".length) match {
        case "goal" =>
          desc.setGoal(annotation.args(0))
          desc.setExecuteGoal(annotation.args(0))
        case "phase" =>
          desc.setPhase(annotation.args(0))
          desc.setExecutePhase(annotation.args(0))
        case "configurator" =>
          desc.setComponentConfigurator(annotation.args(0))
        case "executeGoal" =>
          desc.setExecuteGoal(annotation.args(0))
        case "executePhase" =>
          desc.setExecutePhase(annotation.args(0))
        case "executePhaseInLifecycle" =>
          desc.setExecutePhase(annotation.args(0))
          desc.setExecuteLifecycle(annotation.args(0))
        case "executionStrategy" =>
          //TODO -= Figure this one out
        case "inheritByDefault" =>
          desc.setInheritedByDefault(annotation.args(0).toBoolean)
        case "instantiationStrategy" =>
          desc.setInstantiationStrategy(annotation.args(0))
        case "requiresDependencyResolution" =>
          desc.setDependencyResolutionRequired(annotation.args(0))
        case "requiresDirectInvocation" =>
          desc.setDirectInvocationOnly(annotation.args(0).toBoolean)
        case "requiresOnline" =>
          desc.setOnlineRequired(annotation.args(0).toBoolean)
        case "requiresProject" =>
          desc.setProjectRequired(annotation.args(0).toBoolean)
        case "requiresReports" =>
          desc.setRequiresReports(annotation.args(0).toBoolean)
        case "description" =>
          desc.setDescription(annotation.args(0))
        case "since" =>
          desc.setSince(annotation.args(0))
        case _ => //ignore
      }
    }
    //Add all parameters
    for(param <- mojoInfo.parameters) {
      val paramInfo = new Parameter()
      paramInfo.setName(param.name)
      paramInfo.setType(param.typeClass)
      //TODO - Set implementation to type?
      for(annotation <- param.annotations) {
        annotation.name.substring("org.scala_tools.maven.mojo.annotations.".length) match {
          case "required" => paramInfo.setRequired(true)
          case "readOnly" => paramInfo.setEditable(false)
          case "expression" => paramInfo.setExpression(annotation.args(0))
          case "alias" => paramInfo.setAlias(annotation.args(0))
          case "component" => 
            //TODO Add requirement            
          case "description" =>
            paramInfo.setDeprecated(annotation.args(0))
          case "since" =>
            paramInfo.setSince(annotation.args(0))
          case _ => //Ignore
        }
      } 
      desc.addParameter(paramInfo)
    }    
    desc
  }
  
  
}



//Class information string
class MojoClassInfo(val name : String, val annotations : List[MojoAnnotationInfo], val parameters : List[MojoInjectedVarInfo]) {
  override def toString = name + " - " + annotations.mkString("(",",",")") + "\nwithArgs " + parameters.mkString("\n","\n","")
}

class MojoInjectedVarInfo(val name : String, val typeClass : String, val annotations : List[MojoAnnotationInfo]) {
  override def toString = name + " : " + typeClass + " - " + annotations.mkString("(",",",")")
}

class MojoAnnotationInfo(val name : String, val args : List[String]) {
  override def toString = name + args.mkString("(",",",")")
}