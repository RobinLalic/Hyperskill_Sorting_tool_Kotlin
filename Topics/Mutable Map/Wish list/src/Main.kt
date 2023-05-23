fun makeMyWishList(wishList: Map<String, Int>, limit: Int): MutableMap<String, Int> {
    val trueWishList = wishList.toMutableMap()
    for ( (k,v) in wishList) {
        if (v > limit) {
            trueWishList.remove(k)
        }
    }
    return trueWishList
}