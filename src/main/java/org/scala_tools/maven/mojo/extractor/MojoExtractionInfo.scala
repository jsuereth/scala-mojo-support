package org.scala_tools.maven.mojo.extractor

import org.apache.maven.plugin.descriptor.MojoDescriptor
import org.apache.maven.plugin.descriptor.Parameter
import org.scala_tools.maven.mojo.annotations._;


/**
 * Helper class that can extract a MojoDescriptor from a MojoClassInfo object.
 */
trait MojoExtractionInfo {
  def extractMojoDescriptor(mojoInfo: MojoClassInfo) : MojoDescriptor = {
    val desc = new MojoDescriptor()
    desc.setLanguage("scala");
    desc.setComponentConfigurator("scala");
    desc.setImplementation(mojoInfo.name)
    // Add mojo annotations.  It's tedious code like this where
    // I wish for some escape.
    mojoInfo.annotations foreach {
      case goal(name)                          => desc.setGoal(name)
      case phase(name)                         => desc.setPhase(name)
      case configurator(roleHint)              => desc.setComponentConfigurator(roleHint)
      case executeGoal(value)                  => desc.setExecuteGoal(value)
      case executePhase(value)                 => desc.setExecutePhase(value)
      case inheritByDefault(value)             => desc.setInheritedByDefault(value)
      case instantiationStrategy(value)        => desc.setInstantiationStrategy(value)
      case requiresDependencyResolution(scope) => desc.setDependencyResolutionRequired(scope)
      case requiresDirectInvocation(value)     => desc.setDirectInvocationOnly(value)
      case requiresOnline(value)               => desc.setOnlineRequired(value)
      case requiresProject(value)              => desc.setProjectRequired(value)
      case requiresReports(value)              => desc.setRequiresReports(value)
      case description(value)                  => desc.setDescription(value)
      case since(value)                        => desc.setSince(value)
      case executePhaseInLifecycle(phase, lifeCycle) =>
        desc.setExecutePhase(phase)
        desc.setExecuteLifecycle(lifeCycle)
      case _ => ()
    }
    //Add all parameters
    for(param <- mojoInfo.parameters) {
      val paramInfo = new Parameter()
      paramInfo setName param.name
      paramInfo setType param.typeClass
      //TODO - Set implementation to type?
      param.annotations foreach {
        case required()         => paramInfo.setRequired(true)
        case readOnly()         => paramInfo.setEditable(false)
        case expression(value)  => paramInfo.setExpression(value)
        case alias(value)       => paramInfo.setAlias(value)
        case description(value) => paramInfo.setDescription(value)
        case since(value)       => paramInfo.setSince(value)
        case _                  => ()
      } 
      desc addParameter paramInfo
    }    
    desc
  }
  
  
}



//Class information string
case class MojoClassInfo(name: String, 
                         annotations: List[MavenAnnotation],
                         parameters: List[MojoInjectedVarInfo])

case class MojoInjectedVarInfo(name: String, 
                               typeClass: String,
                               annotations : List[MavenAnnotation])
