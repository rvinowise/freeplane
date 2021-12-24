import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import groovy.io.FileType
import groovy.time.TimeCategory 
import groovy.time.TimeDuration

import org.apache.commons.io.FilenameUtils 

import com.rvinowise.freeplane.Text_styler
import com.rvinowise.freeplane.Renamer
import com.rvinowise.freeplane.File_system

//test
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files

Commander com = new Commander(node, c)

com.run_on_node(node)

@Log
class Commander {

def c
def node
def user_dir
def styler //Text_styler
def renamer
File_system file_system

Commander(_node, _c) {
    c = _c
    node = _node
	styler = new Text_styler(c, node)
	renamer = new Renamer(c, node)
	file_system = new File_system(c, node);
	
    user_dir = c.getUserDirectory()
    FileHandler handler = new FileHandler("$user_dir/logs/Commander.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)
}


def run_on_node(main_node) {
	// def file_path = main_node.link.file.toURI()
	// def base_path = main_node.map.file.parentFile.toURI()
	// def rel_path = base_path.relativize(file_path)
	// c.statusInfo = rel_path
	
	// def uri = rel_path.toString() + "#ID_111853252"
	// main_node.link.text = uri
	
	// def basedir = main_node.map.file.parentFile.toPath()
	// log.info("basedir=$basedir")
	// log.info(new File("D:/test.txt").name)
	// def test = basedir.toFile()
	// basedir.toFile().traverse(type: groovy.io.FileType.FILES) { file ->
		// if (FilenameUtils.getExtension(file.name) == 'mm') { 
			// log.info(file.name)
		// }
	// }
	// return

    def commands;
    def text;
    (commands, text) = extract_tags(main_node.text)
    main_node.text = text; 

	c.getSelecteds().each{node -> 
		commands.each{command->
			if (command == "folder"){
				renamer.create_folder_for_branch(main_node) 
				//change_path(main_node)
			} else
			if (command == "name"){
				//test
				//renamer.reroute_paths_for_branch(main_node)
				// def old_path = Paths.get("D:\\archive\\cloud_storages\\google\\study\\religion\\immaterial spirit.mm")
				// def new_path = new File(
				// "D:\\archive\\cloud_storages\\google\\study\\religion\\commonalities\\immaterial spirit.mm"
				// ).toPath().toAbsolutePath().normalize()
				
				// c.statusInfo = Files.move(old_path, new_path)
				
			} else
			if (styler.is_styling_command(command)) {
				styler.node = node
				styler.apply_command(command)
			} 
		}
	}
	// if (renamer.wrong_link_name(main_node)) {
		// renamer.reroute_paths(main_node)
	// } 
}

/* test */
 // def change_path(folder_node) {
	// def folder = file_system.obtain_folders(folder_node)
	// log.info("beginning of change_path, folder= "+folder.toString())
	// folder_node.findAllDepthFirst().each() {_node ->
		// log.info("checking a node for changing path: "+_node.toString())
		 // if (node.link.file) {
			// def new_file = java.nio.file.Paths.get(
				// folder.toString(), 
				// FilenameUtils.getName(node.link.file.name)
			// ).toFile()
			// log.info("renaming "+node.link.file.toString()+" into "+new_file)
			// rename_with_references(
				// folder_node.root.map.file.parentFile,
				// node.link.file,
				// new_file
			// )
		// } 
	// }
	// file_system.reference_file(folder_node, folder)
// } 
/* end test */





def extract_tags(String text) {
    def tags = []
    StringBuilder chars = StringBuilder.newInstance()
    chars << text
    def tag_positions = []
    String current_tag = null
    String tag_start = '`'
    for (int i = 0; i < chars.size(); i++){
        char symbol = chars[i]
        char next_symbol = (i<chars.size()-1) ? chars[i+1] : '\0' as char    
        if (symbol == tag_start) {
            if (current_tag != null) {
                tags.add(current_tag)
            }
            chars.setCharAt(i, "\0" as char)
            current_tag = new String()
        } else
        if (symbol == ' ') {
            if (current_tag != null) {
                tags.add(current_tag)
                current_tag = null
                
            }
            if (next_symbol == tag_start) {
                chars.setCharAt(i, "\0" as char)
            }
        } else {
            if (current_tag != null) {
                current_tag = "$current_tag$symbol"
                chars.setCharAt(i, "\0" as char)
            }
        }
    }
    if (current_tag != null) {
        tags.add(current_tag)
    }
    
    String out_text = chars.toString().replaceAll("\0", "").trim()
    
    return [tags, out_text]
}


}

