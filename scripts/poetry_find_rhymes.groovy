import groovy.util.logging.Log
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter

import groovy.time.TimeCategory 
import groovy.time.TimeDuration

import java.nio.charset.Charset;
import com.rvinowise.freeplane.NodeManipulator


Rhyme_finder rhyme_finder = new Rhyme_finder(c, node)
rhyme_finder.find_rhymes(node)


class Rhymed_ending{
	String text
	def base_word
	def rhymed_word
	def same_words(Rhymed_ending other) {
		return (
			(base_word.id == other.base_word.id)&&
			(rhymed_word.id == other.rhymed_word.id)
		) || (
			(base_word.id == other.rhymed_word.id)&&
			(rhymed_word.id == other.base_word.id)
		)
	}
	def getInfo() {
		String base_text = "null"
		String rhymed_text = "null"
		if (base_word != null) {
			base_text = base_word.details
			
		}
		if (rhymed_word != null) {
			rhymed_text = rhymed_word.details
		}
		return "$text ($base_text | $rhymed_text)"
	}
	String toString() {
		return getInfo()
	}
}
/* 
	stanza -> 
	lines -> 
	possible_words -> 
	transliteration -> 
	ending
 */
@Log
class Rhyme_finder {

def c
def node
def nm
def endings_hub
def filter_node

Rhyme_finder(_c,_node) {
	c = _c
	node = _node
	nm = new NodeManipulator(c, node);
	init_control_nodes()
	def user_dir = c.getUserDirectory()
	FileHandler handler = new FileHandler("$user_dir/logs/poetry2.log", true);
	handler.setFormatter(new SimpleFormatter())
	log.addHandler(handler)
}

def init_control_nodes() {
	endings_hub = nm.obtain_node(
		'groupped endings', node.mindMap.root
	)
	filter_node = nm.obtain_node(
		'filter', node.mindMap.root
	)
}

def find_rhymes(base_line) {
	def stanza = base_line.parent
	
	for(line in stanza.children) {
		if (line != base_line) {
			check_rhymes_between_ONE_CYCLE(base_line, line)
		}
	}

	//TimeDuration td = TimeCategory.minus( stop, start )
	//c.statusInfo = td
}

def check_rhymes_between_ONE_CYCLE(base_line, rhymed_line) {
	
	
	def base_endings = get_rhymed_endings(base_line)
	def rhymed_endings = get_rhymed_endings(rhymed_line)
	
	def endings = intersect_rhymed_endings(
		base_endings,
		rhymed_endings,
		this.&compare_rhymed_endings
	)

	delete_unnecessary_rhymes(endings)

	def rhymes_hub = nm.obtain_node('rhymes with '+rhymed_line.text, base_line)
	for (ending in endings) {
		append_found_rhyme(
			rhymes_hub, 
			ending.base_word, 
			ending.rhymed_word, 
			base_line,
			rhymed_line,
			ending.text
		)
	}
}

def get_rhymed_endings(line) {
	def endings = get_segmented_endings(line)
	//log.info("get_segmented_endings: $endings")
	def rhymed_endings = []
	endings.each{ ending -> 
		def variations = get_ending_variations(ending)
		//log.info("variations of $ending : $variations")
		variations.each { variation ->
			def rhymed_ending = new Rhymed_ending()
			rhymed_ending.text = variation
			rhymed_ending.base_word = get_word_with_ending(ending.last())
			rhymed_endings.add(rhymed_ending)
		}
	}
	
	return rhymed_endings
}

def intersect_rhymed_endings(
    array1,
    array2,
    comparator
) {
    def result = []
    for (item1 in array1) {
        for (item2 in array2) {
            if (comparator(item1,item2) == 0) {
                def combined_ending = new Rhymed_ending()
				combined_ending.text = item1.text
				combined_ending.base_word =item1.base_word
				combined_ending.rhymed_word = item2.base_word
				result.add(combined_ending)
            }
        }
    }
    return result
}



def compare_rhymed_endings(o1, o2) {
	return o1.text <=> o2.text
}


def get_rhymed_part(segment) {
	return segment.text.replace('+','').replace('?','')
}



// def find_equalities(
	// base_ending,
	// checked_ending
// ) {
	// def base_variations = get_ending_variations(base_ending)
	// def checked_variations = get_ending_variations(checked_ending)
	// def equalities = nm.intersect(
		// base_variations,
		// checked_variations,
		// this.&compare_strings
	// )

