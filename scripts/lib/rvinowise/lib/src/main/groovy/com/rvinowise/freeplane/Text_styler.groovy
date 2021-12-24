package com.rvinowise.freeplane

import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
//import org.freeplane.core.util
import groovy.io.FileType

import groovy.time.TimeCategory 
import groovy.time.TimeDuration




//styler.apply_style_from_text(node)


class Element {
    String tag
    String type
    String value

    Element(_type, _value) {
        type = _type;
        value = _value;
    }
    Element(_tag, _type, _value) {
        tag = _tag
        type = _type;
        value = _value;
    }

    def apply(node) {
        if (type == 'color') {
            node.style.textColorCode = value
        } else if (type == 'icon') {
            node.icons.add(value)
        }
    }

    def getInfo() {
        return "$this: ($tag, $type, $value)"
    }
}

@Log
class Text_styler {

def c
def node
def user_dir

def elements = [
    black:new Element('color', '#000000ff'),
    gray:new Element('color', '#666666ff'),
    light:new Element('color', '#7f7f7fff'),
    green:new Element('color', '#00aa00ff'),
]


Text_styler() {
}
Text_styler(_c, _node) {
    c = _c
    node = _node

    init(_c, _node)
}

def init(in_c, in_node) {
    c = in_c
    node = in_node
	
	user_dir = c.getUserDirectory()

    def start = new Date()

    

    def stop = new Date()
    TimeDuration td = TimeCategory.minus( stop, start )
	//c.statusInfo = td

    FileHandler handler = new FileHandler("$user_dir/logs/Text_styler.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)
	
	elements = find_user_icons()
}

def find_user_icons() {
    def list = []

    def icon_dir = new File("$user_dir/icons")
    icon_dir.eachFileRecurse (FileType.FILES) { file ->
        //String filename = file.name.take(file.name.lastIndexOf('.'))
		
		def base_path = icon_dir.toURI()
		def file_path = file.toURI()
		def relative_path = base_path.relativize(file_path).toString()
	
		relative_path = relative_path.take(relative_path.lastIndexOf('.'))
		
		log.info("adding element icon: $relative_path")
		elements.put(
            relative_path,
            new Element('icon', relative_path)
        )
    }
    return elements
}

def apply_style_from_text(main_node) {

    def tags;
    def text;
    (tags, text) = extract_tags(main_node.text)
    main_node.text = text; 

	c.getSelecteds().each{node -> 
		tags.each{tag->
			apply_command(tag)
		}
	}
}
//inter Commander
def apply_command(command) {
	if (is_rearranging_icons(command)) {
		log.info("command=$command is rearranging")
		rearrange_icons(node, command)
	} else {
		log.info("command=$command is NOT rearranging")
		def element = find_closest_element(command)
		if (element) {
			element.apply(node)
		}
	}
}


def is_styling_command(command) {
	return is_rearranging_icons(command) || find_closest_element(command)
}
//end inter Commander 

def is_rearranging_icons(tag) {
    return is_numeric(tag)
}

def rearrange_icons(node, tag) {
    def icons = node.icons.collect()
    node.icons.clear()
    
    for (int i=0;i<tag.length();i++) {
        int digit = Integer.valueOf(new String(tag.charAt(i)))
        log.info("digit=$digit")
        node.icons.add(icons[digit-1])
    }
}

boolean is_numeric(String s) {
    for (int j = 0; j < s.length(); j++) {
        char charr = s.charAt(j);
        def numeric = '0' <= charr && charr <= '9';
        if (!numeric) {
            return false;
        }
    }
    return true
} 

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

def find_closest_element(String tag) {
    def closest_element = null
    int longest_match = 0
    elements.each { element ->
        def substrings = longestCommonSubstrings(element.key, tag)
        if (substrings.size() > 0) {
            def substring = substrings.first()
            if (substring.length() > longest_match) {
                longest_match = substring.length()
                closest_element = element.value
            }
            
            log.info("$element.value.info = $substring")
        }
    }

    return closest_element
}


Set<String> longestCommonSubstrings(String s, String t) {
    int[][] table = new int[s.length()][t.length()];
    int longest = 0;
    Set<String> result = [];

    for (int i = 0; i < s.length(); i++) {
        for (int j = 0; j < t.length(); j++) {
            if (s.charAt(i) != t.charAt(j)) {
                continue;
            }

            table[i][j] = (i == 0 || j == 0) ? 1
                                             : 1 + table[i - 1][j - 1];
            if (table[i][j] > longest) {
                longest = table[i][j];
                result.clear();
            }
            if (table[i][j] == longest) {
                result.add(s.substring(i - longest + 1, i + 1));
            }
        }
    }
    return result;
}

}

//styler = new Text_styler()

