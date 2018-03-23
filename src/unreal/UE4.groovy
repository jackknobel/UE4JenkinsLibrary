#!/usr/bin/groovy

package unreal;

enum BuildConfiguration
{
	DebugGame,
	Development,
	Test,
	Shipping
}

def GetBuildConfigurationChoices()
{
	return Arrays.toString(BuildConfiguration.values()).replaceAll('^.|.$', "").split(", ").join("\n")
}

// Return UBT Directory
def GetUBTDirectory()
{
	return '/Engine/Build/BatchFiles/Build.bat'
}

// Return UAT Directory
def GetUATDirectory()
{
	return '/Engine/Build/BatchFiles/RunUAT.bat'
}

// Return the editor CMD Directory
def GetCMDDirectory()
{
	return '/Engine/Binaries/Win64/UE4Editor-Cmd.exe'
}

// Project Specific Directories
def ProjectName = ''
def EngineUAT	= ''
def EngineUBT	= ''
def EditorCMD	= ''
def ProjectDir	= ''
def ProjectFile	= ''

def Initialise(String projectName, String workingRoot)
{
	ProjectName		= projectName
	EngineUAT		= "\"${workingRoot}" + GetUATDirectory() + "\""
	EngineUBT       = "\"${workingRoot}" + GetUBTDirectory() + "\""
	EditorCMD       = "\"${workingRoot}" + GetCMDDirectory() + "\""
	ProjectDir      = "${workingRoot}/${ProjectName}"
	ProjectFile     = "\"${ProjectDir}/${ProjectName}.uproject\""
}

/* Generate Project files for the initialised project */
def GenerateProjectFiles()
{
	bat "${EngineUBT} -projectfiles -project=${ProjectFile} -game -engine -progress"
}

/** 
  * Compile passed in project for a given BuildConfiguration.
  *	target - The Compilation target
  *	buildConfiguration - The compilation configuration type
  * platform - the target compilation platform
  * additionalArguments - Additional arguments to pass to the compiler
 */ 
def Compile(String target, BuildConfiguration buildConfiguration, String platform = "Win64", String additionalArguments = "")
{
	bat "${EngineUBT} ${target} ${ProjectFile} ${platform} " +  buildConfiguration.name() + " " + additionalArguments
}

/** 
  * Compile passed in project for a given BuildConfiguration. 
  *	buildConfiguration - The compilation configuration type
  * editor - Whether or not this target is for editor
  * platform - the target compilation platform
  * additionalArguments - Additional arguments to pass to the compiler
 */ 
def CompileProject(BuildConfiguration buildConfiguration, boolean editor = true, String platform = "Win64", String additionalArguments = "")
{
	String projectTarget = "${ProjectName}"
	if(buildConfiguration <= BuildConfiguration.Development && editor)
	{
		projectTarget += "Editor"
	}
	Compile(projectTarget, buildConfiguration, platform, additionalArguments)
}

/** 
  * Cook the project for the given platform(s)
  * mapsToCook - The maps we want cooked
  * iterative - Use iterative cooking
  *	platforms - The desired cooking platform. Each platform should be seperated by a +. e.g. WindowsNoEditor+Xbox+Linux
  * additionalArguments - Optional arguments to pass to the cooker
 */ 
def CookProject(String platforms = "WindowsNoEditor", String mapsToCook, boolean iterative = true, String additionalArguments = "-compressed")
{
	 bat "${EditorCMD} ${ProjectFile} -run=Cook -targetplatform=${platforms} -map=${mapsToCook} ${additionalArguments}" + iterative ? " -iterate -iterateshash" : ""
}

/** 
  * Deploy the project to a platform
  * buildConfiguration - The BuildConfiguration type of this deployment
  * usePak - Whether or not to use pak files
  *	outputDir - The staging directory we want to output this deployment to
  * iterative - Use iterative deployment
  * additionalArguments - Optional arguments to pass to the deployment command
 */ 
def Deploy(String platform, BuildConfiguration buildConfiguration, boolean usePak, String outputDir, boolean iterative = true, String additionalArguments = "")
{
	bat "${EngineUAT} BuildCookRun -project=${ProjectFile} -platform=${platform} -skipcook -skipbuild -nocompileeditor -NoSubmit -stage -package -clientconfig=" + buildConfiguration.name() + (usePak ? " -pak " : " ") + additionalArguments + " -StagingDirectory=\"${outputDir}\"" +  iterative ? " -iterativedeploy" : ""
}

/** 
  * Deploy the project to a platform
  * consoleIP - The IP of the console we want to deploy to
  * buildConfiguration - The BuildConfiguration type of this deployment
  *	outputDir - The staging directory we want to output this deployment to
  * iterative - Use iterative deployment
  * additionalArguments - Optional arguments to pass to the deployment command
 */ 
def DeployXbox(String consoleIP, BuildConfiguration buildConfiguration, String outputDir, boolean iterative = true, String additionalArguments = "")
{
	Deploy("XboxOne", buildConfiguration, true, outputDir, iterative, "-cmdline=-Messaging -device=XboxOne@" + consoleIP + " " + additionalArguments)
}

// Build the project's DDC, recommend to use in combation with a shared DDC https://docs.unrealengine.com/en-us/Engine/Basics/DerivedDataCache
def BuildDDC()
{
	 bat "${EditorCMD} -run=DerivedDataCache -fill -project=${ProjectFile}"
}

return this