package v2015.oasis.pilani.bits.com.home.events.outer

data class OuterData(val heading: String, val color: Int){
    override fun equals(other: Any?): Boolean {
        if (other != null && other is OuterData)
            return this.heading == other.heading
        else
            return false
    }
}
