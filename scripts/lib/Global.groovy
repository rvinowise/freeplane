/**
 *
 * DESCRIPTION
 *
 * static functions for management of global variables.
 * These variables have the following properties:
 *
 *  - choice of scope for each variable
 *    - global for local map
 *    - global for Freeplane instance
 *    - global for a different map [TODO]
 *  - persistent variable content (valid after restart and relocation)
 *  - content can be edited using any text editor (no Freeplane necessary)
 *  - ...
 *
 *
 * CAUTION
 *
 *  - library functions create a subdirectory '_global_vars'
 *    according to the choice of scope. If this folder name
 *    is already used, the code must be altered before function
 *    execution.
 *
 *    -> in case of scope='map' [DEFAULT]
 *       the folder will be created below the current map's location
 *
 *    -> in case of scope='global'
 *       the folder will be created below the user's application folder
 *
 *
 * AUTHOR
 *
 *   nnako, 2017
 *
 */


//
// INCLUDES
//

import org.freeplane.plugin.script.proxy.ScriptUtils
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper


//
// STATIC FUNCTIONS
//

def static get( def strVarname, def strScope='map' ) {

    // get Freeplane objects
    def node = ScriptUtils.node()
    def c = ScriptUtils.c()
    def strSubfolder = '_global_vars'
    def infile = ''

    // check for scope
    if( strScope.equals( 'map' ) ) {

        // construct file name
        infile = new File( 
            node.mindMap.file.getParent() + File.separator
            + strSubfolder + File.separator
            + strVarname + '.json' )

    } else if( strScope.equals( 'global' ) ) {

        // construct file name
        infile = new File( 
            c.getUserDirectory().toString() + File.separator
            + strSubfolder + File.separator
            + strVarname + '.json' )

    }
    // ... to be continued for foreign-map-wide global variable access
    else {
        return
    }

    // check if file exists
    if( infile.exists() && !infile.isDirectory() ) {

        // return value to caller
        return new JsonSlurper().parseText( infile.text )[0]

    }

}

def static set( def strVarname, def objValue, def strScope='map' ) {

    // get Freeplane objects
    def node = ScriptUtils.node()
    def c = ScriptUtils.c()
    def strSubfolder = '_global_vars'
    def outfile = ''

    // check for scope
    if( strScope.equals( 'map' ) ) {

        // construct file name
        outfile = new File(
            node.mindMap.file.getParent() + File.separator
            + strSubfolder + File.separator
            + strVarname + '.json' )

    } else if( strScope.equals( 'global' ) ) {

        // construct file name
        outfile = new File(
            c.getUserDirectory().toString() + File.separator
            + strSubfolder + File.separator
            + strVarname + '.json' )
    }
    // ... to be continued for foreign-map-wide global variable access
    else {
        return
    }

    // create global's folder (if it doesn't exist)
    outfile.getParentFile().mkdirs()

    // write value into file
    outfile.write( new JsonBuilder( [ objValue ] ).toPrettyString() )

}