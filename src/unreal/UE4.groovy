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

/* Return UBT Directory */
def GetUBTDirectory()
{
	return '/Engine/Build/BatchFiles/Build.bat'
}

/* Return UAT Directory */
def GetUATDirectory()
{
	return '/Engine/Build/BatchFiles/RunUAT.bat'
}

/* Return the editor CMD Directory */
def GetCMDDirectory()
{
	return '/Engine/Binaries/Win64/UE4Editor-Cmd.exe'
}

/* Project Specific Directories */
def ProjectName = ''
def EngineUAT	= ''
def EngineUBT	= ''
def EditorCMD	= ''
def ProjectDir	= ''
def ProjectFile	= ''

/* Arguments to pass to all commands. e.g -BuildMachine */
def DefaultArguments = ''

/* Initialise the Object with a project name, the root working directory and optional default arguments to pass to all commands */
def Initialise(String projectName, String workingRoot, String defaultArguments = "")
{
	ProjectName		= projectName
	EngineUAT		= "\"${workingRoot}" + GetUATDirectory() + "\""
	EngineUBT       = "\"${workingRoot}" + GetUBTDirectory() + "\""
	EditorCMD       = "\"${workingRoot}" + GetCMDDirectory() + "\""
	ProjectDir      = "${workingRoot}/${ProjectName}"
	ProjectFile     = "\"${ProjectDir}/${ProjectName}.uproject\""

	DefaultArguments = defaultArguments
}

/* Generate Project files for the initialised project */
def GenerateProjectFiles()
{
	bat "${EngineUBT} -projectfiles -project=${ProjectFile} -game -engine -progress ${DefaultArguments}"
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
	bat "${EngineUBT} ${target} ${ProjectFile} ${platform} " +  buildConfiguration.name() + " ${additionalArguments} ${DefaultArguments}"
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
  * iterative - Use iterative cooking
  * mapsToCook - The maps we want cooked
  *	platforms - The desired cooking platform. Each platform should be seperated by a +. e.g. WindowsNoEditor+Xbox+Linux
  * additionalArguments - Optional arguments to pass to the cooker
 */ 
def CookProject(String platforms = "WindowsNoEditor", String mapsToCook = "", boolean iterative = true, String additionalArguments = "-fileopenlog")
{
	 bat "${EditorCMD} ${ProjectFile} -run=Cook -targetplatform=${platforms} -map=${mapsToCook} ${additionalArguments} ${DefaultArguments}" + (iterative ? " -iterate -iterateshash" : "")
}

/** 
  * Package the project for a target platform
  * platform - The platform we want to package to
  * buildConfiguration - The BuildConfiguration type of this deployment
  *	stagingDir - The staging directory we want to output this deployment to
  * usePak - Whether or not to use pak files
  * iterative - Use iterative deployment
  * cmdlineArguments - Arguments to pass to the commandline when the package next launches
  * additionalArguments - Optional arguments to pass to the deployment command
 */ 
def PackageProject(String platform, BuildConfiguration buildConfiguration, String stagingDir, boolean usePak = true, boolean iterative = true, String cmdlineArguments = "", String additionalArguments = "")
{
	bat "${EngineUAT} BuildCookRun -project=${ProjectFile} -platform=${platform} -skipcook -skipbuild -nocompileeditor -NoSubmit -stage -clientconfig=" + buildConfiguration.name() + " -StagingDirectory=\"${stagingDir}\"" + (usePak ? " -pak " : " ") + " -cmdline=\"${cmdlineArguments}\" " + "${additionalArguments} ${DefaultArguments}" 
}

/**
  * Package and Deploy the project to a platform
  * platform - The platform we want to package and deploy to
  * buildConfiguration - The BuildConfiguration type of this deployment
  *	stagingDir - The staging directory we want to output this deployment to
  * deviceIP - The IP of the device we want to deploy to
  * usePak - Whether or not to use pak files
  * iterative - Use iterative deployment
  * cmdlineArguments - Arguments to pass to the commandline when the package next launches
  * additionalArguments - Optional arguments to pass to the deployment command
 */ 
def PackageAndDeployProject(String platform, BuildConfiguration buildConfiguration, String stagingDir, String deviceIP, boolean usePak = true, boolean iterative = true, String cmdlineArguments = "", String additionalArguments = "")
{
	PackageProject(platform, buildConfiguration, stagingDir, usePak, iterative, cmdlineArguments, " -Messaging -deploy -device=${platform}@${deviceIP} " + (iterative ? " -iterativedeploy " : " ") + additionalArguments)
}

/* Build the project's DDC, recommend to use in combation with a shared DDC https://docs.unrealengine.com/en-us/Engine/Basics/DerivedDataCache */
def BuildDDC()
{
	 bat "${EditorCMD} -run=DerivedDataCache -fill -project=${ProjectFile} ${DefaultArguments}"
}

return this