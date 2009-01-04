package org.scala_tools.maven.mojo.extractor

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor;
import org.apache.maven.tools.plugin.util.PluginUtils;

import scala.collection.jcl.Conversions._
/**
 * This class is responsible for extracting mojo descriptions from scala files.
 */
class ScalaMojoDescriptionExtractor extends MojoDescriptorExtractor {
    /**
	 * This is responsible for pulling mojo descriptions off of scala files in the project.
	 */
    @throws(classOf[ExtractionException]) 
	override def execute(project : MavenProject, pluginDescriptor : PluginDescriptor) : java.util.List[_]= {
		//TODO - parse through scala file and rip out MOJO annotations
		val mojoDescriptions : Seq[MojoDescriptor]= for {
		  root <- project.getCompileSourceRoots().asInstanceOf[java.util.List[String]]
          if new java.io.File(root).isDirectory
          source <- PluginUtils.findSources(root, "**/*.scala")
		} yield extractMojoDescription(source, project, pluginDescriptor)

        val descriptionsAsList = new java.util.ArrayList[MojoDescriptor](mojoDescriptions.length)
        for(desc <- mojoDescriptions) {
          descriptionsAsList.add(desc)
        }
        descriptionsAsList
	}
	/**
	 * Attempt to extract the mojo description from a particular file.
	 * @param source
	 * @param project
	 * @param pluginDescriptor
	 */
	private def extractMojoDescription(source : String, project : MavenProject, pluginDescriptor : PluginDescriptor) : MojoDescriptor = {
		try {
			val descriptor = new MojoDescriptor();
			//TODO - Pull this from inside the .scala file.
			descriptor.setPluginDescriptor(pluginDescriptor);
			//For now let's use a lame algorithm just to test to see if this will work.
			descriptor.setDescription("Testing Scala extraction mojo");
			descriptor.setGoal("echo");
			descriptor.setExecuteGoal("echo");
			descriptor.setExecutePhase("process-sources");
			descriptor.setPhase("process-sources");
			descriptor.setLanguage("scala");
			descriptor.setComponentConfigurator("scala");
			descriptor.setVersion(project.getModelVersion());
			descriptor.setImplementation("org.scala_tools.mojo.TestMojo");
	
			
			val parameter = new Parameter();
			parameter.setName("outputDirectory");
			parameter.setExpression("${project.build.directory}");
			parameter.setType("java.io.File");
			parameter.setRequired(true);            
			descriptor.addParameter(parameter);
   
            val constructorArg = new Parameter()
            constructorArg.setName("$$constructor$$1")
            constructorArg.setExpression("${project.finalName}")
            constructorArg.setType("java.lang.String")
            constructorArg.setRequired(true)
            constructorArg.setAlias("name")
            descriptor.addParameter(constructorArg)
   
			System.err.println("Analyzing: " + source);		
			descriptor;
		} catch {
		  case _ =>
			return null;
		}
	}
}
