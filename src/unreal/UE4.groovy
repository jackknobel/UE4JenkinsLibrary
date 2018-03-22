#!/usr/bin/groovy

package unreal;

class UE4
{
	enum CompilationConfig
	{
		DebugGame,
		Development,
		Test,
		Shipping
	}

	// UE4 Default Paths
	def UBT		= '/Engine/Build/BatchFiles/Build.bat'
	def UAT		= '/Engine/Build/BatchFiles/RunUAT.bat'
	def UE4CMD	= '/Engine/Binaries/Win64/UE4Editor-Cmd.exe'
	
	// Project Specific Directories
	def ProjectName = ''
	def EngineUBT	= ''
	def EditorCMD	= ''
	def ProjectDir	= ''
	def ProjectFile	= ''
	
	def Initialise(String projectName, String workingRoot)
	{
		ProjectName		= projectName
		EngineUBT       = "${workingRoot}${UBT}"
		EditorCMD       = "${workingRoot}${UE4CMD}"
		ProjectDir      = "${workingRoot}/${ProjectName}"
		ProjectFile     = "${ProjectDir}/${ProjectName}.uproject"
	}
	
	def GenerateProjectFiles()
	{
		bat "${EngineUBT} -projectfiles -project=${ProjectFile} -game -engine -progress"
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
}