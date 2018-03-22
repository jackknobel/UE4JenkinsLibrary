#!/usr/bin/groovy

def call(def script) 
{
    return new File("${script.env.WORKSPACE}").getParentFile().getPath()
}