#!/usr/bin/python
"""
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
"""

from optparse import OptionParser
from zipfile import ZipFile
from subprocess import Popen,PIPE
from glob import glob
import os,re

def main() :
    usage= "usage: %prog [optons]"
    description = "Generate a jar file which contains the OpenJPA class files which have been changed. The output from svn info and svn diff may also be included. A typical \
            use is to run from the same directory as the parent pom. This will automatically include all changed files with a .java extension."
    version = "$prog 0.5"
    parser = OptionParser(usage=usage, version=version, description = description) 
    parser.add_option("-f", "--files", dest="files", help="list of filenames to include")
    parser.add_option("-p", "--patchfile", dest="patchfile", help="patch file name", default="patch.jar")
    parser.add_option("-i", "--noinfo", action="store_false", dest="includeInfo", help="exclude output from svn info", default=True)
    parser.add_option("-d", "--nodiff", action="store_false", dest="includeDiff", help="exclude output from svn diff",default=True)
    parser.add_option("-v", "--verbose", action="store_true", dest="verbose", help="print debug information",default=False)
    # parser.add_option("-p", "--pattern", dest="pattern", help="regex of filenames to match")

    global options
    (options,args) = parser.parse_args()

    genZip(options)

def genZip(options):
    """ generate the zip file """
    if options.files == None:
        files = getAllFiles() 
    else :
        files = getFilesFromList(options.files)

    zip = ZipFile(options.patchfile, 'w')

    if options.includeInfo: 
        writeInfo(zip, files)
    if options.includeDiff:
        writeDiff(zip, files)

    writeClasses(zip, files)

    zip.close()

    print 'Wrote patch to %s. ' % (options.patchfile) 
    if options.verbose:
        print 'Files in patch: %s' % ("\n".join(files))

def getAllFiles():
    """ get the list of all modified files. Only Added or Modified .java, .properties files will be considered """
    if options.verbose:
        print ' > getAllFiles'
    files = []

    commandOutput =  Popen(['svn', 'stat'], stdout=PIPE).communicate()[0].splitlines()
    prog = re.compile('([A|M] *)(.*\.java)')
    for candidate in commandOutput: 
        match = prog.match(candidate)
        if match : 
            files.append(match.group(2))

    prog = re.compile('([A|M] *)(.*\.properties)')
    for candidate in commandOutput: 
        match = prog.match(candidate)
        if match : 
            files.append(match.group(2))


    if options.verbose:
        print 'Found modified files : ' + str(files)
        print ' < getAllFiles'
    return files

def getFilesFromList(files) :
    """ get a list of modified files from a comma separated list usually from the command line """

    if options.verbose :
        print ' > getFilesFromList'
        print ' returning ' + str(files.split(','))
        print ' < getFilesFromList' 

    return files.split(',')

def writeInfo(zip, files=None):
    """ write the output of svn info to a temp file and store in a zip """
    if options.verbose : 
        print ' > writeInfo'

    patchFile = open('info.txt', 'w')
    args = ['svn', 'info']
    
    if files: 
        args.extend(files)

    Popen(args=args, stdout=patchFile).communicate()[0] 

    zip.write(patchFile.name)
    patchFile.close()
    os.remove(patchFile.name)

    if options.verbose: 
        print ' < writeInfo' 

def writeDiff(zip, files=None):
    """ Write the output of svn diff to a temp file and store in a zip """
    if options.verbose:
        print ' > writeDiff'

    patchFile = open('patch.txt', 'w')
    args = ['svn', 'diff']
    if files: 
        args.extend(files)

    Popen(args=args, stdout=patchFile).communicate()[0] 

    zip.write(patchFile.name)
    patchFile.close()
    os.remove(patchFile.name)

    if options.verbose: 
        print ' < writeDiff'

def javaToClass(file) :
    """ simple helper function, converts a string from svn stat (or command line) to its corresponding
    .class file
    """

    rval = file.replace('src','target').replace('main','classes').replace('test','test-classes').replace('.java', '.class').replace('java','').replace('\\\\','\\')
    return rval;

def javaToInnerClass(file):
    """ helper function, converts .java file to a glob pattern that matches inner classes """
    return javaToClass(file).replace('.class', '$*.class')

def writeClasses(zip, files=None): 
    """ Write class files to a zip """
   
    prog = re.compile('(.*classes.)(.*)')
    propertiesProg = re.compile('(.*resources.)(.*)')
    
    for file in files :
        if str(file).endswith('.java'):
            for globMatch in glob(javaToClass(file)) :
                if(prog.match(globMatch)):
                    target = prog.match(globMatch).group(2)
                    zip.write(os.path.realpath(globMatch), target)

            # match again on inner classes, not sure if glob supports optional matches. 
            for globMatch in glob(javaToInnerClass(file)) : 
                if(prog.match(globMatch)): 
                    target = prog.match(globMatch).group(2)
                    zip.write(os.path.realpath(globMatch), target)
        elif str(file).endswith('.properties'):
            for globMatch in glob(file) :
                 if(propertiesProg.match(globMatch)): 
                    target = propertiesProg.match(globMatch).group(2)
                    zip.write(os.path.realpath(globMatch), target)

if __name__ == "__main__":
    main()

