#!/usr/bin/groovy

def call() 
{
    return new File("${env.WORKSPACE}").getParentFile().getPath()
}