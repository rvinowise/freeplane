def recentBookmarkNodes = c.find { it.text == 'recent' && it.parent?.text == 'Bookmarks' }
if (recentBookmarkNodes) {
    def recentNode = recentBookmarkNodes.get(0).link.node
    if (recentNode) {
        c.select(recentNode)
    }
}