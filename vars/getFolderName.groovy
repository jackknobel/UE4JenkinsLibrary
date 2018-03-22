#!/usr/bin/groovy

def call(def script) 
{
    splitResult = "${script.env.JOB_NAME}".split('/')
    FolderName = splitResult.length > 1 ? splitResult[splitResult.length - 2] : splitResult.max()
    return FolderName
}