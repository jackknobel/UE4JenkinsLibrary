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

def GenerateProjectFiles()
{
	bat "\"${EngineUBT} -projectfiles -project=${ProjectFile} -game -engine -progress\""
}

// Replace with enum for config type???
def CompileProject(CompilationConfig compilationConfig, boolean editor = true, String platform = "win64")
{
	compilationTarget = "${ProjectName}"
	if(compilationConfig <= CompilationConfig.Development && editor)
	{
		compilationTarget += "Editor"
	}
	bat "${EngineUBT} ${compilationTarget} ${ProjectFile}" + compilationTarget.name() + "${platform}"
}

// Each platform should be seperated by a +. e.g. WindowsNoEditor+Xbox+Linux 
def CookProject(String platforms = "WindowsNoEditor", String additionalArguments = "-iterate")
{
	 bat "${EditorCMD} -run=Cook -project=${ProjectFile} -targetplatform=${platforms} ${additionalArguments}"
}

def BuildDDC()
{
	 bat "%EDITOR_CMD% -run=DerivedDataCache -fill -project=%PROJECT_FILE%"
}


return this