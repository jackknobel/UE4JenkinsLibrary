#!/usr/bin/groovy

package unreal;

enum CompilationConfig
{
	DebugGame,
	Development,
	Test,
	Shipping
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
def EngineUBT	= ''
def EditorCMD	= ''
def ProjectDir	= ''
def ProjectFile	= ''

def Initialise(String projectName, String workingRoot)
{
	ProjectName		= projectName
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
  * Compile passed in project for a given CompilationConfig type. 
  *	compilationConfig - The compilation configuration type
  * editor - Whether or not this target is for editor
  * platform - the target compilation platform
  * additionalArguments
 */ 
def CompileProject(CompilationConfig compilationConfig, boolean editor = true, String platform = "win64", String additionalArguments = "")
{
	compilationTarget = "${ProjectName}"
	if(compilationConfig <= CompilationConfig.Development && editor)
	{
		compilationTarget += "Editor"
	}
	bat "${EngineUBT} ${compilationTarget} ${ProjectFile}" + compilationTarget.name() + "${platform}" + additionalArguments
}

/** 
  * Compile passed in project for a given CompilationConfig type. 
  *	platforms - The desired cooking platform. Each platform should be seperated by a +. e.g. WindowsNoEditor+Xbox+Linux
  * additionalArguments - Optional arguments to pass to the cooker
 */ 
def CookProject(String platforms = "WindowsNoEditor", String additionalArguments = "-iterate")
{
	 bat "${EditorCMD} ${ProjectFile} -run=Cook -targetplatform=${platforms} ${additionalArguments}"
}

// Build the project's DDC, recommend this in combation with a shared DDC https://docs.unrealengine.com/en-us/Engine/Basics/DerivedDataCache
def BuildDDC()
{
	 bat "${EditorCMD} -run=DerivedDataCache -fill -project=${ProjectFile}"
}

return this