	// return equalities;
// }


def get_ending_variations(segmented_ending) {
	def variations = get_segment_variations(segmented_ending.getAt(0))
	
	for(int i=1; i<segmented_ending.size(); i=i+1) {
		def segment = segmented_ending.getAt(i)
		variations = combine_variations(
			variations,
			get_segment_variations(segment)
		)
	}
	
	return variations;
}

def get_segment_variations(segment) {
	def variations = []
	String segment_text = segment.text;
	if (segment.text.endsWith("?")) {
		segment_text = segment.text.substring(0, segment.text.length()-1)
	}
	endings_hub.children.each { ending_group ->
		if (segment_text == ending_group.text) {
			link_packed_ending_with_explanation(segment, ending_group)
			variations = get_leaves(ending_group)
				.findAll { n ->
					!is_filtered_out(n)
					&&
					!is_comment(n)
				}
				.collect{ n -> 
					get_rhymed_part(n)
				}
		}
		
	}
	if (variations.size()==0) {
		variations.add(get_rhymed_part(segment)) 
	}
	if (segment.text.endsWith("?")) {
		variations.add("")
	}
	
	return variations;
}

def is_filtered_out(variation) {
	filter_node.children.any{ 
		(filter_is_active(it)) &&
		(it.text == variation.parent.text)
	}
}

def is_comment(node) {
	nm.find_back_in_hierarchy(
		node, 
		this.&is_info, 
		endings_hub
	) != null
}

def is_info(node) {
	return node.getIcons().contains("info_circle@b")
		
}

def filter_is_active(filtree) {
	return filtree.getIcons().contains("bad@bleak")
}

def combine_variations(list1, list2) {
	def result = []
	for (elem2 in list2) {
		for (elem1 in list1) {
			result.add(elem2+elem1)
		}
	}
	return result;
}

def link_packed_ending_with_explanation(
	usage, explanation
) {
	if (usage.link.node != explanation) {
		usage.link.node = explanation
	}
}

def get_segmented_endings(node) {
	def endings = []
	get_leaves(node).each{ leaf ->
		if (is_ending_filtered(leaf, node)) {
			return
		}
		def segments = [leaf]
		append_parent_segments(segments)
		endings.add(segments)
		debug_attach_endings(leaf, segments)
	}
	return endings;
}

def debug_attach_endings(node, segments) {
	def var_node = node.createChild()
	var_node.text = get_ending_variations(segments).toString()
	var_node.icons.add("letter_i")
}

def is_ending_filtered(node, local_root) {
	filter_node.children.any{ 
		(filter_is_active(it)) &&
		nm.find_text_in_parents(
			node, 
			it.text, 
			local_root
		) != null
	}
}

def get_leaves(node) {
	def leaves = [];
	node.findAllDepthFirst()
	.each { n ->
		// if (n.text == '') {
		// 	return;
		// }
		if (n.isLeaf())  {
			leaves.add(n)
		}
	}
	return leaves
}

def append_parent_segments(ending) {
	
	def parent = ending.last().parent
	while(
		parent.text.endsWith('+') ||
		parent.text.endsWith('?')
	) {
		ending.add(
			parent
		)
		parent = parent.parent
	}
	
}

def delete_unnecessary_rhymes(endings) {
	Set<Rhymed_ending> useless = []
	for (def i=0; i<endings.size(); i++) {
		for (def j=0; j<endings.size(); j++) {
			if (i==j) {
				continue
			}
			def test0 = endings[i]
			def test1 = endings[j]
			def test2 = endings[i].same_words(endings[j])
			
			if (endings[i].same_words(endings[j])) {
				if (endings[i].text.length() < endings[j].text.length()) {
					useless.add(endings[i])
				} else {
					useless.add(endings[j])
				}
			}
		}
	}
	log.info("useless: $useless")
	log.info("endings BEFORE: $endings")
	endings.removeAll(useless)
	log.info("endings AFTER: $endings")
}

def append_found_rhyme(
	result_node, 
	base_word, 
	rhymed_word, 
	base_line,
	rhymed_line,
	common_ending
) {

	def common_ending_node = nm.obtain_node(
		common_ending, result_node
	)
	def base_line_result = nm.obtain_node(
		base_line.text, common_ending_node
	)
	def rhymed_line_result = nm.obtain_node(
		rhymed_line.text, common_ending_node
	)

	nm.obtain_node(
		base_word.text, base_line_result
	).link.node = base_word
	
	nm.obtain_node(
		rhymed_word.text, rhymed_line_result
	).link.node = rhymed_word

}

def get_word_with_ending(ending) {
	if (ending.parent == null) {
		return ending
	}
	if (ending.parent.text.endsWith(':')) {
        return ending
    }
	if (ending.icons.contains('three_dots@b')) {
        return ending
    }
    return get_word_with_ending(ending.parent)
}

} //Rhyme_finder

